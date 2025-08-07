package com.github.rishabhdeepsingh.jhelper20.services

import com.github.rishabhdeepsingh.jhelper20.common.firstRootSource
import com.github.rishabhdeepsingh.jhelper20.configuration.TaskConfiguration
import com.github.rishabhdeepsingh.jhelper20.exceptions.NotificationException
import com.github.rishabhdeepsingh.jhelper20.generation.CodeGenerationUtils
import com.github.rishabhdeepsingh.jhelper20.states.ProjectConfigurationState
import com.github.rishabhdeepsingh.jhelper20.ui.Notificator
import com.intellij.execution.RunManagerEx
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.Service
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

@Service(Service.Level.PROJECT)
class CopySourceService(val project: Project) {

    fun copySource() {
        val runManager = RunManagerEx.getInstanceEx(project)
        val selectedConfiguration = runManager.selectedConfiguration ?: return

        val runConfiguration = selectedConfiguration.configuration
        if (runConfiguration !is TaskConfiguration) {
            Notificator.warn("Not a JHelper configuration", "You have to choose JHelper Task to copy")
            return
        }

        CodeGenerationUtils.generateSubmissionFileForTask(project, runConfiguration)

        val file = project.firstRootSource().findFileByRelativePath(ProjectConfigurationState.getInstance().outputFile)
            ?: throw NotificationException("Couldn't find output file")
        val document = FileDocumentManager.getInstance().getDocument(file)
            ?: throw NotificationException("Couldn't open output file")
        val selection = StringSelection(document.text)
        Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, selection)
        Notificator.showNotification("Jhelper", "Source copied to clipboard", NotificationType.INFORMATION)

    }
}