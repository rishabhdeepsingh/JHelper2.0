package com.github.rishabhdeepsingh.jhelper20.utils

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URI
import java.nio.charset.Charset

object FileUtilities {

    fun getWebPageContent(address: String?): String? {
        return getWebPageContent(address, "UTF-8")
    }

    private fun getWebPageContent(address: String?, charset: String): String? {
        if (address == null) return null
        repeat(10) {
            try {
                val url = URI.create(address).toURL()
                val input: InputStream? = url.openStream()
                val reader = BufferedReader(InputStreamReader(input, Charset.forName(charset)))
                val builder = StringBuilder()
                var s: String?
                while ((reader.readLine().also { s = it }) != null) {
                    builder.append(s).append('\n')
                }
                return String(builder.toString().toByteArray(charset("UTF-8")), charset("UTF-8"))
            } catch (_: IOException) {
            }
        }
        return null
    }

}