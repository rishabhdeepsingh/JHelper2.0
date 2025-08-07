package com.github.rishabhdeepsingh.jhelper20.startup

import com.github.rishabhdeepsingh.jhelper20.services.ChromeParserService
import com.github.rishabhdeepsingh.jhelper20.ui.Notificator
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class ProjectOpenStartupActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        ApplicationManager.getApplication().getService(ChromeParserService::class.java).startHttpServer()

//        TODO: Add support for auto switch on file change or task change. Two way binding

        ApplicationManager.getApplication().invokeLater {
            Notificator.info("JHelper2.0", "Project open startup activity ran!")
        }
    }
}