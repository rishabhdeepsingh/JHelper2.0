package com.github.rishabhdeepsingh.jhelper20.common

import com.github.rishabhdeepsingh.jhelper20.configuration.TaskConfiguration
import com.github.rishabhdeepsingh.jhelper20.exceptions.NotificationException
import com.intellij.execution.ExecutionTarget
import com.intellij.execution.ExecutionTargetManager
import com.intellij.execution.RunManager
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets


object CommonUtils {

    @JvmStatic
    fun getStringFromInputStream(stream: InputStream): String {
        BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8)).use { reader ->
            val sb = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                sb.append(line).append('\n')
            }
            return sb.toString()
        }
    }

    @JvmStatic
    fun generatePSIFromTask(project: Project, taskConfiguration: TaskConfiguration): PsiFile {
        val pathToClassFile = taskConfiguration.cppPath
        val virtualFile = project.firstRootSource().findFileByRelativePath(pathToClassFile)
            ?: throw NotificationException("Task file not found", "Seems your task is in inconsistent state")
        return PsiManager.getInstance(project).findFile(virtualFile)
            ?: throw NotificationException("Couldn't get PSI file for input file")
    }

    fun chooseConfigurationAndTarget(
        project: Project?,
        runConfiguration: RunnerAndConfigurationSettings?,
        target: ExecutionTarget?,
    ) {
        if (project == null || target == null) return
        RunManager.getInstance(project).selectedConfiguration = runConfiguration
        ExecutionTargetManager.getInstance(project).activeTarget = target
    }


}