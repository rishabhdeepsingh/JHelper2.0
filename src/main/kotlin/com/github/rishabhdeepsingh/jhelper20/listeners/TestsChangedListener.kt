package com.github.rishabhdeepsingh.jhelper20.listeners

import com.github.rishabhdeepsingh.jhelper20.task.Test

interface TestsChangedListener {
    fun testsChanged(tests: List<Test>)
}
