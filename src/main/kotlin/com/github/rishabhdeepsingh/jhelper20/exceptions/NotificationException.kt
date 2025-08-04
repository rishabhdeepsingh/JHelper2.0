package com.github.rishabhdeepsingh.jhelper20.exceptions

/**
 * An exception representing an error that should be shown to end user
 */
class NotificationException : RuntimeException {
    val title: String
    val content: String

    @JvmOverloads
    constructor(content: String, cause: Throwable? = null) : super(content, cause) {
        title = ""
        this.content = content
    }

    @JvmOverloads
    constructor(title: String, content: String, cause: Throwable? = null) : super("$title: $content", cause) {
        this.title = title
        this.content = content
    }
}