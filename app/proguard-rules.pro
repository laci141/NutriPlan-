# NutriPlan – ProGuard szabályok

# Kotlinx Serialization – a generált serializerek megőrzése
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.nutriplan.app.**$$serializer { *; }
-keepclassmembers class com.nutriplan.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.nutriplan.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Room – entitások megőrzése
-keep class com.nutriplan.app.data.local.entity.** { *; }
