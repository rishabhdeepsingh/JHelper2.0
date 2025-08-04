package com.github.rishabhdeepsingh.jhelper20.components

import com.github.rishabhdeepsingh.jhelper20.ui.Notificator
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class ProjectOpenStartupActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        ApplicationManager.getApplication().getService(ChromeParserService::class.java).startHttpServer()

//        ApplicationManager.getApplication().getService(AutoSwitcherService::class.java).startAutoSwitcher()

        ApplicationManager.getApplication().invokeLater {
            Notificator.info("JHelper2.0", "Project open startup activity ran!")
        }
    }
}