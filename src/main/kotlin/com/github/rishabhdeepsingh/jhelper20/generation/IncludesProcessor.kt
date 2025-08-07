package com.github.rishabhdeepsingh.jhelper20.generation

import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import java.nio.file.Paths

class IncludesProcessor private constructor() {
    private val processedFiles = mutableSetOf<String>()
    private val result = StringBuilder()
    private val standardIncludes = mutableSetOf<String>()

    private fun resolveRelativePath(basePath: String, relativePath: String): String {
        return try {
            val base = Paths.get(basePath).parent
            base.resolve(relativePath).normalize().toString()
        } catch (e: Exception) {
            relativePath
        }
    }

    private fun processIncludeLine(line: String, currentFilePath: String): Boolean {
        // Parse include directive
        val includePattern = """#include\s*["<]([^">]+)[">]""".toRegex()
        val match = includePattern.find(line) ?: return false

        val includePath = match.groupValues[1]
        val isSystemInclude = line.contains("<")

        if (isSystemInclude) {
            // Store system includes to put them at the top of the final file
            standardIncludes.add(line)
            return true
        }

        // Resolve relative path for local includes
        val absolutePath = resolveRelativePath(currentFilePath, includePath)
        if (processedFiles.contains(absolutePath)) {
            return true
        }

        // Process the included file
        val includedFile = LocalFileSystem.getInstance().findFileByPath(absolutePath)
        if (includedFile != null) {
            processFile(includedFile)
        }
        return true
    }

    private fun processFile(virtualFile: VirtualFile) {
        val filePath = virtualFile.path
        if (processedFiles.contains(filePath)) {
            return
        }
        processedFiles.add(filePath)

        val content = virtualFile.inputStream.bufferedReader().use { it.readText() }
        val lines = content.lines()

        // Process file line by line
        for (line in lines) {
            when {
                line.trim().startsWith("#include") -> {
                    processIncludeLine(line.trim(), filePath)
                }

                line.trim().startsWith("#pragma once") -> {
                    // Skip pragma once directives
                    continue
                }

                else -> {
                    // Add non-include lines to the result
                    result.append(line).append("\n")
                }
            }
        }
    }

    private fun getProcessedContent(): String {
        return buildString {
            // Add standard includes at the top
            standardIncludes.forEach {
                appendLine(it)
            }
            appendLine() // Appends the extra newline separator

            // Add processed content
            append(result)
        }
    }

    companion object {
        fun process(file: PsiFile): String {
            val processor = IncludesProcessor()
            processor.processFile(file.virtualFile)
            return processor.getProcessedContent()
        }
    }
}