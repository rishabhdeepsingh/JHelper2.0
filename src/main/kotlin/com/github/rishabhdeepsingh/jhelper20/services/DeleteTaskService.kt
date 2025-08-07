package com.github.rishabhdeepsingh.jhelper20.services

import com.github.rishabhdeepsingh.jhelper20.common.firstRootSource
import com.github.rishabhdeepsingh.jhelper20.configuration.TaskConfiguration
import com.github.rishabhdeepsingh.jhelper20.ui.Notificator
import com.intellij.execution.RunManagerEx
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import java.io.IOException


@Service(Service.Level.PROJECT)
class DeleteTaskService(val project: Project) {

    fun deleteTask() {
        val runManager = RunManagerEx.getInstanceEx(project)
        val selectedConfiguration = runManager.selectedConfiguration ?: return
        val configuration = selectedConfiguration.configuration
        if (configuration is TaskConfiguration) {
            removeFiles(project, configuration)
            runManager.removeConfiguration(selectedConfiguration)
            selectSomeTaskConfiguration(runManager)
        } else {
            Notificator.showNotification(
                "Not a JHelper configuration",
                "To delete a configuration you should choose it first",
                NotificationType.WARNING
            )
        }
    }

    private fun removeFiles(project: Project, taskConfiguration: TaskConfiguration) {
        val cppPath = taskConfiguration.cppPath

        ApplicationManager.getApplication().runWriteAction(
            object : Runnable {
                override fun run() {
                    val classFile = project.firstRootSource().findFileByRelativePath(cppPath) ?: return
                    try {
                        classFile.delete(this)
                    } catch (ignored: IOException) {
                        Notificator.showNotification("Couldn't delete class file", NotificationType.WARNING)
                    }
                }
            }
        )
    }

    companion object {
        private fun selectSomeTaskConfiguration(runManager: RunManagerEx) {
            for (settings in runManager.allSettings) {
                if (settings.configuration is TaskConfiguration) {
                    runManager.selectedConfiguration = settings
                    return
                }
            }
        }
    }

}