package dev.notune.transcribe;

import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Persists the UI language choice and applies it.
 *
 * <p>Stored as a small marker file in the app's private files dir (not
 * SharedPreferences), matching the existing marker-file settings
 * (auto_record, theme_mode, …).
 *
 * <p>Values are BCP 47 tags for every locale the app ships:
 * {@code en}, {@code de}, {@code es}, {@code fr}, {@code it},
 * {@code pt}, {@code ru}, {@code zh-Hans} — plus {@code "auto"}
 * (the default), which clears the app-local override so the UI follows
 * the system language. Applied with
 * {@link AppCompatDelegate#setApplicationLocales}, which recreates running
 * activities; on API 33+ the choice is also surfaced in the system
 * per-app language settings via {@code res/xml/locales_config.xml}.
 */
public final class LocalePrefs {
    /** Follow the system language (clears any app-local override). */
    public static final String AUTO = "auto";

    public static final String EN = "en";
    public static final String DE = "de";
    public static final String ES = "es";
    public static final String FR = "fr";
    public static final String IT = "it";
    public static final String PT = "pt";
    public static final String RU = "ru";
    public static final String ZH_HANS = "zh-Hans";

    /** All selectable UI languages, in menu display order. */
    public static final List<String> SUPPORTED = Collections.unmodifiableList(
            Arrays.asList(EN, ES, DE, FR, IT, PT, RU, ZH_HANS));

    private static final Map<String, String> NATIVE_NAMES;
    static {
        Map<String, String> names = new LinkedHashMap<>();
        names.put(EN, "English");
        names.put(ES, "Español");
        names.put(DE, "Deutsch");
        names.put(FR, "Français");
        names.put(IT, "Italiano");
        names.put(PT, "Português");
        names.put(RU, "Русский");
        names.put(ZH_HANS, "中文（简体）");
        NATIVE_NAMES = Collections.unmodifiableMap(names);
    }

    private static final String FILE_NAME = "app_ui_language";

    private LocalePrefs() {}

    /** Menu display name of a tag, always written in the language itself. */
    public static String nativeName(String tag) {
        String name = NATIVE_NAMES.get(tag);
        return name != null ? name : tag;
    }

    public static boolean isSupported(String tag) {
        return tag != null && SUPPORTED.contains(tag.trim());
    }

    /** Stored value: {@code "auto"} or a supported tag. Unknown → {@code "auto"}. */
    public static String getTag(Context context) {
        String tag = MarkerFileHelper.readString(context, FILE_NAME, AUTO);
        if (tag == null) return AUTO;
        tag = tag.trim();
        if (tag.isEmpty() || AUTO.equals(tag)) return AUTO;
        return SUPPORTED.contains(tag) ? tag : AUTO;
    }

    /** Persists the choice and applies it (AppCompat recreates activities). */
    public static void setTag(Context context, String tag) {
        String normalized = tag == null ? AUTO : tag.trim();
        if (!AUTO.equals(normalized) && !SUPPORTED.contains(normalized)) {
            normalized = AUTO;
        }
        MarkerFileHelper.writeString(context, FILE_NAME, normalized);
        apply(context);
    }

    /** Applies the persisted choice. Call from Application.onCreate(). */
    public static void apply(Context context) {
        String tag = getTag(context);
        if (AUTO.equals(tag)) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList());
        } else {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag));
        }
    }
}
