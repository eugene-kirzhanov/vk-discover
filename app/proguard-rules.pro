# Default values in:
# /android-sdk/tools/proguard/proguard-android.txt

-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

-keepattributes Annotation
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses
-keepattributes Exceptions

-keep public class * extends java.lang.Exception
-keep public class * implements java.lang.Throwable

# Keep all Enum values, including not used
-keepclassmembers class * extends java.lang.Enum {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

###############################
# Android
###############################

-dontwarn android.content.**
-dontwarn android.animation.**
-dontnote android.net.http.*
-dontnote org.apache.commons.codec.**
-dontnote org.apache.http.**

-keep public class * extends android.support.design.widget.CoordinatorLayout$Behavior {
    public <init>(android.content.Context, android.util.AttributeSet);
}

###############################
# Remove Logcat messages
###############################

-assumenosideeffects class android.util.Log {
    public static int v(...);
}

###############################
# Kotlin
###############################

-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keep class kotlin.Metadata { *; }
-dontnote kotlin.internal.PlatformImplementationsKt

-keep class kotlin.reflect.jvm.internal.** { *; }
-dontnote kotlin.reflect.jvm.internal.**

# Kotlin Experimental
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

###############################
## Android architecture components
###############################

# LifecycleObserver's empty constructor is considered to be unused by proguard
-keepclassmembers class * implements androidx.lifecycle.LifecycleObserver {
    <init>(...);
}
# ViewModel's empty constructor is considered to be unused by proguard
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
# keep Lifecycle State and Event enums values
-keepclassmembers class androidx.lifecycle.Lifecycle$State { *; }
-keepclassmembers class androidx.lifecycle.Lifecycle$Event { *; }
# keep methods annotated with @OnLifecycleEvent even if they seem to be unused
# (Mostly for LiveData.LifecycleBoundObserver.onStateChange(), but who knows)
-keepclassmembers class * {
    @androidx.lifecycle.OnLifecycleEvent *;
}
-keep class * implements androidx.lifecycle.LifecycleObserver {
    <init>(...);
}
-keep class * implements androidx.lifecycle.GeneratedAdapter {
    <init>(...);
}
-keepclassmembers class androidx.lifecycle.** { *; }
-keep class androidx.lifecycle.** { *; }
-dontwarn androidx.lifecycle.**

###############################
# AndroidX
###############################

-dontnote android.databinding.**
-keep class android.databinding.** { *; }
-dontnote com.google.android.material.**

###############################
# Dagger
###############################

-dontwarn com.google.errorprone.annotations.**

###############################
# Glide
###############################

-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep class com.bumptech.glide.GeneratedAppGlideModuleImpl

-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

-dontwarn com.bumptech.glide.load.resource.bitmap.VideoDecoder

###############################
# VK Android SDK
###############################

-keep class com.vk.** { *; }