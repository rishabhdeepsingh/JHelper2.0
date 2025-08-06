package com.github.rishabhdeepsingh.jhelper20.configuration

import com.intellij.execution.ExecutionTarget
import com.intellij.execution.ExecutionTargetManager
import com.intellij.execution.ExecutionTargetProvider
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.project.Project

class TaskConfigurationTargetProvider : ExecutionTargetProvider() {
    override fun getTargets(
        project: Project,
        configuration: RunConfiguration,
    ): List<ExecutionTarget> {
        if (configuration !is TaskConfiguration) {
            return emptyList()
        }
        val testRunner = TaskRunner.getRunnerSettings(project) ?: return emptyList()
        return ExecutionTargetManager.getInstance(project).getTargetsFor(testRunner.configuration)
            .mapNotNull { target ->
                TaskConfigurationExecutionTarget(target)
            }
    }

}
