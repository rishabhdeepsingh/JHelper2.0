package com.github.rishabhdeepsingh.jhelper20.ui

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

object Utils {
    /**
     * Finds method @{code methodName} in @{code file} and opens it in an editor.
     */
    fun openMethodInEditor(project: Project, file: VirtualFile) {
        FileEditorManager.getInstance(project).openFile(file, true)
    }
}