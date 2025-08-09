package com.github.rishabhdeepsingh.jhelper20.toolWindow

import com.github.rishabhdeepsingh.jhelper20.listeners.TestsChangedListener
import com.github.rishabhdeepsingh.jhelper20.services.EditTestsService
import com.github.rishabhdeepsingh.jhelper20.task.Test
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.EditorFontType
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.openapi.util.text.StringUtil
import com.intellij.ui.CollectionListModel
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.SimpleColoredComponent
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.util.Alarm
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Cursor
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class TestsPanel(project: Project) : JPanel(BorderLayout()), Disposable {
    private val editTestsService = project.service<EditTestsService>()

    private val listModel = CollectionListModel<Test>()
    private val list = JBList(listModel).apply {
        selectionMode = ListSelectionModel.SINGLE_SELECTION
        emptyText.text = "No tests"
        cellRenderer = TestWithCheckBoxRenderer()
    }
    val editorFont = EditorColorsManager.getInstance().globalScheme.getFont(EditorFontType.PLAIN)

    private val inputArea = JBTextArea().apply {
        lineWrap = true
        wrapStyleWord = true
        font = editorFont
    }
    private val outputArea = JBTextArea().apply {
        lineWrap = true
        wrapStyleWord = true
        font = editorFont
    }

    private val debounce = Alarm(Alarm.ThreadToUse.SWING_THREAD, this)
    private var applyingFromModel = false

    // Metrics for checkbox hit area (matches renderer: 4px left inset + checkbox width)
    private val checkBoxMetrics = JCheckBox()
    private val checkBoxLeftInsetPx = 4

    init {
        val ioSplitter = OnePixelSplitter(true, 0.5f).apply {
            firstComponent = LabeledComponent.create(JBScrollPane(inputArea), "Input")
            secondComponent = LabeledComponent.create(JBScrollPane(outputArea), "Output")
        }
        val mainSplitter = OnePixelSplitter(false, 0.45f).apply {
            firstComponent = JBScrollPane(list)
            secondComponent = ioSplitter
        }
        add(mainSplitter, BorderLayout.CENTER)

        // Mouse: toggle only when clicking inside the checkbox area
        list.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                val index = list.locationToIndex(e.point)
                if (index < 0) return
                list.selectedIndex = index // select row on any click
                if (isInCheckboxArea(e, index)) {
                    toggleIndex(index)
                }
            }
        })

        // Change cursor to hand when hovering over checkbox area
        list.addMouseMotionListener(object : MouseMotionAdapter() {
            override fun mouseMoved(e: MouseEvent) {
                val index = list.locationToIndex(e.point)
                val hand = index >= 0 && isInCheckboxArea(e, index)
                list.cursor = if (hand) Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) else Cursor.getDefaultCursor()
            }
        })

        // Selection -> load editors
        list.addListSelectionListener {
            if (!it.valueIsAdjusting) loadSelectedIntoEditors()
        }

        // Editors -> auto-save with debounce
        val docListener = object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) = scheduleCommit()
            override fun removeUpdate(e: DocumentEvent) = scheduleCommit()
            override fun changedUpdate(e: DocumentEvent) = scheduleCommit()
        }
        inputArea.document.addDocumentListener(docListener)
        outputArea.document.addDocumentListener(docListener)

        // Subscribe to service updates
        val connection = project.messageBus.connect(this)
        connection.subscribe(EditTestsService.TOPIC, object : TestsChangedListener {
            override fun testsChanged(tests: List<Test>) {
                val selected = list.selectedIndex
                listModel.replaceAll(tests)
                val newIndex = if (tests.isEmpty()) -1 else selected.coerceIn(0, tests.lastIndex)
                list.selectedIndex = newIndex
                loadSelectedIntoEditors()
                list.repaint()
            }
        })
    }

    private fun isInCheckboxArea(e: MouseEvent, index: Int): Boolean {
        val cellBounds = list.getCellBounds(index, index) ?: return false
        val relX = e.x - cellBounds.x
        val cbWidth = checkBoxMetrics.preferredSize.width
        val start = checkBoxLeftInsetPx
        val end = start + cbWidth
        return relX in start..end
    }

    private fun toggleIndex(index: Int) {
        if (index !in 0 until listModel.size) return
        val current = listModel.getElementAt(index)
        editTestsService.setActive(index, !current.active)
    }

    // Helper for actions to select a particular test in the list
    fun selectIndex(index: Int) {
        if (index !in 0 until listModel.size) {
            return
        }
        list.selectedIndex = index
        list.ensureIndexIsVisible(index)
        loadSelectedIntoEditors()
    }

    fun deleteSelectedTest() {
        val idx = list.selectedIndex
        if (idx >= 0) {
            editTestsService.deleteAt(idx)
        }
    }


    private fun loadSelectedIntoEditors() {
        val idx = list.selectedIndex
        applyingFromModel = true
        try {
            val t = if (idx in 0 until listModel.size) listModel.getElementAt(idx) else null
            inputArea.text = t?.input ?: ""
            outputArea.text = t?.output ?: ""
        } finally {
            applyingFromModel = false
        }
    }

    private fun scheduleCommit() {
        if (applyingFromModel) return
        val idx = list.selectedIndex
        if (idx < 0) return

        // Capture current values at schedule time to avoid race with selection change
        val newInput = inputArea.text
        val newOutput = outputArea.text

        // Optimistically update the visible row so preview updates immediately
        val current = listModel.getElementAt(idx)
        val updated = Test(newInput, newOutput, current.active)
        listModel.setElementAt(updated, idx)
        list.repaint()

        // Debounced publish to the service (will round-trip and re-sync the list)
        debounce.cancelAllRequests()
        debounce.addRequest({
            editTestsService.updateTestIO(idx, newInput, newOutput)
        }, 200)
    }

    fun setInitialTests(tests: List<Test>) {
        listModel.replaceAll(tests)
        if (tests.isNotEmpty()) {
            list.selectedIndex = 0
            loadSelectedIntoEditors()
        }
    }

    override fun dispose() {
        // disposed with message bus connection
    }

    // Renderer: checkbox on the left + formatted text on the right
    private class TestWithCheckBoxRenderer : ListCellRenderer<Test> {
        private val panel = JPanel(BorderLayout())
        private val checkBox = JCheckBox()
        private val text = SimpleColoredComponent()

        init {
            panel.border = BorderFactory.createEmptyBorder(2, 4, 2, 4) // 4px left inset
            panel.add(checkBox, BorderLayout.WEST)
            panel.add(text, BorderLayout.CENTER)
        }

        override fun getListCellRendererComponent(
            list: JList<out Test>,
            value: Test?,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean,
        ): Component {
            text.clear()
            if (value != null) {
                checkBox.isSelected = value.active

                val number = index + 1
                val inPreview = preview(value.input)
                val outPreview = preview(value.output)

                val titleAttrs = if (value.active) SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES
                else SimpleTextAttributes.GRAYED_ATTRIBUTES

                text.append("Test $number", titleAttrs)
                text.append("  — ", SimpleTextAttributes.GRAYED_ATTRIBUTES)
                text.append("in: $inPreview", SimpleTextAttributes.GRAYED_ATTRIBUTES)
                text.append("   ")
                text.append("out: $outPreview", SimpleTextAttributes.GRAYED_ATTRIBUTES)
            } else {
                checkBox.isSelected = false
            }

            panel.isOpaque = true
            panel.background = if (isSelected) list.selectionBackground else list.background
            text.setForeground(if (isSelected) list.selectionForeground else list.foreground)
            return panel
        }

        private fun preview(s: String?): String {
            val oneLine = (s ?: "").replace("\r\n", " ").replace('\n', ' ')
            return StringUtil.shortenTextWithEllipsis(oneLine, 50, 0, true)
        }
    }
}