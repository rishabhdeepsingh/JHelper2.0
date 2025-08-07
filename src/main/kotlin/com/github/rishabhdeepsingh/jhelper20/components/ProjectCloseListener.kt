package com.github.rishabhdeepsingh.jhelper20.components

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener

class ProjectCloseListener : ProjectManagerListener {

    override fun projectClosed(project: Project) {
        ApplicationManager.getApplication().getService(ChromeParserService::class.java).stopServer()
    }
}