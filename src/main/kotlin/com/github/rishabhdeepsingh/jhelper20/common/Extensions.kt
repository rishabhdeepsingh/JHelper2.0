package com.github.rishabhdeepsingh.jhelper20.common

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile


/**
 * TODO(rishabhdeepsingh): Change this later to get the exact source roots in case multiple projects are open.
 */
fun Project.firstRootSource(): VirtualFile {
    return LocalFileSystem.getInstance().findFileByPath(this.basePath!!)!!
}

/**
 * TODO(rishabhdeepsingh): Change this later to get the exact project.
 */
fun currentProject(): Project = ProjectManager.getInstance().openProjects[0]


/**
 * Appends string at the end of the given string if suffix is absent.
 */
fun String.appendIfAbsent(suffix: String): String = if (this.endsWith(suffix)) this else this + suffix