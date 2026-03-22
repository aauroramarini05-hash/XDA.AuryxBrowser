# Add project specific ProGuard rules here.
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

# Keep WebView JavaScript interface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep data classes
-keep class com.xdustatom.auryxbrowser.models.** { *; }
-keep class com.xdustatom.auryxbrowser.news.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep class okio.** { *; }

# Gson
-keep class com.google.gson.** { *; }
-keepattributes Signature
