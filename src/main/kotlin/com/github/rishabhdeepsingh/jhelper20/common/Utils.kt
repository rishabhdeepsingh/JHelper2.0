package com.github.rishabhdeepsingh.jhelper20.common

import com.github.rishabhdeepsingh.jhelper20.configuration.TaskConfiguration
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets


object CommonUtils {

    @JvmStatic
    fun getStringFromInputStream(stream: InputStream): String {
        BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8)).use { reader ->
            val sb = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                sb.append(line).append('\n')
            }
            return sb.toString()
        }
    }

    @JvmStatic
    fun generatePSIFromTask(project: Project, taskConfiguration: TaskConfiguration): PsiFile? {
        val pathToClassFile = taskConfiguration.cppPath

//        ProjectFileIndex.getInstance(project).getContentRootForFile()

//        val virtualFile = project.guessProjectDir()?.findFileByRelativePath(pathToClassFile)
//            ?: throw IllegalStateException("Task file not found: Seems your task is in inconsistent state")
//
//        return PsiManager.getInstance(project).findFile(virtualFile)
//            ?: throw IllegalStateException("Couldn't get PSI file for input file")
        return null
    }

}