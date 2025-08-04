package com.github.rishabhdeepsingh.jhelper20.ui

import com.github.rishabhdeepsingh.jhelper20.common.firstRootSource
import com.github.rishabhdeepsingh.jhelper20.task.StreamConfiguration
import com.github.rishabhdeepsingh.jhelper20.task.StreamType
import com.github.rishabhdeepsingh.jhelper20.task.TaskData
import com.github.rishabhdeepsingh.jhelper20.task.TestType
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.LabeledComponent
import org.jdesktop.swingx.VerticalLayout
import javax.swing.JPanel
import javax.swing.JTextField


class TaskSettingsComponent(
    private val project: Project,
    private val canChangeName: Boolean,
    private val listener: StreamConfigurationPanel.SizeChangedListener? = null,
) : JPanel(VerticalLayout()) {

    private lateinit var name: JTextField
    private lateinit var className: JTextField
    private lateinit var cppPath: FileSelector
    private lateinit var input: StreamConfigurationPanel
    private lateinit var output: StreamConfigurationPanel
    private lateinit var testType: ComboBox<TestType>

    init {
        setTaskData(TaskData.emptyTaskData())
    }

    fun setTaskData(taskData: TaskData) {
        removeAll()
        name = JTextField(taskData.name)
        name.isEnabled = canChangeName

        className = JTextField(taskData.className)
        cppPath = FileSelector(
            project,
            taskData.cppPath,
            RelativeFileChooserDescriptor.fileChooser(project.firstRootSource())
        )
        input = StreamConfigurationPanel(
            taskData.input,
            StreamType.entries,
            "input.txt",
            listener
        )
        output = StreamConfigurationPanel(
            taskData.output,
            StreamConfiguration.OUTPUT_TYPES,
            "output.txt",
            listener
        )

        testType = ComboBox(TestType.values())
        testType.selectedItem = taskData.testType

        add(LabeledComponent.create(name, "Task name"))
        add(LabeledComponent.create(className, "Class name"))
        add(LabeledComponent.create(cppPath, "Path"))
        add(LabeledComponent.create(input, "Input"))
        add(LabeledComponent.create(output, "Output"))
        add(LabeledComponent.create(testType, "Test type"))

//        UIUtils.mirrorFields(name, className)
//        UIUtils.mirrorFields(name, cppPath.textField, defaultCppPathFormat())
    }

    val task: TaskData
        get() = TaskData(
            name.text,
            className.text,
            cppPath.text,
            input.streamConfiguration,
            output.streamConfiguration,
            testType.selectedItem as TestType,
            listOf()
        )
}