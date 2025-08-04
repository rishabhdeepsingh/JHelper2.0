package com.github.rishabhdeepsingh.jhelper20.checkers

import com.github.rishabhdeepsingh.jhelper20.tester.VerdictType

interface Checker {
    fun check(input: String?, expectedOutput: String?, actualOutput: String?): VerdictType?
}