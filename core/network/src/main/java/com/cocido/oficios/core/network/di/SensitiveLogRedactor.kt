package com.cocido.oficios.core.network.di

/**
 * Redacts secrets from OkHttp log lines (JSON bodies, form fields, headers echoed in logs).
 * Pure string transform so it can be unit-tested without Android Log.
 */
internal object SensitiveLogRedactor {

    private val SENSITIVE_KEYS = listOf(
        "contrasena",
        "contrasenia",
        "password",
        "nuevacontrasena",
        "confirmpassword",
        "confirmarcontrasena",
        "token",
        "accesstoken",
        "refreshtoken",
        "resettoken",
        "idtoken",
        "authtoken",
        "codigo",
        "otp",
        "code",
        "dni",
        "documento",
        "authorization",
    )

    private val SENSITIVE_NORMALIZED: Set<String> =
        SENSITIVE_KEYS.map { normalizeKey(it) }.toSet()

    private fun normalizeKey(raw: String): String =
        raw.lowercase().replace("_", "").replace("-", "")

    fun isSensitiveKey(raw: String): Boolean {
        val n = normalizeKey(raw)
        if (n.isEmpty()) return false
        if (n in SENSITIVE_NORMALIZED) return true
        if (n.endsWith("token")) return true
        if (n.contains("password") || n.contains("contrasena") || n.contains("contrasenia")) {
            return true
        }
        return false
    }

    /** "key": "value" (JSON string values). */
    private val jsonStringValue = Regex(
        """("([^"\\]+)"\s*:\s*")([^"]*)(")""",
    )

    /** "key": number|true|false|null — uncommon for secrets but keep safe. */
    private val jsonBareValue = Regex(
        """("([^"\\]+)"\s*:\s*)([^,"}\]\s]+)""",
    )

    /** application/x-www-form-urlencoded: key=value */
    private val formUrlEncoded = Regex(
        """(^|[&?\s])([^=&\s]+)=([^&\s]*)""",
    )

    /** Multipart Content-Disposition line (value often follows in the same log message). */
    private val dispositionLine = Regex(
        """Content-Disposition:[^\r\n]*""",
        RegexOption.IGNORE_CASE,
    )

    private val dispositionName = Regex(
        """name=(?:"([^"]+)"|'([^']+)'|([^\s;]+))""",
        RegexOption.IGNORE_CASE,
    )

    private val bearer = Regex("""(?i)(Bearer\s+)(\S+)""")

    private val authorizationHeader = Regex("""(?i)(authorization\s*:\s*)(\S.*)""")

    private const val REDACTED = "***"

    fun redact(message: String): String {
        if (message.isEmpty()) return message
        var out = redactMultipartParts(message)
        out = jsonStringValue.replace(out) { match ->
            val key = match.groupValues[2]
            if (isSensitiveKey(key)) "${match.groupValues[1]}$REDACTED${match.groupValues[4]}"
            else match.value
        }
        out = jsonBareValue.replace(out) { match ->
            val key = match.groupValues[2]
            if (isSensitiveKey(key)) "${match.groupValues[1]}$REDACTED"
            else match.value
        }
        out = formUrlEncoded.replace(out) { match ->
            val key = match.groupValues[2]
            if (isSensitiveKey(key)) "${match.groupValues[1]}${key}=$REDACTED"
            else match.value
        }
        out = authorizationHeader.replace(out) { "${it.groupValues[1]}$REDACTED" }
        out = bearer.replace(out) { "${it.groupValues[1]}$REDACTED" }
        return out
    }

    /**
     * OkHttp BODY logs a multipart body in a single call, with the part value
     * on the lines after Content-Disposition — not as separate logger.log lines.
     */
    internal fun redactMultipartParts(message: String): String {
        if (!message.contains("Content-Disposition", ignoreCase = true)) return message
        val output = StringBuilder()
        var cursor = 0
        for (match in dispositionLine.findAll(message)) {
            val nameMatch = dispositionName.find(match.value) ?: continue
            val name = nameMatch.groupValues[1].ifEmpty {
                nameMatch.groupValues[2].ifEmpty { nameMatch.groupValues[3] }
            }
            if (!isSensitiveKey(name)) continue
            val valueStart = findMultipartValueStart(message, match.range.last + 1) ?: continue
            if (valueStart < cursor) continue
            val valueEnd = findMultipartValueEnd(message, valueStart)
            output.append(message, cursor, valueStart)
            output.append(REDACTED)
            cursor = valueEnd
        }
        output.append(message, cursor, message.length)
        return output.toString()
    }

    /** Index of the first char of the part body (after the blank line that ends headers). */
    private fun findMultipartValueStart(message: String, from: Int): Int? {
        var i = from
        val len = message.length
        while (i < len) {
            i = when {
                message.startsWith("\r\n", i) -> i + 2
                message[i] == '\n' -> i + 1
                else -> return null
            }
            if (i >= len) return i
            if (message.startsWith("\r\n", i) || message[i] == '\n') {
                return if (message.startsWith("\r\n", i)) i + 2 else i + 1
            }
            if (message.regionMatches(i, "Content-", 0, 8, ignoreCase = true)) {
                val nl = indexOfNewline(message, i) ?: return null
                i = nl
                continue
            }
            return null
        }
        return null
    }

    private fun findMultipartValueEnd(message: String, valueStart: Int): Int {
        val crlf = message.indexOf("\r\n--", valueStart)
        val lf = message.indexOf("\n--", valueStart)
        val boundary = when {
            crlf >= 0 && (lf < 0 || crlf <= lf) -> crlf
            lf >= 0 -> lf
            else -> -1
        }
        return if (boundary >= 0) boundary else message.length
    }

    private fun indexOfNewline(message: String, from: Int): Int? {
        val i = message.indexOf('\n', from)
        return if (i >= 0) i else null
    }

    /**
     * Stateful helper for multipart bodies where the secret value is logged on the line
     * after Content-Disposition. Also safe when OkHttp emits the whole body in one call.
     */
    class MultipartAwareLogger(
        private val emit: (String) -> Unit,
    ) {
        private var redactNextValueLine = false

        fun log(message: String) {
            if (redactNextValueLine) {
                val trimmed = message.trim()
                if (trimmed.isEmpty() || trimmed.startsWith("--") || trimmed.startsWith("Content-")) {
                    emit(redact(message))
                    return
                }
                redactNextValueLine = false
                emit(REDACTED)
                return
            }
            val redacted = redact(message)
            if (hasSensitiveDisposition(message) && !partValueAlreadyPresent(message)) {
                redactNextValueLine = true
            }
            emit(redacted)
        }

        private fun hasSensitiveDisposition(message: String): Boolean {
            val disp = dispositionLine.find(message) ?: return false
            val nameMatch = dispositionName.find(disp.value) ?: return false
            val name = nameMatch.groupValues[1].ifEmpty {
                nameMatch.groupValues[2].ifEmpty { nameMatch.groupValues[3] }
            }
            return isSensitiveKey(name)
        }

        private fun partValueAlreadyPresent(message: String): Boolean =
            message.contains("\n\n") || message.contains("\r\n\r\n")
    }
}
