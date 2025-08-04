package com.github.rishabhdeepsingh.jhelper20.ui

import com.github.rishabhdeepsingh.jhelper20.generation.FileUtils
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextBrowseFolderListener
import com.intellij.openapi.ui.TextFieldWithBrowseButton.NoPathCompletion
import com.intellij.openapi.vfs.VirtualFile
import javax.swing.JTextField


class FileSelector(project: Project, initialValue: String, descriptor: FileChooserDescriptor) :
    NoPathCompletion(JTextField(initialValue)) {

    init {
        addBrowseFolderListener(RelativePathBrowseListener(descriptor, project))
        installPathCompletion(descriptor)
    }

    private class RelativePathBrowseListener(descriptor: FileChooserDescriptor, project: Project) :
        TextBrowseFolderListener(descriptor, project) {
        private val basePath = project.basePath

        override fun expandPath(path: String) = "$basePath/$path"

        override fun chosenFileToResultingText(chosenFile: VirtualFile) =
            FileUtils.relativePath(basePath ?: "", chosenFile.path)
    }
}