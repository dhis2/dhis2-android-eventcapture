    # this is an open source project, hassle of obfuscation not worth it
    -dontobfuscate

    # ignoring warnings for now. have a look in the gradle console or event log
    # to see if you can improve upon warned issues
    -ignorewarnings

    # prevents crash in DatabaseHolder
    -keep class * extends com.raizlabs.android.dbflow.config.DatabaseHolder { *; }

    # Dagger
    -dontwarn dagger.internal.codegen.**
    -keepclassmembers,allowobfuscation class * {
        @javax.inject.* *;
        @dagger.* *;
        <init>();
    }
    -keep class dagger.* { *; }
    -keep class javax.inject.* { *; }
    -keep class * extends dagger.internal.Binding
    -keep class * extends dagger.internal.ModuleAdapter
    -keep class * extends dagger.internal.StaticInjection