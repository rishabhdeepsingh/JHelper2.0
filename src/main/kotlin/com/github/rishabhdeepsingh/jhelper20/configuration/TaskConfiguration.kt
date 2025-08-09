package com.github.rishabhdeepsingh.jhelper20.configuration

import com.github.rishabhdeepsingh.jhelper20.task.StreamConfiguration
import com.github.rishabhdeepsingh.jhelper20.task.StreamType
import com.github.rishabhdeepsingh.jhelper20.task.TaskData
import com.github.rishabhdeepsingh.jhelper20.task.Test
import com.github.rishabhdeepsingh.jhelper20.ui.TaskSettingsComponent
import com.intellij.execution.ExecutionTarget
import com.intellij.execution.Executor
import com.intellij.execution.configuration.EmptyRunProfileState
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.RunConfigurationBase
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import org.jdom.Element
import javax.swing.JComponent

private const val ATTR_CLASS_NAME = "className"
private const val ATTR_CPP_PATH = "cppPath"
private const val ATTR_INPUT_TYPE = "inputType"
private const val ATTR_OUTPUT_TYPE = "outputType"
private const val ATTR_INPUT_FILE = "inputFile"
private const val ATTR_OUTPUT_FILE = "outputFile"

class TaskConfiguration(project: Project, factory: ConfigurationFactory?) :
    RunConfigurationBase<Any>(project, factory, "") {

    var className: String = ""
        private set
    var cppPath: String = ""
        private set
    var input: StreamConfiguration = StreamConfiguration(StreamType.STANDARD)
        private set
    var output: StreamConfiguration = StreamConfiguration(StreamType.STANDARD)
        private set
    var tests: List<Test> = listOf()

    override fun canRunOn(target: ExecutionTarget): Boolean {
        return target is TaskConfigurationExecutionTarget
    }

    override fun clone(): TaskConfiguration = (super.clone() as TaskConfiguration).also {
        it.className = className
        it.cppPath = cppPath
        it.input = input
        it.output = output
        it.tests = tests
    }

    override fun readExternal(element: Element) {
        super.readExternal(element)
        className = element.getAttributeValue(ATTR_CLASS_NAME).orEmpty()
        cppPath = element.getAttributeValue(ATTR_CPP_PATH).orEmpty()
        input = readStreamConfiguration(element, "inputPath", ATTR_INPUT_FILE)
        output = readStreamConfiguration(element, "outputPath", ATTR_OUTPUT_FILE)
        tests = element.getChild("tests")?.getChildren("test")?.map(::readTest).orEmpty()
    }

    override fun writeExternal(element: Element) {
        element.setAttribute(ATTR_CLASS_NAME, className)
        element.setAttribute(ATTR_CPP_PATH, cppPath)
        element.setAttribute(ATTR_INPUT_TYPE, input.type!!.name)
        input.fileName?.let { element.setAttribute(ATTR_INPUT_FILE, it) }
        element.setAttribute(ATTR_OUTPUT_TYPE, output.type!!.name)
        output.fileName?.let { element.setAttribute(ATTR_OUTPUT_FILE, it) }

        val testsElements = Element("tests").apply {
            for (test in tests) {
                val testElement = Element("test").apply {
                    setAttribute("input", test.input)
                    setAttribute("output", test.output)
                    setAttribute("active", test.active.toString())
                }
                addContent(testElement)
            }
        }
        element.addContent(testsElements)

        super.writeExternal(element)
    }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        return object : SettingsEditor<TaskConfiguration>() {
            private val component = TaskSettingsComponent(project, false, null)

            override fun resetEditorFrom(settings: TaskConfiguration) {
                component.setTaskData(
                    TaskData(
                        name, className, cppPath, input, output, listOf()
                    )
                )
            }

            override fun applyEditorTo(settings: TaskConfiguration) {
                val data = component.task
                settings.className = data.className
                settings.cppPath = data.cppPath
                settings.input = data.input
                settings.output = data.output
                settings.tests = data.tests.toList()
            }

            override fun createEditor(): JComponent {
                return component
            }
        }
    }

    override fun checkConfiguration() {}

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState? {
        return EmptyRunProfileState.INSTANCE
    }

    fun setFromTaskData(data: TaskData) {
        name = data.name
        className = data.className
        cppPath = data.cppPath
        input = data.input
        output = data.output
        tests = data.tests
    }

    companion object {
        private fun readStreamConfiguration(
            element: Element,
            typeAttribute: String,
            filenameAttribute: String,
        ): StreamConfiguration {
            val inputType: StreamType
            try {
                inputType = StreamType.valueOf(element.getAttribute(typeAttribute).value)
            } catch (_: RuntimeException) {
                return StreamConfiguration.STANDARD
            }
            return if (inputType.hasStringParameter) {
                StreamConfiguration(inputType, element.getAttributeValue(filenameAttribute))
            } else {
                StreamConfiguration(inputType)
            }
        }

        private fun readTest(element: Element): Test {
            assert(element.name == "test")
            val input = element.getAttributeValue("input").orEmpty()
            val output = element.getAttributeValue("output").orEmpty()
            val active = element.getAttributeValue("active").toBooleanStrictOrNull() ?: false
            return Test(input, output, active)
        }
    }
}