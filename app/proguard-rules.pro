-keepattributes *Annotation*, InnerClasses, EnclosingMethod, Signature, RuntimeVisibleAnnotations

-keep class com.cocido.oficios.** { *; }

-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response

-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

-if class *
-keepclasseswithmembers class <1> {
    @kotlinx.serialization.SerialName <fields>;
}

-keep,includedescriptorclasses class com.cocido.oficios.**$$serializer { *; }
-keepclassmembers class com.cocido.oficios.** {
    *** Companion;
}
-keepclasseswithmembers class com.cocido.oficios.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-dontwarn org.conscrypt.**
-dontwarn okhttp3.internal.platform.**
