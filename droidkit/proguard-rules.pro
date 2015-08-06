-dontpreverify

-keepattributes Signature,Exceptions,InnerClasses,*Annotation*

-keep public interface droidkit.** { *; }

-keep public class droidkit.** {
    public static <fields>;
    public static <methods>;
    public <methods>;
    protected <methods>;
}

-keep class droidkit.sqlite.SQLiteProvider {
    static <methods>;
}

-keep class droidkit.sqlite.SQLiteSchema {
    static <methods>;
}

-dontwarn javax.**
-dontwarn com.squareup.**
