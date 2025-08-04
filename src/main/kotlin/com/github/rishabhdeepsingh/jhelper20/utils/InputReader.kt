package com.github.rishabhdeepsingh.jhelper20.utils

import java.io.IOException
import java.io.InputStream
import java.io.UnsupportedEncodingException
import java.util.InputMismatchException
import kotlin.math.pow


class InputReader(private val stream: InputStream) : InputStream() {

    private val buf = ByteArray(1024)
    private var curChar = 0
    private var numChars = 0

    override fun read(): Int {
        if (numChars == -1) {
            throw InputMismatchException()
        }
        if (curChar >= numChars) {
            curChar = 0
            try {
                numChars = stream.read(buf)
            } catch (e: IOException) {
                throw InputMismatchException()
            }
            if (numChars <= 0) {
                return -1
            }
        }
        return buf[curChar++].toInt()
    }

    fun peek(): Int {
        if (numChars == -1) {
            return -1
        }
        if (curChar >= numChars) {
            curChar = 0
            try {
                numChars = stream.read(buf)
            } catch (e: IOException) {
                return -1
            }
            if (numChars <= 0) {
                return -1
            }
        }
        return buf[curChar].toInt()
    }

    fun readInt(): Int {
        var c = read()
        while (isSpaceChar(c)) c = read()
        var sgn = 1
        if (c == '-'.code) {
            sgn = -1
            c = read()
        }
        var res = 0
        do {
            if (c < '0'.code || c > '9'.code) {
                throw InputMismatchException()
            }
            res *= 10
            res += c - '0'.code
            c = read()
        } while (!isSpaceChar(c))
        return res * sgn
    }

    fun readLong(): Long {
        var c = read()
        while (isSpaceChar(c)) c = read()
        var sgn = 1
        if (c == '-'.code) {
            sgn = -1
            c = read()
        }
        var res: Long = 0
        do {
            if (c < '0'.code || c > '9'.code) {
                throw InputMismatchException()
            }
            res *= 10
            res += (c - '0'.code).toLong()
            c = read()
        } while (!isSpaceChar(c))
        return res * sgn
    }

    fun readString(): String? {
        val length = readInt()
        if (length < 0) {
            return null
        }
        val bytes = ByteArray(length)
        for (i in 0..<length) bytes[i] = read().toByte()
        try {
            return String(bytes, charset("UTF-8"))
        } catch (e: UnsupportedEncodingException) {
            return String(bytes)
        }
    }

    fun readToken(): String {
        var c: Int
        while (isSpaceChar(read().also { c = it }));
        val result = StringBuilder()
        result.appendCodePoint(c)
        while (!isSpaceChar(read().also { c = it })) result.appendCodePoint(c)
        return result.toString()
    }

    fun readCharacter(): Char {
        var c = read()
        while (isSpaceChar(c)) c = read()
        return c.toChar()
    }

    fun readDouble(): Double {
        var c = read()
        while (isSpaceChar(c)) c = read()
        var sgn = 1
        if (c == '-'.code) {
            sgn = -1
            c = read()
        }
        var res = 0.0
        while (!isSpaceChar(c) && c != '.'.code) {
            if (c == 'e'.code || c == 'E'.code) {
                return res * 10.0.pow(readInt().toDouble())
            }
            if (c < '0'.code || c > '9'.code) {
                throw InputMismatchException()
            }
            res *= 10.0
            res += (c - '0'.code).toDouble()
            c = read()
        }
        if (c == '.'.code) {
            c = read()
            var m = 1.0
            while (!isSpaceChar(c)) {
                if (c == 'e'.code || c == 'E'.code) {
                    return res * 10.0.pow(readInt().toDouble())
                }
                if (c < '0'.code || c > '9'.code) {
                    throw InputMismatchException()
                }
                m /= 10.0
                res += (c - '0'.code) * m
                c = read()
            }
        }
        return res * sgn
    }

    fun isExhausted(): Boolean {
        var value: Int
        while (isSpaceChar(peek().also { value = it }) && value != -1) read()
        return value == -1
    }

    fun readBoolean(): Boolean {
        return readInt() == 1
    }

    fun <E : Enum<E?>?> readEnum(c: Class<E?>): E? {
        val name = readString()
        if (name == null) {
            return null
        }
        for (e in c.getEnumConstants()) {
            if (e!!.name == name) {
                return e
            }
        }
        throw EnumConstantNotPresentException(c, name)
    }

    fun readTopCoder(): Any? {
        val type = readToken()
        if (type == "-1") {
            return null
        }
        if ("int" == type) {
            return readInt()
        } else if ("long" == type) {
            return readLong()
        } else if ("double" == type) {
            return readDouble()
        } else if ("String" == type) {
            return readString()
        } else if ("int[]" == type) {
            val length = readInt()
            val result = IntArray(length)
            for (i in 0..<length) result[i] = readInt()
            return result
        } else if ("long[]" == type) {
            val length = readInt()
            val result = LongArray(length)
            for (i in 0..<length) result[i] = readLong()
            return result
        } else if ("double[]" == type) {
            val length = readInt()
            val result = DoubleArray(length)
            for (i in 0..<length) result[i] = readDouble()
            return result
        } else if ("String[]" == type) {
            val length = readInt()
            val result = arrayOfNulls<String>(length)
            for (i in 0..<length) result[i] = readString()
            return result
        }
        throw InputMismatchException()
    }

    companion object {
        fun isSpaceChar(c: Int): Boolean {
            return c == ' '.code || c == '\n'.code || c == '\r'.code || c == '\t'.code || c == -1
        }
    }
}