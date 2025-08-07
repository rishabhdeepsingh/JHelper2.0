package com.github.rishabhdeepsingh.jhelper20.configuration

import com.github.rishabhdeepsingh.jhelper20.common.CommonUtils.chooseConfigurationAndTarget
import com.github.rishabhdeepsingh.jhelper20.common.CommonUtils.generatePSIFromTask
import com.github.rishabhdeepsingh.jhelper20.exceptions.NotificationException
import com.github.rishabhdeepsingh.jhelper20.generation.CodeGenerationUtils
import com.github.rishabhdeepsingh.jhelper20.generation.CodeGenerationUtils.generateSubmissionFileForTask
import com.intellij.execution.ProgramRunnerUtil
import com.intellij.execution.RunManager
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.configurations.RunnerSettings
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner
import com.intellij.openapi.project.Project

/**
 * Class for Running TaskConfiguration
 * It isn't fully compliant with [ProgramRunner] Interface because [.execute] doesn't call [RunProfile.getState]
 * as described in [IDEA DEV Confluence](http://confluence.jetbrains.com/display/IDEADEV/Run+Configurations#RunConfigurations-RunningaProcess)
 */
class TaskRunner : ProgramRunner<RunnerSettings?> {
    override fun getRunnerId(): String {
        return "com.github.rishabhdeepsingh.jhelper20.configuration.TaskRunner"
    }

    override fun canRun(executorId: String, profile: RunProfile): Boolean {
        return profile is TaskConfiguration
    }

    /**
     * Runs specified TaskConfiguration: generates code and then runs output configuration.
     *
     * @throws ClassCastException if `environment.getRunProfile()` is not [TaskConfiguration].
     * @see ExecutionEnvironment.getRunProfile
     */
    override fun execute(environment: ExecutionEnvironment) {
        val project = environment.project

        val taskConfiguration = environment.runProfile as TaskConfiguration
        generateSubmissionFileForTask(project, taskConfiguration)

        generateRunFileForTask(project, taskConfiguration)

        val testRunnerSettings: RunnerAndConfigurationSettings? = RunManager.getInstance(project).allSettings.find {
            it.name == RUN_CONFIGURATION_NAME
        }
        if (testRunnerSettings == null) {
            throw NotificationException("No run configuration found", "It should be called ($RUN_CONFIGURATION_NAME)")
        }

        val originalExecutionTarget = environment.executionTarget
        val testRunnerExecutionTarget = (originalExecutionTarget as TaskConfigurationExecutionTarget).originalTarget
        val originalSettings = environment.runnerAndConfigurationSettings

        chooseConfigurationAndTarget(project, testRunnerSettings, testRunnerExecutionTarget)
        ProgramRunnerUtil.executeConfiguration(testRunnerSettings, environment.executor)

        chooseConfigurationAndTarget(project, originalSettings, originalExecutionTarget)
    }


    companion object {
        const val RUN_CONFIGURATION_NAME = "testrunner"
    }
}

fun getRunnerSettings(project: Project): RunnerAndConfigurationSettings? {
    return getSettingsByName(project)
}

private fun getSettingsByName(project: Project): RunnerAndConfigurationSettings? =
    RunManager.getInstance(project).allSettings.find {
        it.name == TaskRunner.RUN_CONFIGURATION_NAME
    }

private fun generateRunFileForTask(project: Project, taskConfiguration: TaskConfiguration) {
    val psiFile = generatePSIFromTask(project, taskConfiguration)
    CodeGenerationUtils.generateRunFile(project, psiFile, taskConfiguration)
}