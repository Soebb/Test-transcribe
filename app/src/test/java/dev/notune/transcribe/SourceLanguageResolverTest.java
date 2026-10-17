package dev.notune.transcribe;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SourceLanguageResolverTest {

    // --- Script detection ---------------------------------------------------

    @Test
    public void chineseIdeographsDetectZh() {
        assertEquals("zh", SourceLanguageResolver.detectFromText("今天我们学习中文。"));
    }

    @Test
    public void mixedCjkWithLatinStillDetectsZh() {
        assertEquals("zh", SourceLanguageResolver.detectFromText("Red Note 的评论区"));
    }

    @Test
    public void japaneseKanaDetectsJa() {
        assertEquals("ja", SourceLanguageResolver.detectFromText("今日は日本語を勉強します。"));
    }

    @Test
    public void koreanHangulDetectsKo() {
        assertEquals("ko", SourceLanguageResolver.detectFromText("오늘은 한국어를 공부합니다."));
    }

    @Test
    public void cyrillicDetectsRu() {
        assertEquals("ru", SourceLanguageResolver.detectFromText("Сегодня мы учим русский язык."));
    }

    // --- Latin heuristic ----------------------------------------------------

    @Test
    public void spanishTildeDetectsEs() {
        assertEquals("es", SourceLanguageResolver.detectFromText("mañana niño"));
    }

    @Test
    public void spanishInvertedMarksDetectEs() {
        assertEquals("es", SourceLanguageResolver.detectFromText("¿Qué tal estás?"));
    }

    @Test
    public void germanSharpSDDetectsDe() {
        assertEquals("de", SourceLanguageResolver.detectFromText("Straße Fußball"));
    }

    @Test
    public void germanUmlautsDetectDe() {
        assertEquals("de", SourceLanguageResolver.detectFromText("über morgen Mädchen grüße"));
    }

    @Test
    public void frenchAccentsDetectFr() {
        // ê is a distinctive French diacritic (é/è/à are shared with Italian
        // and Portuguese and deliberately never decide on their own).
        assertEquals("fr", SourceLanguageResolver.detectFromText("L'être est dans la forêt"));
    }

    @Test
    public void italianDistinctiveAccentsDetectIt() {
        assertEquals("it", SourceLanguageResolver.detectFromText("più o meno"));
    }

    @Test
    public void portugueseTildeDetectsPt() {
        assertEquals("pt", SourceLanguageResolver.detectFromText("não são pão coração"));
    }

    @Test
    public void plainAsciiTextReturnsNull() {
        assertNull(SourceLanguageResolver.detectFromText("this is plain english text"));
    }

    @Test
    public void singleAmbiguousAccentReturnsNull() {
        // "café" — the é is shared by fr/it/pt; one occurrence must not decide.
        assertNull(SourceLanguageResolver.detectFromText("café"));
    }

    @Test
    public void emptyTextReturnsNull() {
        assertNull(SourceLanguageResolver.detectFromText(""));
        assertNull(SourceLanguageResolver.detectFromText(null));
    }

    @Test
    public void punctuationOnlyReturnsNull() {
        assertNull(SourceLanguageResolver.detectFromText("…"));
    }

    // --- model_language precedence ------------------------------------------

    @Test
    public void fixedModelLanguageOverridesDetection() {
        // Chinese speech with a fixed es-ES hint: the user says the audio is
        // Spanish, so the translator uses es regardless of the text's script.
        assertEquals("es", SourceLanguageResolver.resolve("今天我们学习中文。", "es-ES"));
    }

    @Test
    public void autoModelLanguageFallsBackToDetection() {
        assertEquals("zh", SourceLanguageResolver.resolve("今天我们学习中文。", "auto"));
        assertEquals("zh", SourceLanguageResolver.resolve("今天我们学习中文。", ""));
        assertEquals("zh", SourceLanguageResolver.resolve("今天我们学习中文。", null));
    }

    @Test
    public void unsupportedModelLanguageFallsBackToDetection() {
        assertEquals("zh", SourceLanguageResolver.resolve("今天我们学习中文。", "xx-XX"));
    }

    @Test
    public void autoModelLanguageWithLatinTextReturnsNull() {
        assertNull(SourceLanguageResolver.resolve("this is plain english text", "auto"));
    }

    // --- primaryCode mapping ------------------------------------------------

    @Test
    public void primaryCodeMapsSupportedTags() {
        assertEquals("en", SourceLanguageResolver.primaryCode("en-US"));
        assertEquals("es", SourceLanguageResolver.primaryCode("es-ES"));
        assertEquals("zh", SourceLanguageResolver.primaryCode("zh-CN"));
        assertEquals("pt", SourceLanguageResolver.primaryCode("pt-PT"));
    }

    @Test
    public void primaryCodeNullForAutoOrBlank() {
        assertNull(SourceLanguageResolver.primaryCode("auto"));
        assertNull(SourceLanguageResolver.primaryCode(""));
        assertNull(SourceLanguageResolver.primaryCode(null));
        assertNull(SourceLanguageResolver.primaryCode("  "));
    }

    @Test
    public void primaryCodeNullForUnsupported() {
        assertNull(SourceLanguageResolver.primaryCode("xx-XX"));
    }
        @Test
    public void primaryCodeNullForUnsupported() {
        assertNull(SourceLanguageResolver.primaryCode("xx-XX"));
    } →     @Test
    public void primaryCodeNullForUnsupported() {
        assertNull(SourceLanguageResolver.primaryCode("xx-XX"));
    }

    @Test
    public void primaryCodeCoversEveryTranslatableModelsScreenLanguage() {
        // Languages the Models screen offers that ML Kit can translate; these
        // used to return null, so a fixed source like Dutch never translated.
        String[] tags = {"bg-BG", "hr-HR", "cs-CZ", "da-DK", "nl-NL", "en-GB",
                "et-EE", "fi-FI", "el-GR", "hu-HU", "lv-LV", "lt-LT", "mt-MT",
                "pl-PL", "pt-BR", "ro-RO", "sk-SK", "sl-SI", "sv-SE", "uk-UA",
                "ar-SA", "hi-IN", "tr-TR", "fa-IR"};
        for (String tag : tags) {
            String expected = tag.substring(0, 2);
            assertEquals(tag, expected, SourceLanguageResolver.primaryCode(tag));
        }
    }

    @Test
    public void primaryCodeFoldsLegacyAliases() {
        assertEquals("he", SourceLanguageResolver.primaryCode("iw-IL"));
        assertEquals("id", SourceLanguageResolver.primaryCode("in-ID"));
        assertEquals("no", SourceLanguageResolver.primaryCode("nb-NO"));
    }

    // --- Language-ID result mapping -----------------------------------------
    @Test
    public void identifiedCodeAcceptsSupportedLanguages() {
        assertEquals("en", SourceLanguageResolver.identifiedCode("en"));
        assertEquals("fa", SourceLanguageResolver.identifiedCode("fa"));
        assertEquals("zh", SourceLanguageResolver.identifiedCode("zh"));
    }

    @Test
    public void identifiedCodeRejectsUndeterminedRomanizedAndUnsupported() {
        assertNull(SourceLanguageResolver.identifiedCode("und"));
        assertNull(SourceLanguageResolver.identifiedCode(""));
        assertNull(SourceLanguageResolver.identifiedCode(null));
        assertNull(SourceLanguageResolver.identifiedCode("ja-Latn"));
        assertNull(SourceLanguageResolver.identifiedCode("hi-Latn"));
        assertNull(SourceLanguageResolver.identifiedCode("xx"));
    }

    // --- pickSource (auto-detect order) -------------------------------------
    @Test
    public void plainEnglishIsTranslatableWhenLanguageIdNamesIt() {
        // The original bug: plain English resolved to null and was never
        // translated. Language ID now names it.
        assertEquals("en", SourceLanguageResolver.pickSource(
                "en", "this is plain english text", null));
    }

    @Test
    public void languageIdBeatsTheDiacriticHeuristic() {
        // "você" carries ê, which the heuristic scores as French.
        assertEquals("fr", SourceLanguageResolver.detectFromText("você"));
        assertEquals("pt", SourceLanguageResolver.pickSource("pt", "você", null));
    }

    @Test
    public void undeterminedFallsBackToHeuristic() {
        assertEquals("es", SourceLanguageResolver.pickSource("und", "mañana niño", null));
        assertEquals("ru", SourceLanguageResolver.pickSource(null, "Сегодня", null));
    }

    @Test
    public void undeterminedShortSegmentReusesSessionLanguage() {
        assertEquals("en", SourceLanguageResolver.pickSource("und", "OK", "en"));
    }

    @Test
    public void nothingKnownKeepsOriginal() {
        assertNull(SourceLanguageResolver.pickSource("und", "OK", null));
        assertNull(SourceLanguageResolver.pickSource(null, "…", null));
    }
}
