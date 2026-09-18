package com.nhonghuafan.docgen

import android.content.Context
import java.io.*
import java.util.zip.*

/** Local DOCX generator. Uses real Word OOXML and keeps a small metadata part
 * inside the DOCX so documents created by this app can be imported back later. */
object DocxTemplateEngine {
    private fun esc(s: String): String = s
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")

    private fun xmlText(s: String): String = esc(s)
        .replace("\r\n", "\n")
        .replace("\r", "\n")
        .replace("\n", "<w:br/>")

    private fun jsonEsc(s: String): String = buildString {
        for (c in s) when (c) {
            '\\' -> append("\\\\")
            '"' -> append("\\\"")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            else -> append(c)
        }
    }

    private fun metadataJson(type: String, values: Map<String, String>): String {
        val body = values.entries.joinToString(",\n") { "    \"${jsonEsc(it.key)}\": \"${jsonEsc(it.value)}\"" }
        return "{\n  \"app\": \"ระบบหนังสือราชการ\",\n  \"version\": 4,\n  \"type\": \"${jsonEsc(type)}\",\n  \"values\": {\n$body\n  }\n}\n"
    }

    fun create(context: Context, assetPath: String, output: File, type: String, values: Map<String, String>) {
        context.assets.open(assetPath).use { input ->
            ZipInputStream(BufferedInputStream(input)).use { zin ->
                ZipOutputStream(BufferedOutputStream(FileOutputStream(output))).use { zos ->
                    val buf = ByteArray(8192)
                    val metadataName = "docgen/metadata.json"
                    while (true) {
                        val e = zin.nextEntry ?: break
                        if (e.name == metadataName) continue
                        val data = ByteArrayOutputStream()
                        var n: Int
                        while (zin.read(buf).also { n = it } > 0) data.write(buf, 0, n)
                        var bytes = data.toByteArray()
                        if (e.name == "word/document.xml") {
                            var xml = bytes.toString(Charsets.UTF_8)
                            for ((key, value) in values) xml = xml.replace("{{$key}}", xmlText(value))
                            bytes = xml.toByteArray(Charsets.UTF_8)
                        }
                        val ne = ZipEntry(e.name)
                        ne.method = ZipEntry.DEFLATED
                        zos.putNextEntry(ne)
                        zos.write(bytes)
                        zos.closeEntry()
                    }
                    val meta = ZipEntry(metadataName)
                    meta.method = ZipEntry.DEFLATED
                    zos.putNextEntry(meta)
                    zos.write(metadataJson(type, values).toByteArray(Charsets.UTF_8))
                    zos.closeEntry()
                }
            }
        }
    }

    data class Imported(val type: String, val values: Map<String, String>)

    fun importMetadata(input: InputStream): Imported? {
        ZipInputStream(BufferedInputStream(input)).use { zin ->
            while (true) {
                val e = zin.nextEntry ?: break
                if (e.name != "docgen/metadata.json") continue
                val text = readAll(zin).toString(Charsets.UTF_8)
                val type = Regex("\\\"type\\\"\\s*:\\s*\\\"(.*?)\\\"").find(text)?.groupValues?.get(1) ?: return null
                val section = Regex("\\\"values\\\"\\s*:\\s*\\{(.*)\\n\\s*\\}", RegexOption.DOT_MATCHES_ALL).find(text)?.groupValues?.get(1) ?: ""
                val map = linkedMapOf<String, String>()
                val pair = Regex("\\\"((?:\\\\.|[^\\\"])*)\\\"\\s*:\\s*\\\"((?:\\\\.|[^\\\"])*)\\\"")
                pair.findAll(section).forEach { m -> map[unescapeJson(m.groupValues[1])] = unescapeJson(m.groupValues[2]) }
                return Imported(unescapeJson(type), map)
            }
        }
        return null
    }

    private fun unescapeJson(s: String): String = buildString {
        var i = 0
        while (i < s.length) {
            if (s[i] == '\\' && i + 1 < s.length) {
                when (s[i + 1]) {
                    'n' -> append('\n')
                    'r' -> append('\r')
                    't' -> append('\t')
                    '\\' -> append('\\')
                    '"' -> append('"')
                    else -> append(s[i + 1])
                }
                i += 2
            } else { append(s[i]); i++ }
        }
    }

    private fun readAll(input: InputStream): ByteArray {
        val out = ByteArrayOutputStream()
        val buf = ByteArray(8192)
        var n: Int
        while (input.read(buf).also { n = it } > 0) out.write(buf, 0, n)
        return out.toByteArray()
    }
}
