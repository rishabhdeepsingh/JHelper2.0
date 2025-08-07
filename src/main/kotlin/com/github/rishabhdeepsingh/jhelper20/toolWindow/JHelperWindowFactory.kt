package com.github.rishabhdeepsingh.jhelper20.toolWindow

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import com.github.rishabhdeepsingh.jhelper20.MyBundle
import com.github.rishabhdeepsingh.jhelper20.services.CopySourceService
import com.github.rishabhdeepsingh.jhelper20.services.DeleteTaskService
import javax.swing.JButton
import com.intellij.ui.JBColor


class JHelperWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val jHelperToolWindow = JHelperToolWindow(toolWindow)
        val content = ContentFactory.getInstance().createContent(jHelperToolWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true

    class JHelperToolWindow(toolWindow: ToolWindow) {

        private val copySourceService = toolWindow.project.service<CopySourceService>()
        private val deleteTaskService = toolWindow.project.service<DeleteTaskService>()


        fun getContent() = JBPanel<JBPanel<*>>().apply {
            add(JButton(MyBundle.message("COPY_SOURCES")).apply {
                background = JBColor.GREEN
                font = font.deriveFont(16.0f)
                addActionListener { copySourceService.copySource() }
            })

            add(JButton(MyBundle.message("DELETE_TASK")).apply {
                background = JBColor.RED
                addActionListener { deleteTaskService.deleteTask() }
            })
        }
    }
}
