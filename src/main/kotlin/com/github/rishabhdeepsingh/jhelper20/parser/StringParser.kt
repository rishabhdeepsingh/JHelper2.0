package com.github.rishabhdeepsingh.jhelper20.parser

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


class StringParser(private var underlying: String) : CharSequence {

    override val length: Int
        get() = underlying.length

    override fun get(index: Int): Char {
        return underlying[index]
    }

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
        return underlying.subSequence(startIndex, endIndex)
    }

    @Throws(ParseException::class)
    fun advance(toEnd: Boolean, vararg samples: String): String {
        var position = -1
        var targetSample: String? = null
        for (sample in samples) {
            val candidate = underlying.indexOf(sample)
            if (position == -1 || candidate != -1 && candidate < position) {
                position = candidate
                targetSample = sample
            }
        }
        if (position == -1) {
            throw ParseException(underlying, -1)
        }
        val result = underlying.substring(0, position)
        underlying = if (toEnd) {
            underlying.substring(position + targetSample!!.length)
        } else {
            underlying.substring(position)
        }
        return result
    }

    @Throws(ParseException::class)
    fun advanceRegex(toEnd: Boolean, vararg samples: String): String {
        var position = -1
        var targetSampleLength = 0
        for (sample in samples) {
            var candidate = -1
            val ptr: Pattern = Pattern.compile(sample)
            val mtc: Matcher = ptr.matcher(underlying)
            var lastIndex = candidate
            if (mtc.find()) {
                candidate = mtc.start()
                lastIndex = mtc.end()
            }
            if (position == -1 || candidate != -1 && candidate < position) {
                position = candidate
                targetSampleLength = lastIndex - candidate
            }
        }
        if (position == -1) throw ParseException(underlying, -1)
        val result = underlying.substring(0, position)
        underlying = if (toEnd) underlying.substring(position + targetSampleLength)
        else underlying.substring(position)
        return result
    }

    fun advanceIfPossible(toEnd: Boolean, vararg samples: String): String? {
        return try {
            advance(toEnd, *samples)
        } catch (e: ParseException) {
            null
        }
    }

    fun advanceRegexIfPossible(toEnd: Boolean, vararg samples: String): String? {
        return try {
            advanceRegex(toEnd, *samples)
        } catch (_: ParseException) {
            null
        }
    }

    @Throws(ParseException::class)
    fun dropTail(sample: String) {
        val position = underlying.indexOf(sample)
        if (position == -1) {
            throw ParseException(underlying, -1)
        }
        underlying = underlying.substring(0, position)
    }

    fun advance(offset: Int) {
        underlying = underlying.substring(offset)
    }

    fun startsWith(s: String): Boolean {
        return underlying.startsWith(s)
    }
}