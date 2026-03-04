#!/bin/bash

echo "=== AuryxBrowser Build Script ==="
echo "Building AuryxBrowser v1.305.01"
echo ""

# Check if running in CI or local
if [ -z "$CI" ]; then
    echo "Running local build..."
else
    echo "Running CI build..."
fi

# Navigate to project directory
cd "$(dirname "$0")/.."

# Clean previous builds
echo "Cleaning previous builds..."
./gradlew clean

# Build debug APK
echo ""
echo "Building Debug APK..."
./gradlew assembleDebug

if [ $? -eq 0 ]; then
    echo ""
    echo "✓ Debug APK built successfully!"
    echo "  Location: app/build/outputs/apk/debug/app-debug.apk"
else
    echo "✗ Debug build failed!"
    exit 1
fi

# Build release APK
echo ""
echo "Building Release APK..."
./gradlew assembleRelease

if [ $? -eq 0 ]; then
    echo ""
    echo "✓ Release APK built successfully!"
    echo "  Location: app/build/outputs/apk/release/app-release-unsigned.apk"
else
    echo "✗ Release build failed!"
    exit 1
fi

echo ""
echo "=== Build Complete ==="
