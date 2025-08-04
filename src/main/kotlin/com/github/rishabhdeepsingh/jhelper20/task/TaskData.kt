package com.github.rishabhdeepsingh.jhelper20.task

import com.github.rishabhdeepsingh.jhelper20.components.ProjectConfigurationState

data class TaskData(
    val name: String,
    val className: String,
    val cppPath: String,
    val input: StreamConfiguration,
    val output: StreamConfiguration,
    val testType: TestType,
    val tests: List<Test>,
) {

    companion object {
        fun emptyTaskData() = TaskData(
            "",
            "", String.format(defaultCppPathFormat(), ""),
            StreamConfiguration.STANDARD,
            StreamConfiguration.STANDARD,
            TestType.SINGLE, listOf()
        )

        fun defaultCppPathFormat() = "${ProjectConfigurationState.getInstance()!!.state.tasksDirectory}/%s.cpp"
    }
}