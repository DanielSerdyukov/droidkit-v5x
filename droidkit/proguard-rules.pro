-keepattributes Signature,Exceptions,InnerClasses,EnclosingMethod,*Annotation*

-keep public interface droidkit.** { *; }

-keep public class droidkit.** {
    public static <fields>;
    public static <methods>;
    public <methods>;
    protected <methods>;
}

-keepclassmembers public class droidkit.sqlite.** {
    @android.support.annotation.Keep <methods>;
}

-dontwarn javax.**
-dontwarn com.squareup.**
