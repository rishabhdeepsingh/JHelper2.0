package com.github.rishabhdeepsingh.jhelper20.listeners

import com.github.rishabhdeepsingh.jhelper20.configuration.TaskConfiguration
import com.github.rishabhdeepsingh.jhelper20.services.EditTestsService
import com.intellij.execution.RunManagerListener
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

class RunConfigurationChangeListener(val project: Project) : RunManagerListener {
    override fun runConfigurationSelected(settings: RunnerAndConfigurationSettings?) {
        val cfg = settings?.configuration as? TaskConfiguration ?: return
        val editTestsService = project.service<EditTestsService>()
        editTestsService.setTests(cfg.tests)
    }
}