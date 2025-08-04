package com.github.rishabhdeepsingh.jhelper20.task

enum class StreamType(val uiDescription: String, val hasStringParameter: Boolean) {
    STANDARD("Standard stream", false),
    TASK_ID("Name.in/.out", false),
    CUSTOM("Custom filename", true),
    LOCAL_REGEXP("Local regular expression", true);

    override fun toString(): String {
        return uiDescription
    }
}