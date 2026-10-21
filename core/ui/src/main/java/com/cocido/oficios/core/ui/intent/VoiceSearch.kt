package com.cocido.oficios.core.ui.intent

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

const val VOICE_SEARCH_LANGUAGE_AR = "es-AR"
const val VOICE_SEARCH_LANGUAGE_ES = "es-ES"

fun preferredVoiceSearchLanguage(
    availableLanguageTags: Collection<String>? = null,
    deviceLanguageTag: String = Locale.getDefault().toLanguageTag(),
): String {
    val preferred = listOf(VOICE_SEARCH_LANGUAGE_AR, VOICE_SEARCH_LANGUAGE_ES)
    if (availableLanguageTags.isNullOrEmpty()) return VOICE_SEARCH_LANGUAGE_AR
    val normalized = availableLanguageTags.map { it.replace('_', '-') }
    preferred.forEach { lang ->
        if (normalized.any { it.equals(lang, ignoreCase = true) }) return lang
    }
    normalized.firstOrNull { it.startsWith("es", ignoreCase = true) }?.let { return it }
    return deviceLanguageTag.ifBlank { VOICE_SEARCH_LANGUAGE_ES }
}

fun createVoiceSearchIntent(
    prompt: String? = null,
    languageTag: String = preferredVoiceSearchLanguage(),
): Intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
    putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageTag)
    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
    if (!prompt.isNullOrBlank()) {
        putExtra(RecognizerIntent.EXTRA_PROMPT, prompt)
    }
}

fun isVoiceRecognitionPresent(
    recognitionAvailable: Boolean,
    activityResolved: Boolean,
): Boolean = recognitionAvailable || activityResolved

fun isVoiceRecognitionPresent(
    context: Context,
    intent: Intent = createVoiceSearchIntent(),
): Boolean = isVoiceRecognitionPresent(
    recognitionAvailable = SpeechRecognizer.isRecognitionAvailable(context),
    activityResolved = context.packageManager.resolveActivity(
        intent,
        PackageManager.MATCH_DEFAULT_ONLY,
    ) != null,
)

fun spokenQueryFromRecognition(resultCode: Int, results: List<String>?): String? {
    if (resultCode != Activity.RESULT_OK) return null
    return results?.firstOrNull()?.trim()?.takeIf { it.isNotEmpty() }
}

fun spokenQueryFromActivityResult(resultCode: Int, data: Intent?): String? =
    spokenQueryFromRecognition(
        resultCode = resultCode,
        results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS),
    )

fun dispatchSpokenSearch(
    spoken: String?,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
): Boolean {
    val query = spoken?.trim()?.takeIf { it.isNotEmpty() } ?: return false
    onQueryChange(query)
    onSearch(query)
    return true
}

@Composable
fun rememberVoiceSearchLauncher(
    prompt: String? = null,
    onSpokenQuery: (String) -> Unit,
    onUnavailable: () -> Unit,
): () -> Unit {
    val context = LocalContext.current
    val onSpokenQueryState = rememberUpdatedState(onSpokenQuery)
    val onUnavailableState = rememberUpdatedState(onUnavailable)
    val promptState = rememberUpdatedState(prompt)
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val query = spokenQueryFromActivityResult(result.resultCode, result.data)
        if (query != null) {
            onSpokenQueryState.value(query)
        }
    }
    return remember(launcher, context) {
        {
            val intent = createVoiceSearchIntent(prompt = promptState.value)
            if (!isVoiceRecognitionPresent(context, intent)) {
                onUnavailableState.value()
            } else {
                try {
                    launcher.launch(intent)
                } catch (_: ActivityNotFoundException) {
                    onUnavailableState.value()
                }
            }
        }
    }
}
