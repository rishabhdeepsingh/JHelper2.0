package com.github.rishabhdeepsingh.jhelper20.tester

import java.io.IOException
import java.io.InputStream


class StringInputStream(private val s: String) : InputStream() {
    private var index = 0

    @Throws(IOException::class)
    override fun read(): Int {
        if (index < s.length) {
            return s[index++].code
        }
        return -1
    }
}