package com.ktvme.mq.util

import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets.UTF_8
import java.util.*
import java.util.zip.GZIPOutputStream

/**
 *  @author fanwu 编写于 2018/1/23.
 */
object SecurityUtil {




    fun zip(content: String): ByteArray {
        val bos = ByteArrayOutputStream()
        GZIPOutputStream(bos).bufferedWriter(UTF_8).use { it.write(content) }
        return bos.toByteArray()
    }

//    fun unzip(content: ByteArray): String =
//            GZIPInputStream(content.inputStream()).bufferedReader(UTF_8).use { it.readText() }

//    fun String.debase64(): ByteArray = Base64.getDecoder().decode(this)

    fun ByteArray.base64(): String = Base64.getEncoder().encodeToString(this)
}