package com.github.rishabhdeepsingh.jhelper20.generation

import com.github.rishabhdeepsingh.jhelper20.common.firstRootSource
import com.github.rishabhdeepsingh.jhelper20.exceptions.NotificationException
import com.github.rishabhdeepsingh.jhelper20.common.CommonUtils.getStringFromInputStream
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import java.io.IOException
import java.util.regex.Matcher
import java.util.regex.Pattern


fun String.replaceAll(pattern: Pattern, replacement: String?): String {
    return pattern.matcher(this).replaceAll(Matcher.quoteReplacement(replacement ?: ""))
}

/**
 * Utility class for customizing templates of code
 */
object TemplatesUtils {

    fun replaceAll(text: String?, pattern: Pattern, replacement: String?): String {
        return pattern.matcher(text ?: "").replaceAll(Matcher.quoteReplacement(replacement ?: ""))
    }

    val CLASS_NAME: Pattern = Pattern.compile("%ClassName%", Pattern.LITERAL)
    val TASK_FILE: Pattern = Pattern.compile("%TaskFile%", Pattern.LITERAL)
    val TESTS: Pattern = Pattern.compile("%Tests%", Pattern.LITERAL)
    val SOLVER_CALL: Pattern = Pattern.compile("%SolverCall%", Pattern.LITERAL)
    val INPUT: Pattern = Pattern.compile("%Input%", Pattern.LITERAL)
    val OUTPUT: Pattern = Pattern.compile("%Output%", Pattern.LITERAL)
    val CODE: Pattern = Pattern.compile("%Code%", Pattern.LITERAL)


    fun getTemplate(project: Project, name: String): String {
        val filename = "$name.template"
        var file = project.firstRootSource().findFileByRelativePath(filename)
        if (file == null) {
            createTemplateFromDefault(project, name)
            file = project.firstRootSource().findFileByRelativePath(filename)
            if (file == null) {
                throw RuntimeException("Can't open template file($filename) after its creation")
            }
        }
        return FileDocumentManager.getInstance().getDocument(file)?.text
            ?: throw NotificationException("Couldn't find template \"$name\"")
    }

    private fun createTemplateFromDefault(project: Project, name: String) {
        val filename = "$name.template"
        val file: VirtualFile = FileUtils.findOrCreateByRelativePath(project.firstRootSource(), filename)
        val psiFile = PsiManager.getInstance(project).findFile(file)
        val defaultTemplate: String
        try {
            defaultTemplate = getResourceContent("/name/admitriev/jhelper/templates/$filename")
        } catch (e: IOException) {
            throw NotificationException("Couldn't open default template $filename", e)
        }

        psiFile?.let { FileUtils.writeToFile(it, defaultTemplate) }
    }

    /**
     * Returns content of a resource file (from resource folder) as a string.
     */
    @Throws(IOException::class)
    private fun getResourceContent(filePath: String): String {
        TemplatesUtils::class.java.getResourceAsStream(filePath).use { stream ->
            if (stream == null) {
                throw IOException("Couldn't open a stream to resource $filePath")
            }
            return getStringFromInputStream(stream)
        }
    }
}