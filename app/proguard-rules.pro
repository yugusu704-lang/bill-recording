# Room rules
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.room.* <methods>;
    @androidx.room.* <fields>;
}
-keep class com.localbill.recording.data.entity.** { *; }
-keep class com.localbill.recording.data.dao.** { *; }
-keep class com.localbill.recording.data.AppDatabase { *; }

# Gson rules
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.localbill.recording.data.model.** { *; }
-keepclassmembers class com.localbill.recording.data.model.** { *; }

# Compose rules
-keep class androidx.compose.** { *; }
