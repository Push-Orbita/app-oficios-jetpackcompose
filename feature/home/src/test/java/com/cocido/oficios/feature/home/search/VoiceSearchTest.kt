package com.cocido.oficios.feature.home.search

import android.app.Activity
import android.speech.RecognizerIntent
import com.cocido.oficios.core.ui.intent.VOICE_SEARCH_LANGUAGE_AR
import com.cocido.oficios.core.ui.intent.VOICE_SEARCH_LANGUAGE_ES
import com.cocido.oficios.core.ui.intent.createVoiceSearchIntent
import com.cocido.oficios.core.ui.intent.dispatchSpokenSearch
import com.cocido.oficios.core.ui.intent.isVoiceRecognitionPresent
import com.cocido.oficios.core.ui.intent.preferredVoiceSearchLanguage
import com.cocido.oficios.core.ui.intent.spokenQueryFromActivityResult
import com.cocido.oficios.core.ui.intent.spokenQueryFromRecognition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class VoiceSearchTest {

    @Test
    fun `prefers es-AR then es-ES then device default`() {
        assertEquals(VOICE_SEARCH_LANGUAGE_AR, preferredVoiceSearchLanguage())
        assertEquals(
            VOICE_SEARCH_LANGUAGE_AR,
            preferredVoiceSearchLanguage(availableLanguageTags = listOf("en-US", "es-AR", "es-ES")),
        )
        assertEquals(
            VOICE_SEARCH_LANGUAGE_ES,
            preferredVoiceSearchLanguage(availableLanguageTags = listOf("en-US", "es-ES")),
        )
        assertEquals(
            "es-MX",
            preferredVoiceSearchLanguage(availableLanguageTags = listOf("en-US", "es-MX")),
        )
        assertEquals(
            "en-US",
            preferredVoiceSearchLanguage(
                availableLanguageTags = listOf("en-US"),
                deviceLanguageTag = "en-US",
            ),
        )
    }

    @Test
    fun `intent uses recognize speech with preferred language`() {
        val intent = createVoiceSearchIntent(
            prompt = "¿Qué oficio está buscando?",
            languageTag = VOICE_SEARCH_LANGUAGE_AR,
        )
        assertEquals(RecognizerIntent.ACTION_RECOGNIZE_SPEECH, intent.action)
        assertEquals(
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
            intent.getStringExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL),
        )
        assertEquals(VOICE_SEARCH_LANGUAGE_AR, intent.getStringExtra(RecognizerIntent.EXTRA_LANGUAGE))
        assertEquals(
            VOICE_SEARCH_LANGUAGE_AR,
            intent.getStringExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE),
        )
        assertEquals("¿Qué oficio está buscando?", intent.getStringExtra(RecognizerIntent.EXTRA_PROMPT))
        assertEquals(1, intent.getIntExtra(RecognizerIntent.EXTRA_MAX_RESULTS, -1))
    }

    @Test
    fun `spoken query takes first trimmed result`() {
        assertEquals(
            "plomero",
            spokenQueryFromRecognition(Activity.RESULT_OK, listOf("  plomero ", "otro")),
        )
    }

    @Test
    fun `cancel empty or blank spoken query is ignored`() {
        assertNull(spokenQueryFromRecognition(Activity.RESULT_CANCELED, listOf("plomero")))
        assertNull(spokenQueryFromRecognition(Activity.RESULT_OK, emptyList()))
        assertNull(spokenQueryFromRecognition(Activity.RESULT_OK, listOf("   ")))
        assertNull(spokenQueryFromRecognition(Activity.RESULT_OK, null))
        assertNull(spokenQueryFromActivityResult(Activity.RESULT_CANCELED, null))
    }

    @Test
    fun `dispatch spoken search fills query and triggers search`() {
        var query: String? = null
        var searched: String? = null
        assertTrue(
            dispatchSpokenSearch(
                spoken = "  gasista ",
                onQueryChange = { query = it },
                onSearch = { searched = it },
            ),
        )
        assertEquals("gasista", query)
        assertEquals("gasista", searched)
    }

    @Test
    fun `dispatch spoken search ignores cancel and blank`() {
        var called = false
        assertFalse(dispatchSpokenSearch(null, { called = true }, { called = true }))
        assertFalse(dispatchSpokenSearch("  ", { called = true }, { called = true }))
        assertFalse(called)
    }

    @Test
    fun `voice is available if recognizer or activity resolves`() {
        assertTrue(isVoiceRecognitionPresent(recognitionAvailable = true, activityResolved = false))
        assertTrue(isVoiceRecognitionPresent(recognitionAvailable = false, activityResolved = true))
        assertFalse(isVoiceRecognitionPresent(recognitionAvailable = false, activityResolved = false))
    }
}
