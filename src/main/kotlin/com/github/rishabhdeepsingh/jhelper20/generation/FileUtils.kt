package com.github.rishabhdeepsingh.jhelper20.generation


import com.github.rishabhdeepsingh.jhelper20.common.appendIfAbsent
import com.github.rishabhdeepsingh.jhelper20.exceptions.NotificationException
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.Computable
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import java.io.IOException
import java.util.Arrays
import kotlin.math.min

object FileUtils {
    private fun findChild(file: VirtualFile, child: String): VirtualFile? {
        if (child == ".") {
            return file
        }
        if (child == "..") {
            return file.parent
        }
        return file.findChild(child)
    }

    fun findOrCreateByRelativePath(root: VirtualFile, localPath: String): VirtualFile {
        return ApplicationManager.getApplication().runWriteAction(object : Computable<VirtualFile> {
            override fun compute(): VirtualFile {
                val path = StringUtil.trimStart(localPath, "/")
                if (path.isEmpty()) {
                    return root
                }
                var index = path.indexOf('/')
                if (index < 0) {
                    index = path.length
                }
                val name = path.take(index)

                var child = findChild(root, name)
                if (child == null) {
                    try {
                        child = if (index == path.length) {
                            root.createChildData(this, name)
                        } else {
                            root.createChildDirectory(this, name)
                        }
                    } catch (e: IOException) {
                        throw NotificationException("Couldn't create directory: " + root.path + '/' + name, e)
                    }
                }

                if (index < path.length) {
                    return findOrCreateByRelativePath(child, path.substring(index + 1))
                }
                return child
            }
        })
    }

    /**
     * Checks if a given file is a C++ file.
     * In other words, checks if code may be generated for that file
     */
    fun isNotCppFile(file: PsiFile): Boolean {
        return !(file.name.endsWith(".cpp") || file.name.endsWith(".cc") || file.name.endsWith(".c"))
    }

    fun writeToFile(outputFile: PsiFile, vararg strings: String) {
        val project = outputFile.project
        val document = PsiDocumentManager.getInstance(project).getDocument(outputFile)
            ?: throw NotificationException("Couldn't open output file as document")

        WriteCommandAction.writeCommandAction(project).run<RuntimeException> {
            document.deleteString(0, document.textLength)
            Arrays.stream(strings).forEach { string: String ->
                document.insertString(document.textLength, string)
            }
            FileDocumentManager.getInstance().saveDocument(document)
            PsiDocumentManager.getInstance(project).commitDocument(document)
        }
    }

    fun relativePath(parent: String, childPath: String): String {
        val parentPath = parent.appendIfAbsent("/")
        require(isChild(parentPath, childPath)) { "childPath should be inside a parentPath" }
        // Minimum is needed for case when childPath = parentPath and there's no / at the end of childPath
        return childPath.substring(min(parentPath.length, childPath.length))
    }

    fun isChild(parent: String, child: String): Boolean =
        child.appendIfAbsent("/").startsWith(parent.appendIfAbsent("/"))

    fun getDirectory(filePath: String): String {
        val index = filePath.lastIndexOf('/')
        if (index < 0) {
            return "."
        }
        return filePath.take(index + 1)
    }

    fun getFilename(filePath: String): String {
        val index = filePath.lastIndexOf('/')
        if (index < 0) {
            return filePath
        }
        return filePath.substring(index + 1)
    }
}