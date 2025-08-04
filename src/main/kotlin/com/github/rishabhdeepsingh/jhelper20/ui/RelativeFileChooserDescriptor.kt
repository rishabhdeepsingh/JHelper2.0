package com.github.rishabhdeepsingh.jhelper20.ui

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.vfs.VirtualFile
import com.github.rishabhdeepsingh.jhelper20.generation.FileUtils.isChild

class RelativeFileChooserDescriptor private constructor(
    baseDir: VirtualFile,
    chooseFiles: Boolean,
    chooseFolders: Boolean,
) : FileChooserDescriptor(chooseFiles, chooseFolders, false, false, false, false) {
    private val basePath = baseDir.path

    init {
        withShowHiddenFiles(true)
        setRoots(baseDir)
    }

    override fun isFileVisible(file: VirtualFile, showHiddenFiles: Boolean): Boolean {
        return (isChild(basePath, file.path) || isChild(file.path, basePath))
    }

    override fun isFileSelectable(file: VirtualFile?): Boolean {
        return file != null && isChild(basePath, file.path)
    }

    companion object {
        fun fileChooser(baseDir: VirtualFile): RelativeFileChooserDescriptor {
            return RelativeFileChooserDescriptor(baseDir, chooseFiles = true, chooseFolders = false)
        }

        fun directoryChooser(baseDir: VirtualFile): RelativeFileChooserDescriptor {
            return RelativeFileChooserDescriptor(baseDir, chooseFiles = false, chooseFolders = true)
        }
    }
}