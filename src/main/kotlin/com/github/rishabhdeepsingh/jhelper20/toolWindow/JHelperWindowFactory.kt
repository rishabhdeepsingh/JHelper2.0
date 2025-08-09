package com.github.rishabhdeepsingh.jhelper20.toolWindow

import com.github.rishabhdeepsingh.jhelper20.services.CopySourceService
import com.github.rishabhdeepsingh.jhelper20.services.DeleteTaskService
import com.github.rishabhdeepsingh.jhelper20.services.EditTestsService
import com.github.rishabhdeepsingh.jhelper20.task.Test
import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.Separator
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JComponent

class JHelperWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val jHelperToolWindow = JHelperToolWindow(toolWindow)

        val content = ContentFactory.getInstance().createContent(jHelperToolWindow.getContent(), "JHelper", false)

        // Dispose inner resources (TestsPanel) with the content
        content.setDisposer(jHelperToolWindow)
        toolWindow.contentManager.addContent(content)

        // Seed initial tests so the list is populated immediately
        val service = project.service<EditTestsService>()
        jHelperToolWindow.setInitialTests(service.tests)
    }

    override fun shouldBeAvailable(project: Project) = true

    class JHelperToolWindow(toolWindow: ToolWindow) : Disposable {

        private val copySourceService = toolWindow.project.service<CopySourceService>()
        private val deleteTaskService = toolWindow.project.service<DeleteTaskService>()
        private val editTestsService = toolWindow.project.service<EditTestsService>()

        // Single TestsPanel instance used inside the main tab
        private val testsPanel = TestsPanel(toolWindow.project)

        fun getContent(): JComponent {
            val toolbar = ActionManager.getInstance().createActionToolbar(
                "JHelper.Toolbar", DefaultActionGroup(
                    AddTestAction {
                        val index = editTestsService.addTest("", "", true)
                        // Try to select the created test after the model updates
                        javax.swing.SwingUtilities.invokeLater {
                            testsPanel.selectIndex(index)
                        }
                    },
                    DeleteSelectedTestAction { testsPanel.deleteSelectedTest() },
                    Separator.getInstance(),
                    CopyAction { copySourceService.copySource() },
                    Separator.getInstance(),
                    ToggleAllTestsAction { editTestsService.toggleAll() },
                    Separator.getInstance(),
                    DeleteAction { deleteTaskService.deleteTask() },
                ), true // horizontal
            )

            // Center content: Tests list
            val testsContent = LabeledComponent.create(
                JBScrollPane(testsPanel), "Tests"
            )
            val root = JBPanel<JBPanel<*>>(BorderLayout()).apply {
                add(toolbar.component, BorderLayout.NORTH)
                add(testsContent, BorderLayout.CENTER)
                preferredSize = Dimension(600, 400)
            }
            toolbar.targetComponent = root

            Disposer.register(this, testsPanel)
            return root
        }

        fun setInitialTests(tests: List<Test>) {
            testsPanel.setInitialTests(tests)
        }

        override fun dispose() {
            // testsPanel is disposed via Disposer.register
        }
    }
}

private class AddTestAction(private val onAdd: () -> Unit) :
    DumbAwareAction("Add Testcase", "Add new test case", AllIcons.General.Add) {
    override fun actionPerformed(e: AnActionEvent) = onAdd()
}

private class CopyAction(private val onCopy: () -> Unit) :
    DumbAwareAction("Copy Sources", "Copy sources", AllIcons.Actions.Copy) {
    override fun actionPerformed(e: AnActionEvent) = onCopy()
}

private class DeleteSelectedTestAction(private val onDeleteSelected: () -> Unit) :
    DumbAwareAction("Delete Testcase", "Delete selected test case", AllIcons.General.Remove) {
    override fun actionPerformed(e: AnActionEvent) = onDeleteSelected()
}

private val DELETE_TASK_ICON = IconLoader.getIcon("/icons/delete.png", JHelperWindowFactory::class.java)

private class DeleteAction(private val onDelete: () -> Unit) :
    DumbAwareAction("Delete Task", "Delete task", DELETE_TASK_ICON) {
    override fun actionPerformed(e: AnActionEvent) = onDelete()
}

private class ToggleAllTestsAction(private val onSelectAll: () -> Unit) :
    DumbAwareAction("Toggle All", "Toggle all tests", AllIcons.General.TreeSelected) {
    override fun actionPerformed(e: AnActionEvent) = onSelectAll()
}
