import json
import hashlib
import os
from datetime import datetime, timezone
from email.utils import parsedate_to_datetime
from urllib.request import Request, urlopen
import xml.etree.ElementTree as ET


ROOT_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SOURCES_PATH = os.path.join(ROOT_DIR, "news_sources.json")
OUTPUT_DIR = os.path.join(ROOT_DIR, "docs", "news")
OUTPUT_PATH = os.path.join(OUTPUT_DIR, "index.json")


def fetch_text(url: str) -> str:
    req = Request(url, headers={"User-Agent": "Mozilla/5.0"})
    with urlopen(req, timeout=20) as response:
        return response.read().decode("utf-8", errors="ignore")


def safe_text(value):
    if value is None:
        return ""
    return " ".join(value.split()).strip()


def parse_pub_date(value: str) -> str:
    value = safe_text(value)
    if not value:
        return datetime.now(timezone.utc).isoformat()

    try:
        dt = parsedate_to_datetime(value)
        if dt.tzinfo is None:
            dt = dt.replace(tzinfo=timezone.utc)
        return dt.astimezone(timezone.utc).isoformat()
    except Exception:
        return datetime.now(timezone.utc).isoformat()


def make_id(source: str, title: str, url: str) -> str:
    base = f"{source}|{title}|{url}"
    return hashlib.sha256(base.encode("utf-8")).hexdigest()[:24]


def parse_rss(source_info: dict) -> list:
    xml_text = fetch_text(source_info["rss"])
    root = ET.fromstring(xml_text)

    items = []
    for item in root.findall(".//item"):
        title = safe_text(item.findtext("title"))
        link = safe_text(item.findtext("link"))
        summary = safe_text(item.findtext("description"))
        pub_date = parse_pub_date(item.findtext("pubDate"))

        if not title or not link:
            continue

        items.append({
            "id": make_id(source_info["name"], title, link),
            "title": title,
            "summary": summary[:350],
            "url": link,
            "imageUrl": None,
            "source": source_info["name"],
            "country": source_info["country"],
            "category": source_info["category"],
            "publishedAt": pub_date
        })

    return items


def main():
    with open(SOURCES_PATH, "r", encoding="utf-8") as f:
        config = json.load(f)

    all_items = []
    for source in config["sources"]:
        try:
            all_items.extend(parse_rss(source))
        except Exception as e:
            print(f"Skipping source {source['name']}: {e}")

    dedup = {}
    for item in all_items:
        key = item["url"]
        if key not in dedup:
            dedup[key] = item

    items = list(dedup.values())
    items.sort(key=lambda x: x["publishedAt"], reverse=True)
    items = items[:300]

    output = {
        "updatedAt": datetime.now(timezone.utc).isoformat(),
        "items": items
    }

    os.makedirs(OUTPUT_DIR, exist_ok=True)
    with open(OUTPUT_PATH, "w", encoding="utf-8") as f:
        json.dump(output, f, ensure_ascii=False, indent=2)

    print(f"Generated {len(items)} items at {OUTPUT_PATH}")


if __name__ == "__main__":
    main()
