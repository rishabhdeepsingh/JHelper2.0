package com.github.rishabhdeepsingh.jhelper20.task

enum class TestType(private val uiDescription: String) {
    SINGLE("Single test"),
    MULTI_NUMBER("Number of tests known"),
    MULTI_EOF("Number of tests unknown");
}