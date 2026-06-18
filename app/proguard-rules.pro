# ==========================================================================
# NutriPlan – szigorított R8 / ProGuard szabályok (Release build)
# Cél: méretcsökkentés, obfuszkáció és a metaadat-szivárgások megszüntetése.
# ==========================================================================

# --- Obfuszkáció és optimalizálás erőssége ---
# Több optimalizációs kör; a kódot átnevezzük (obfuszkáljuk).
-optimizationpasses 5
-allowaccessmodification
-repackageclasses ""

# --- Forrásinformációk eltávolítása (biztonság) ---
# A SourceFile és LineNumberTable attribútumokat NEM tartjuk meg, így a
# stack trace-ekből nem derül ki a forrásfájl neve és a sorszám.
# A forrásfájl nevét ezen felül egy semmitmondó értékre cseréljük.
-renamesourcefileattribute SourceFile
-keepattributes !SourceFile,!LineNumberTable,!LocalVariableTable,!LocalVariableTypeTable

# --- Szükséges attribútumok megtartása ---
# A generikusokhoz, annotációkhoz és a szerializációhoz ezek kellenek.
-keepattributes Signature, *Annotation*, EnclosingMethod, InnerClasses

# ==========================================================================
# kotlinx.serialization – a generált serializerek megőrzése
# ==========================================================================
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
# A saját, @Serializable osztályaink (backup DTO-k) és serializereik megtartása.
-keep,includedescriptorclasses class com.nutriplan.app.**$$serializer { *; }
-keepclassmembers class com.nutriplan.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.nutriplan.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}
# A biztonsági mentés adatosztályainak mezői (JSON kulcsok) maradjanak meg.
-keep class com.nutriplan.app.data.backup.** { *; }

# ==========================================================================
# Room – az adatbázis entitások megtartása (a mezőnevek = oszlopnevek)
# ==========================================================================
-keep class com.nutriplan.app.data.local.entity.** { *; }

# ==========================================================================
# Általános, biztonságos megtartások
# ==========================================================================
# Az enumok values()/valueOf() metódusai (a fromKey és entries működéséhez).
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
# Parcelable CREATOR mezők.
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# A Hilt, a Room és a Compose saját "consumer" szabályaikat automatikusan
# hozzáadják, ezért azokhoz itt nincs szükség külön szabályra.

# --- Naplózás eltávolítása release-ben (opcionális méret/biztonság) ---
# A részletes (debug/verbose) naplóhívások kiejtése a release csomagból.
-assumenosideeffects class android.util.Log {
    public static int d(...);
    public static int v(...);
}

# ==========================================================================
# ML Kit – vonalkód-felismerés (a beágyazott modell betöltőjének megőrzése)
# ==========================================================================
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.internal.mlkit_vision_barcode.** { *; }
-dontwarn com.google.mlkit.**
