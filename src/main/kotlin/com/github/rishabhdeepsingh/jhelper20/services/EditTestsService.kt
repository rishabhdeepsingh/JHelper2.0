package com.github.rishabhdeepsingh.jhelper20.services

import com.github.rishabhdeepsingh.jhelper20.configuration.TaskConfiguration
import com.github.rishabhdeepsingh.jhelper20.listeners.TestsChangedListener
import com.github.rishabhdeepsingh.jhelper20.task.Test
import com.intellij.execution.impl.RunManagerImpl
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.util.messages.Topic
import com.intellij.ide.SaveAndSyncHandler
import java.util.Collections

@Service(Service.Level.PROJECT)
class EditTestsService(val project: Project) {

    companion object {
        val TOPIC: Topic<TestsChangedListener> = Topic.create("JHelper Tests Changed", TestsChangedListener::class.java)
    }

    private val _tests: MutableList<Test> = mutableListOf()
    val tests: List<Test> get() = Collections.unmodifiableList(_tests)

    // Replace the whole list (e.g., when user selects a configuration)
    fun setTests(newTests: List<Test>) {
        _tests.clear()
        _tests.addAll(newTests)
        persistIntoSelectedTaskConfiguration()
        notifyTestsChanged()
    }

    // Update input/output for a single test (called by UI)
    fun updateTestIO(index: Int, newInput: String, newOutput: String) {
        if (index !in _tests.indices) return
        val t = _tests[index]
        _tests[index] = Test(newInput, newOutput, t.index, t.active)
        persistIntoSelectedTaskConfiguration()
        notifyTestsChanged()
    }

    // Toggle active flag (called by UI)
    fun setActive(index: Int, active: Boolean) {
        if (index !in _tests.indices) return
        val t = _tests[index]
        _tests[index] = Test(t.input, t.output, t.index, active)
        persistIntoSelectedTaskConfiguration()
        notifyTestsChanged()
    }

    // New: mark all tests active/inactive in one go
    fun toggleAll() {
        val allActive = _tests.all { it.active }
        val active = !allActive
        for (i in _tests.indices) {
            val t = _tests[i]
            if (t.active != active) {
                setActive(i, active)
            }
        }
    }


    private fun persistIntoSelectedTaskConfiguration() {
        val runManager = RunManagerImpl.getInstanceImpl(project)
        val settings = runManager.selectedConfiguration ?: return
        val cfg = settings.configuration as? TaskConfiguration ?: return

        // Copy values to avoid accidental external mutation
        cfg.tests = _tests.map { Test(it.input, it.output, it.index, it.active) }

        // Ask the IDE to save the project (persists workspace.xml with run configurations)
        SaveAndSyncHandler.getInstance().scheduleProjectSave(project)
    }

    private fun notifyTestsChanged() {
        ApplicationManager.getApplication().invokeLater({
            project.messageBus.syncPublisher(TOPIC).testsChanged(tests)
        }, ModalityState.any())
    }
}