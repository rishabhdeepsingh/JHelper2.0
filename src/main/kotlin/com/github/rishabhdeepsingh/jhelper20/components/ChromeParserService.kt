package com.github.rishabhdeepsingh.jhelper20.components

import com.github.rishabhdeepsingh.jhelper20.common.currentProject
import com.github.rishabhdeepsingh.jhelper20.network.SimpleHttpServer
import com.github.rishabhdeepsingh.jhelper20.parser.CompetitiveCompanion
import com.github.rishabhdeepsingh.jhelper20.task.StreamConfiguration
import com.github.rishabhdeepsingh.jhelper20.task.TaskData
import com.github.rishabhdeepsingh.jhelper20.task.TaskUtils
import com.github.rishabhdeepsingh.jhelper20.task.TestType
import com.github.rishabhdeepsingh.jhelper20.ui.Utils
import com.intellij.openapi.components.Service
import com.intellij.openapi.vfs.VirtualFile
import java.net.InetSocketAddress

/**
 * A Component to monitor request from CHelper Chrome Extension and parse them to Tasks
 */
@Service
class ChromeParserService {
    private lateinit var server: SimpleHttpServer

    fun startHttpServer() {
        println("Starting ChromeParserService")
        server = SimpleHttpServer(
            InetSocketAddress("localhost", PORT)
        ) { request: String ->
            val task = parser.parseJsonTask(request.substringAfter("json"))

            println("Tasks: $task")

            println("Task Directory: ${ProjectConfigurationState.getInstance()?.tasksDirectory}")

            val generatedFile = TaskUtils.saveNewTask(
                TaskData(
                    task?.name ?: "",
                    task?.name ?: "",
                    String.format("%s.cpp", task?.name ?: ""),
                    StreamConfiguration.STANDARD,
                    StreamConfiguration.STANDARD,
                    TestType.SINGLE,
                    task?.tests ?: emptyList()
                ), currentProject()
            )
            Utils.openMethodInEditor(currentProject(), generatedFile as VirtualFile)
        }

        Thread(server, "ChromeParserThread").start()
    }

    fun stopServer() {
        server.stop()
    }

    companion object {
        private const val PORT = 4243
        val parser = CompetitiveCompanion()
    }
}