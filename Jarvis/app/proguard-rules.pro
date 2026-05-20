# Keep app components
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.app.Activity
-keep public class * extends android.accessibilityservice.AccessibilityService

# Keep Retrofit/OkHttp
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*

# Keep Jackson (used by Janus API models)
-keep class com.hmdm.control.janus.json.** { *; }
-keepclassmembers class * {
    @com.fasterxml.jackson.annotation.* <fields>;
}
-keep class com.fasterxml.jackson.** { *; }
-dontwarn com.fasterxml.jackson.databind.**

# Keep WebRTC
-keep class org.webrtc.** { *; }
-dontwarn org.webrtc.**

# Keep Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
