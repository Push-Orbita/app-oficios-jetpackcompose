package com.cocido.oficios.core.network.di

/**
 * Redacts secrets from OkHttp log lines (JSON bodies, form fields, headers echoed in logs).
 * Pure string transform so it can be unit-tested without Android Log.
 */
internal object SensitiveLogRedactor {

    private val SENSITIVE_KEYS = listOf(
        "contrasena",
        "password",
        "nuevaContrasena",
        "confirmPassword",
        "confirmarContrasena",
        "token",
        "accessToken",
        "refreshToken",
        "resetToken",
        "codigo",
        "otp",
        "code",
        "dni",
        "documento",
        "authorization",
    )

    private val keyAlternation = SENSITIVE_KEYS.joinToString("|") { Regex.escape(it) }

    /** "key"\s*:\s*"value" (JSON string values). */
    private val jsonStringValue = Regex(
        """(?i)("(?:$keyAlternation)"\s*:\s*")([^"]*)(")""",
    )

    /** "key"\s*:\s*number|true|false|null — uncommon for secrets but keep safe. */
    private val jsonBareValue = Regex(
        """(?i)("(?:$keyAlternation)"\s*:\s*)([^,}\]\s]+)""",
    )

    /** application/x-www-form-urlencoded: key=value */
    private val formUrlEncoded = Regex(
        """(?i)\b((?:$keyAlternation)=)([^&\s]*)""",
    )

    /** Multipart Content-Disposition name="key" — value is often on a following logged line. */
    private val multipartName = Regex(
        """(?i)(name="(?:$keyAlternation)")""",
    )

    private val bearer = Regex("""(?i)(Bearer\s+)(\S+)""")

    private const val REDACTED = "***"

    fun redact(message: String): String {
        if (message.isEmpty()) return message
        var out = message
        out = jsonStringValue.replace(out) { "${it.groupValues[1]}$REDACTED${it.groupValues[3]}" }
        out = formUrlEncoded.replace(out) { "${it.groupValues[1]}$REDACTED" }
        out = bearer.replace(out) { "${it.groupValues[1]}$REDACTED" }
        // Multipart: if this line is only the secret value after a disposition we already
        // marked, callers still need line-level protection for bare password lines.
        // Mark disposition lines; also redact lines that look like sole secret payloads
        // when the previous redaction left name="contrasena" visible (value on next log line).
        if (multipartName.containsMatchIn(out)) {
            out = multipartName.replace(out) { it.groupValues[1] }
            // Nothing else on disposition line usually.
        }
        return out
    }

    /**
     * Stateful helper for multipart bodies where the secret value is logged on the line
     * after Content-Disposition. Use from the OkHttp Logger.
     */
    class MultipartAwareLogger(
        private val emit: (String) -> Unit,
    ) {
        private var redactNextValueLine = false

        fun log(message: String) {
            if (redactNextValueLine) {
                val trimmed = message.trim()
                if (trimmed.isEmpty() || trimmed.startsWith("--") || trimmed.startsWith("Content-")) {
                    // keep scanning until the value line
                    emit(redact(message))
                    return
                }
                redactNextValueLine = false
                emit(REDACTED)
                return
            }
            val redacted = redact(message)
            if (multipartName.containsMatchIn(message)) {
                redactNextValueLine = true
            }
            emit(redacted)
        }
    }
}