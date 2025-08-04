package com.github.rishabhdeepsingh.jhelper20.configuration

import com.github.rishabhdeepsingh.jhelper20.task.StreamConfiguration
import com.github.rishabhdeepsingh.jhelper20.task.StreamType
import com.github.rishabhdeepsingh.jhelper20.task.TaskData
import com.github.rishabhdeepsingh.jhelper20.task.Test
import com.github.rishabhdeepsingh.jhelper20.task.TestType
import com.github.rishabhdeepsingh.jhelper20.ui.TaskSettingsComponent
import com.intellij.execution.ExecutionTarget
import com.intellij.execution.Executor
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.RunConfigurationBase
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import org.jdom.Element
import javax.swing.JComponent

class TaskConfiguration(project: Project?, factory: ConfigurationFactory?) :
    RunConfigurationBase<Any>(project!!, factory, "") {

    var className: String = ""
        private set
    var cppPath: String = ""
        private set
    var input: StreamConfiguration
        private set
    var output: StreamConfiguration
        private set
    var testType: TestType
        private set
    var tests: List<Test>

    init {
        input = StreamConfiguration(StreamType.STANDARD)
        output = StreamConfiguration(StreamType.STANDARD)
        testType = TestType.SINGLE
        tests = listOf()
    }

    override fun canRunOn(target: ExecutionTarget): Boolean {
        return target is TaskConfigurationExecutionTarget
    }

    override fun clone(): TaskConfiguration =
        (super.clone() as TaskConfiguration).also {
            it.className = className
            it.cppPath = cppPath
            it.input = input
            it.output = output
            it.testType = testType
            it.tests = tests
        }

    override fun readExternal(element: Element) {
        super.readExternal(element)
        className = element.getAttributeValue("className", "")
        cppPath = element.getAttributeValue("cppPath", "")
        input = readStreamConfiguration(element, "inputPath", "inputFile")
        output = readStreamConfiguration(element, "outputPath", "outputFile")
        testType = try {
            TestType.valueOf(element.getAttributeValue("testType", "SINGLE"))
        } catch (ignored: IllegalArgumentException) {
            TestType.SINGLE
        }

        for (child in element.children) {
            if (child.name == "tests") {
                tests = List(child.children.size) {
                    readTest(child.children[it])
                }
            }
        }
    }

    override fun writeExternal(element: Element) {
        element.setAttribute("className", className)
        element.setAttribute("cppPath", cppPath)
        element.setAttribute("inputType", input.type!!.name)
        if (input.fileName != null) {
            element.setAttribute("inputFile", input.fileName)
        }
        element.setAttribute("outputType", output.type!!.name)
        if (output.fileName != null) {
            element.setAttribute("outputFile", output.fileName)
        }
        element.setAttribute("testType", testType.name)

        val testsElements = Element("tests")
        for (test in tests) {
            val testElement = Element("test")
            testElement.setAttribute("input", test.input)
            testElement.setAttribute("output", test.output)
            testElement.setAttribute("active", test.active.toString())
            testsElements.addContent(testElement)
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
                        name,
                        className,
                        cppPath,
                        input,
                        output,
                        testType,
                        listOf()
                    )
                )
            }

            override fun applyEditorTo(settings: TaskConfiguration) {
                val data = component.task
                settings.className = data.className
                settings.cppPath = data.cppPath
                settings.input = data.input
                settings.output = data.output
                settings.testType = data.testType
            }

            override fun createEditor(): JComponent {
                return component
            }
        }
    }

    override fun checkConfiguration() {}

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState? {
//      RunConfiguration configuration = TaskRunner.getRunnerSettings(getProject()).getConfiguration();
//		return new CidrCommandLineState(environment, new CMakeLauncher(environment, (CMakeAppRunConfiguration)configuration));
        throw RuntimeException("This method is not expected to be used")
    }

    fun setFromTaskData(data: TaskData) {
        name = data.name
        className = data.className
        cppPath = data.cppPath
        input = data.input
        output = data.output
        testType = data.testType
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
            } catch (ignored: RuntimeException) {
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
            val input = element.getAttributeValue("input")
            val output = element.getAttributeValue("output")
            val active = element.getAttributeValue("active") == "true"
            return Test(input, output, 0, active)
        }
    }
}