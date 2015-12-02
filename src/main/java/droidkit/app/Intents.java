package droidkit.app;

import android.app.SearchManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.BundleCompat;
import android.text.TextUtils;

import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

/**
 * @author Daniel Serdyukov
 */
public abstract class Intents {

    private Intents() {
    }

    public static boolean hasResolution(@NonNull Context context, @NonNull Intent intent) {
        final PackageManager pm = context.getPackageManager();
        return pm.resolveActivity(intent, 0) != null || pm.resolveService(intent, 0) != null;
    }

    public static void startActivity(@NonNull Context context, @NonNull Intent intent,
                                     @Nullable CharSequence title) {
        if (context.getPackageManager().resolveActivity(intent, 0) != null) {
            context.startActivity(intent);
        } else {
            context.startActivity(Intent.createChooser(intent, title));
        }
    }

    public static void startService(@NonNull Context context, @NonNull Class<? extends Service> clazz) {
        startService(context, clazz, Bundle.EMPTY);
    }

    public static void startService(@NonNull Context context, @NonNull Class<? extends Service> clazz,
                                    @NonNull Bundle extras) {
        final Intent intent = new Intent(context, clazz);
        intent.putExtras(extras);
        if (context.getPackageManager().resolveService(intent, 0) != null) {
            context.startService(intent);
        } else {
            throw new IllegalArgumentException("Check that service registered in AndroidManifest.xml");
        }
    }

    public abstract static class Common {

        private Common() {
        }

        @NonNull
        public static Intent openUrl(@NonNull String url) {
            return openUrl(url, true);
        }

        @NonNull
        public static Intent openUrl(@NonNull String url, boolean showInChromeTabsIfSupported) {
            final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            if (showInChromeTabsIfSupported) {
                final Bundle bundle = new Bundle();
                BundleCompat.putBinder(bundle, "android.support.customtabs.extra.SESSION", null);
                intent.putExtras(bundle);
            }
            return intent;
        }

        @NonNull
        public static Intent search(@NonNull String query) {
            final Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
            intent.putExtra(SearchManager.QUERY, query);
            return intent;
        }

        @NonNull
        public static Intent sendEmail(@NonNull String[] to, @NonNull String subject, @NonNull String body,
                                       @NonNull Uri... attachments) {
            final Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            intent.setType("message/rfc822");
            intent.putExtra(Intent.EXTRA_EMAIL, to);
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
            final ArrayList<CharSequence> extraText = new ArrayList<>(1);
            extraText.add(body);
            intent.putCharSequenceArrayListExtra(Intent.EXTRA_TEXT, extraText);
            if (attachments.length > 0) {
                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,
                        new ArrayList<Parcelable>(Arrays.asList(attachments)));
            }
            return intent;
        }

        @NonNull
        public static Intent sendSms(@NonNull String to, @NonNull String message) {
            final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + to));
            intent.putExtra("sms_body", message);
            return intent;
        }

        @NonNull
        public static Intent openContent(@NonNull Uri uri) {
            final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            final String mime = URLConnection.guessContentTypeFromName(uri.toString());
            if (!TextUtils.isEmpty(mime)) {
                intent.setType(mime);
            }
            return intent;
        }

        @NonNull
        public static Intent openDialer(@NonNull String number) {
            return new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number));
        }

    }

    public abstract static class Pick {

        private Pick() {
        }

        @NonNull
        public static Intent contact() {
            return new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        }

        @NonNull
        public static Intent file(@NonNull String mime) {
            final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType(mime);
            return intent;
        }

        @NonNull
        public static Intent image() {
            final Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
            return intent;
        }

        @NonNull
        public static Intent video() {
            final Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType(MediaStore.Video.Media.CONTENT_TYPE);
            return intent;
        }

    }

    public abstract static class Camera {

        private Camera() {
        }

        @NonNull
        public static Intent capturePhoto(@Nullable Uri output) {
            final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (output != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, output);
            }
            return intent;
        }

        @NonNull
        public static Intent captureVideo(@Nullable Uri output) {
            final Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            if (output != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, output);
            }
            return intent;
        }

    }

    public abstract static class Maps {

        static final Uri MAPS_URI = Uri.parse("https://maps.google.com/maps");

        private Maps() {
        }

        @NonNull
        public static Intent openMaps() {
            return new Intent(Intent.ACTION_VIEW, MAPS_URI);
        }

        @NonNull
        public static Intent openMaps(double lat, double lng) {
            return new Intent(Intent.ACTION_VIEW, MAPS_URI.buildUpon()
                    .appendQueryParameter("q", formatLatLng(lat, lng))
                    .build());
        }

        @NonNull
        public static Intent route(double lat, double lng) {
            return new Intent(Intent.ACTION_VIEW, MAPS_URI.buildUpon()
                    .appendQueryParameter("daddr", formatLatLng(lat, lng))
                    .build());
        }

        @NonNull
        public static Intent route(double fromLat, double fromLng, double toLat, double toLng) {
            return new Intent(Intent.ACTION_VIEW, MAPS_URI.buildUpon()
                    .appendQueryParameter("saddr", formatLatLng(fromLat, fromLng))
                    .appendQueryParameter("daddr", formatLatLng(toLat, toLng))
                    .build());
        }

        @NonNull
        public static Intent search(@NonNull String query) {
            return new Intent(Intent.ACTION_VIEW, MAPS_URI.buildUpon()
                    .appendQueryParameter("q", query)
                    .build());
        }

        static String formatLatLng(double lat, double lng) {
            return String.format(Locale.US, "%.5f,%.5f", lat, lng);
        }

    }

    public abstract static class PlayStore {

        static final String MARKET = "market";

        static final String DETAILS = "details";

        static final String APPS = "apps";

        static final String MOVIES = "movies";

        static final String MUSIC = "music";

        static final String BOOKS = "books";

        static final String SEARCH = "search";

        static final String ID = "id";

        static final String Q = "q";

        static final String C = "c";

        private PlayStore() {
        }

        @NonNull
        public static Intent details(@NonNull Context context) {
            return details(context.getPackageName());
        }

        @NonNull
        public static Intent details(@NonNull String packageName) {
            return new Intent(Intent.ACTION_VIEW, new Uri.Builder()
                    .scheme(MARKET)
                    .authority(DETAILS)
                    .appendQueryParameter(ID, packageName)
                    .build());
        }

        @NonNull
        public static Intent publisher(@NonNull String publisher) {
            return new Intent(Intent.ACTION_VIEW, new Uri.Builder()
                    .scheme(MARKET)
                    .authority(SEARCH)
                    .appendQueryParameter(Q, "pub:" + publisher)
                    .build());
        }

        @NonNull
        public static Intent search(@NonNull String query) {
            return new Intent(Intent.ACTION_VIEW, new Uri.Builder()
                    .scheme(MARKET)
                    .authority(SEARCH)
                    .appendQueryParameter(Q, query)
                    .build());
        }

        @NonNull
        public static Intent apps(@NonNull String query) {
            return search(query, APPS);
        }

        @NonNull
        public static Intent movies(@NonNull String query) {
            return search(query, MOVIES);
        }

        @NonNull
        public static Intent music(@NonNull String query) {
            return search(query, MUSIC);
        }

        @NonNull
        public static Intent books(@NonNull String query) {
            return search(query, BOOKS);
        }

        @NonNull
        private static Intent search(@NonNull String query, @NonNull String category) {
            return new Intent(Intent.ACTION_VIEW, new Uri.Builder()
                    .scheme(MARKET)
                    .authority(SEARCH)
                    .appendQueryParameter(Q, query)
                    .appendQueryParameter(C, category)
                    .build());
        }

    }

    public abstract static class Share {

        private Share() {
        }

        @NonNull
        public static Intent text(@NonNull String text) {
            return share("text/*", text);
        }

        @NonNull
        public static Intent image(@NonNull Uri... attachments) {
            return share("image/*", null, attachments);
        }

        @NonNull
        public static Intent video(@NonNull Uri... attachments) {
            return share("video/*", null, attachments);
        }

        @NonNull
        public static Intent share(@Nullable String text, @NonNull Uri... attachments) {
            return share("*/*", text, attachments);
        }

        @NonNull
        private static Intent share(@NonNull String mime, @Nullable String text, @NonNull Uri... attachments) {
            final Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            intent.setType(mime);
            if (!TextUtils.isEmpty(text)) {
                final ArrayList<CharSequence> extras = new ArrayList<>(1);
                extras.add(text);
                intent.putCharSequenceArrayListExtra(Intent.EXTRA_TEXT, extras);
            }
            if (attachments.length > 0) {
                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,
                        new ArrayList<Parcelable>(Arrays.asList(attachments)));
            }
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            return intent;
        }

    }

}
