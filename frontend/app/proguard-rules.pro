# Retrofit & Gson rules
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.hostelmanagement.app.data.remote.dto.** { *; }

# Coroutines rules
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Hilt rules
-keep,allowobfuscation,allowshrinking class * extends androidx.lifecycle.ViewModel
