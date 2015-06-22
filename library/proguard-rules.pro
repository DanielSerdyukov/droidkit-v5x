-dontpreverify

-keepattributes Signature,Exceptions,*Annotation*

-keep public interface droidkit.** { *; }

-keep public class droidkit.** {
    public static <fields>;
    public static <methods>;
    public <methods>;
    protected <methods>;
}

-keepnames class droidkit.sqlite.SQLiteProvider {
    static final ** SCHEMA;
}

-dontwarn javax.**
-dontwarn com.squareup.**
-dontwarn droidkit.apt.**