package droidkit.app;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import droidkit.log.Logger;

/**
 * @author Daniel Serdyukov
 */
public final class Intents {

    private Intents() {
    }

    public static void startActivity(@NonNull Context context, @NonNull Intent intent,
                                     @Nullable CharSequence title) {
        if (context.getPackageManager().resolveActivity(intent, 0) != null) {
            context.startActivity(Intent.createChooser(intent, title));
        } else {
            context.startActivity(intent);
        }
    }

    public static void startService(@NonNull Context context, @NonNull Intent intent) {
        if (context.getPackageManager().resolveService(intent, 0) != null) {
            context.startService(intent);
        } else {
            Logger.error("No matching service was found.");
        }
    }

    public static final class Common {

        private Common() {
        }

        @NonNull
        public static Intent openUrl(@NonNull String url) {
            return new Intent(Intent.ACTION_VIEW, Uri.parse(url));
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

    public static final class Pick {

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

    public static final class Camera {

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

    public static final class Maps {

        private static final String MAPS_URL = "https://maps.google.com/maps";

        private Maps() {
        }

        @NonNull
        public static Intent openMaps() {
            return new Intent(Intent.ACTION_VIEW, Uri.parse(MAPS_URL));
        }

        @NonNull
        public static Intent openMaps(double lat, double lng) {
            return new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(Locale.US, MAPS_URL +
                    "?q=%f,%f", lat, lng)));
        }

        @NonNull
        public static Intent route(double lat, double lng) {
            return new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(Locale.US, MAPS_URL +
                    "?daddr=%f,%f", lat, lng)));
        }

        @NonNull
        public static Intent route(double fromLat, double fromLng, double toLat, double toLng) {
            return new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(Locale.US, MAPS_URL +
                    "?saddr=%f,%f&daddr=%f,%f", fromLat, fromLng, toLat, toLng)));
        }

        @NonNull
        public static Intent search(@NonNull String query) {
            return new Intent(Intent.ACTION_VIEW, Uri.parse(MAPS_URL + "?q=" + query));
        }

    }

    public static final class PlayStore {

        public static final String APPS = "apps";

        public static final String MOVIES = "movies";

        public static final String MUSIC = "music";

        public static final String BOOKS = "books";

        private PlayStore() {
        }

        @NonNull
        public static Intent details(@NonNull Context context) {
            return details(context.getPackageName());
        }

        @NonNull
        public static Intent details(@NonNull String packageName) {
            return new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName));
        }

        @NonNull
        public static Intent publisher(@NonNull String publisher) {
            return new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pub:" + publisher));
        }

        @NonNull
        public static Intent search(@NonNull String query) {
            return new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=" + query));
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
            return new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=" + query + "&c=" + category));
        }

    }

    public static final class Share {

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
                intent.putExtra(Intent.EXTRA_TEXT, text);
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
