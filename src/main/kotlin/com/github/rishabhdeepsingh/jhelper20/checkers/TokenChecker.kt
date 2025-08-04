package com.github.rishabhdeepsingh.jhelper20.checkers

import com.github.rishabhdeepsingh.jhelper20.tester.StringInputStream
import com.github.rishabhdeepsingh.jhelper20.tester.VerdictType
import com.github.rishabhdeepsingh.jhelper20.utils.InputReader
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class TokenChecker(parameters: String) : Checker {
    private var certainty = 0.0
    private var allowAbsolute = false
    private var allowRelative = false

    init {
        if (parameters.isNotEmpty()) {
            val tokens = parameters.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
            if (tokens.size == 1) {
                certainty = tokens[0].toDouble()
                allowRelative = true
                allowAbsolute = true
            } else {
                if (tokens[0].lowercase(Locale.getDefault()).indexOf('a') != -1) {
                    allowAbsolute = true
                }
                if (tokens[0].lowercase(Locale.getDefault()).indexOf('r') != -1) {
                    allowRelative = true
                }
                certainty = tokens[1].toDouble()
            }
        }
    }

    override fun check(input: String?, expectedOutput: String?, actualOutput: String?): VerdictType? {
        if (expectedOutput == null || actualOutput == null) {
            return VerdictType.UNDECIDED
        }
        val expected = InputReader(StringInputStream(expectedOutput))
        val actual = InputReader(StringInputStream(actualOutput))
        var count = -1
        var maxDelta = 0.0
        while (true) {
            count++
            if (expected.isExhausted()) {
                if (actual.isExhausted()) {
                    if (allowRelative || allowAbsolute) {
                        return VerdictType.OK
                    }
                    return VerdictType.OK
                }
                return VerdictType.PE
            }
            if (actual.isExhausted()) {
                return VerdictType.PE
            }
            val expectedToken: String = expected.readToken()
            val actualToken: String = actual.readToken()
            if (expectedToken != actualToken) {
                if (allowAbsolute || allowRelative) {
                    try {
                        val expectedValue = expectedToken.toDouble()
                        val actualValue = actualToken.toDouble()
                        val absoluteDiff = abs(expectedValue - actualValue)
                        val relativeDiff =
                            absoluteDiff / (if (expectedValue == 0.0 && absoluteDiff == 0.0) 1.0 else abs(expectedValue))
                        var diff = Double.Companion.POSITIVE_INFINITY
                        if (allowAbsolute) {
                            diff = min(diff, absoluteDiff)
                        }
                        if (allowRelative) {
                            diff = min(diff, relativeDiff)
                        }
                        maxDelta = max(maxDelta, diff)
                        if (diff <= certainty) {
                            continue
                        }
                    } catch (_: NumberFormatException) {
                    }
                }
                return VerdictType.WA
            }
        }
    }
}