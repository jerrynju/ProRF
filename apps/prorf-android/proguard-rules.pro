# ProRF ProGuard/R8 rules
# Keep all RF domain and platform classes accessible at runtime
-keep class com.prorf.** { *; }
-keepattributes *Annotation*
-keepclassmembers class * extends android.app.Application { *; }
# kotlinx.serialization reflection
-keepattributes SourceFile,LineNumberTable
-keep class kotlinx.serialization.** { *; }
