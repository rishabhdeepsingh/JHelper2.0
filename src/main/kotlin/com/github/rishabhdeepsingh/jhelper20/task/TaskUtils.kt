package com.github.rishabhdeepsingh.jhelper20.task

import com.github.rishabhdeepsingh.jhelper20.common.firstRootSource
import com.github.rishabhdeepsingh.jhelper20.configuration.TaskConfiguration
import com.github.rishabhdeepsingh.jhelper20.configuration.TaskConfigurationType
import com.github.rishabhdeepsingh.jhelper20.exceptions.NotificationException
import com.github.rishabhdeepsingh.jhelper20.generation.FileUtils
import com.github.rishabhdeepsingh.jhelper20.generation.TemplatesUtils
import com.intellij.execution.RunManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiManager

object TaskUtils {

    fun saveNewTask(taskData: TaskData, project: Project): PsiElement? {
        createConfigurationForTask(project, taskData)
        return generateCPP(project, taskData)
    }

    private fun createConfigurationForTask(project: Project, taskData: TaskData) {
        val configurationType = TaskConfigurationType()
        val factory = configurationType.configurationFactories[0]

        val manager = RunManager.getInstance(project)
        val taskConfiguration = TaskConfiguration(project, factory)
        taskConfiguration.setFromTaskData(taskData)
        val configuration = manager.createConfiguration(taskConfiguration, factory)
        manager.addConfiguration(configuration)

        manager.selectedConfiguration = configuration
    }

    private fun generateCPP(project: Project, taskData: TaskData): PsiElement? {
        val projectBaseFile =
            createVirtualFileFromBasePath(project) ?: throw NotificationException("Couldn't find root source")
        val parent =
            FileUtils.findOrCreateByRelativePath(
                projectBaseFile,
                FileUtils.getDirectory(taskData.cppPath)
            )
        println()
        val psiParent = PsiManager.getInstance(project).findDirectory(parent)
            ?: throw NotificationException("Couldn't open parent directory as PSI")

        val file = PsiFileFactory.getInstance(project).createFileFromText(
            FileUtils.getFilename(taskData.cppPath),
            PlainTextFileType.INSTANCE,
            getTaskContent(project, taskData.className)
        )
        return file.let { ApplicationManager.getApplication().runWriteAction(Computable { psiParent.add(it) }) }
            ?: throw NotificationException("Couldn't generate file")
    }

    private fun createVirtualFileFromBasePath(project: Project): VirtualFile? {
        if (project.basePath == null) return null
        return LocalFileSystem.getInstance().findFileByPath(project.basePath!!)
    }


    /**
     * Generates task file content depending on a custom user template
     */
    private fun getTaskContent(project: Project, className: String): String {
        val template = TemplatesUtils.getTemplate(project, "task")
        return TemplatesUtils.replaceAll(template, TemplatesUtils.CLASS_NAME, className)
    }
}