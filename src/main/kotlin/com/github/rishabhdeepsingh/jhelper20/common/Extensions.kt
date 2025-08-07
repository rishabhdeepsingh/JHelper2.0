package com.github.rishabhdeepsingh.jhelper20.common

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile


/**
 * Returns the first source root of the project.
 */
fun Project.firstRootSource(): VirtualFile {
    if (this.basePath == null) throw Exception("Project basePath is null.")
    return LocalFileSystem.getInstance().findFileByPath(this.basePath!!)
        ?: throw Exception("Couldn't find source root.")
}

/**
 * TODO(rishabhdeepsingh): Change this later to get the exact project.
 */
fun currentProject(): Project = ProjectManager.getInstance().openProjects[0]


/**
 * Appends string at the end of the given string if suffix is absent.
 */
fun String.appendIfAbsent(suffix: String): String = if (this.endsWith(suffix)) this else this + suffix


fun String.toClassName() = this.replace(Regex("[^a-zA-Z0-9]"), "")
