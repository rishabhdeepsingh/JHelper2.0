package com.github.rishabhdeepsingh.jhelper20.states

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "com.github.rishabhdeepsingh.jhelper20.components.ProjectConfigurationState",
    storages = [Storage("JHelper.xml")]
)
class ProjectConfigurationState : PersistentStateComponent<ProjectConfigurationState> {
    val tasksDirectory = "tasks"
    var outputFile = "output/main.cpp"
    var runFile = "testrunner/main.cpp"
    var isCodeReformattingOn = false

    override fun getState(): ProjectConfigurationState {
        return this
    }

    override fun loadState(state: ProjectConfigurationState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        fun getInstance(): ProjectConfigurationState =
            ApplicationManager.getApplication().getService(ProjectConfigurationState::class.java)
    }
}