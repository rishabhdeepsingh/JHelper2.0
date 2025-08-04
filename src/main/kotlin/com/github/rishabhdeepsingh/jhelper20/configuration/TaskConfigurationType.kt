package com.github.rishabhdeepsingh.jhelper20.configuration

import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.SimpleConfigurationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.openapi.util.IconLoader.getIcon


class TaskConfigurationType : SimpleConfigurationType(
    "com.github.rishabhdeepsingh.jhelper20.configuration.TaskConfigurationType",
    "Task",
    "Task for JHelper",
    NotNullLazyValue.createConstantValue(
        getIcon(
            "/icons/task.png", TaskConfigurationType::class.java
        )
    )
) {
    override fun createTemplateConfiguration(project: Project): RunConfiguration {
        return TaskConfiguration(project, this)
    }
}