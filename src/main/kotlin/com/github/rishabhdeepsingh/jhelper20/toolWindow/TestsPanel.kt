package com.github.rishabhdeepsingh.jhelper20.toolWindow

import com.github.rishabhdeepsingh.jhelper20.services.EditTestsService
import com.github.rishabhdeepsingh.jhelper20.services.TestsChangedListener
import com.github.rishabhdeepsingh.jhelper20.task.Test
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.ui.CollectionListModel
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.components.JBList
import com.intellij.openapi.util.text.StringUtil
import java.awt.BorderLayout
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.ListSelectionModel

class TestsPanel(project: Project) : JPanel(BorderLayout()), Disposable {
    private val listModel = CollectionListModel<Test>()
    private val list = JBList(listModel).apply {
        selectionMode = ListSelectionModel.SINGLE_SELECTION
        emptyText.text = "No tests"
        // Custom renderer to show readable text for every test case
        cellRenderer = object : ColoredListCellRenderer<Test>() {
            override fun customizeCellRenderer(
                list: JList<out Test>,
                value: Test?,
                index: Int,
                selected: Boolean,
                hasFocus: Boolean
            ) {
                if (value == null) return

                val number = index + 1
                val inPreview = preview(value.input)
                val outPreview = preview(value.output)

                val titleAttrs =
                    if (value.active) SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES
                    else SimpleTextAttributes.GRAYED_ATTRIBUTES

                append("Test $number", titleAttrs)

                // space before metadata
                append("  — ", SimpleTextAttributes.GRAYED_ATTRIBUTES)
                append("in: $inPreview", SimpleTextAttributes.GRAYED_ATTRIBUTES)
                append("   ")
                append("out: $outPreview", SimpleTextAttributes.GRAYED_ATTRIBUTES)
            }

            private fun preview(text: String?): String {
                val oneLine = (text ?: "").replace("\r\n", " ").replace('\n', ' ')
                return StringUtil.shortenTextWithEllipsis(oneLine, 50, 0, true)
            }
        }
    }

    init {
        add(list, BorderLayout.CENTER)

        // Subscribe to updates
        val connection = project.messageBus.connect(this)
        connection.subscribe(EditTestsService.TOPIC, object : TestsChangedListener {
            override fun testsChanged(tests: List<Test>) {
                listModel.replaceAll(tests)
            }
        })
    }

    // Initial sync so the tab shows existing tests right away
    fun setInitialTests(tests: List<Test>) {
        listModel.replaceAll(tests)
    }

    override fun dispose() {
        // message bus connection bound to `this` is disposed automatically
    }
}
