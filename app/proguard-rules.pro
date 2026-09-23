# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-keepclassmembers class br.com.rotacerta.colaborador.data.model.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}
# Retrofit
-keepattributes Signature, Exceptions
-keep,allowobfuscation interface br.com.rotacerta.colaborador.data.remote.RotaCertaApi
