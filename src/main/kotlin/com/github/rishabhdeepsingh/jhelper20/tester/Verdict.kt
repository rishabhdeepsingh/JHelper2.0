package com.github.rishabhdeepsingh.jhelper20.tester


enum class VerdictType(private val description: String) {
    UNDECIDED("Undecided"),
    OK("OK"),
    WA("Wrong Answer"),
    PE("Presentation Error"),
    RTE("RunTime Error"),
    SKIPPED("Skipped");

    override fun toString(): String {
        return description
    }
}