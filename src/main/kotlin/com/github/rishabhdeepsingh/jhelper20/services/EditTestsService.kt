package com.github.rishabhdeepsingh.jhelper20.services

import com.github.rishabhdeepsingh.jhelper20.task.Test
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.util.messages.Topic
import java.util.Collections


interface TestsChangedListener {
    fun testsChanged(tests: List<Test>)
}


@Service(Service.Level.PROJECT)
class EditTestsService(val project: Project) {
    // TODO(rishabhdeepsingh): save configuration

    companion object {
        val TOPIC: Topic<TestsChangedListener> = Topic.create("JHelper Tests Changed", TestsChangedListener::class.java)
    }

    private val _tests: MutableList<Test> = mutableListOf()
    val tests: List<Test> get() = Collections.unmodifiableList(_tests)

    // Replace the whole list
    fun setTests(newTests: List<Test>) {
        _tests.clear()
        _tests.addAll(newTests)
        notifyTestsChanged()
    }

    private fun notifyTestsChanged() {
        // Ensure UI subscribers update on EDT
        ApplicationManager.getApplication().invokeLater({
            project.messageBus.syncPublisher(TOPIC).testsChanged(tests)
        }, ModalityState.any())
    }
}