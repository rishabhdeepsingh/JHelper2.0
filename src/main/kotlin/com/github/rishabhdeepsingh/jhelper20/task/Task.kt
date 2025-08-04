package com.github.rishabhdeepsingh.jhelper20.task

data class Task(
    val name: String = "",
    val tests: List<Test> = emptyList(),
    val contestName: String = "",
)