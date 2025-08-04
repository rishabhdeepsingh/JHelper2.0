package com.github.rishabhdeepsingh.jhelper20.configuration

import com.intellij.execution.ExecutionTarget
import com.intellij.execution.configurations.RunConfiguration
import javax.swing.Icon

class TaskConfigurationExecutionTarget internal constructor(val originalTarget: ExecutionTarget) : ExecutionTarget() {
    override fun getId(): String {
        return "com.github.rishabhdeepsingh.jhelper20.configuration.TaskConfigurationExecutionTarget" + originalTarget.id
    }

    override fun getDisplayName(): String {
        return originalTarget.displayName
    }

    override fun getIcon(): Icon? {
        return originalTarget.icon
    }

    override fun canRun(runConfiguration: RunConfiguration): Boolean {
        return runConfiguration is TaskConfiguration
    }
}