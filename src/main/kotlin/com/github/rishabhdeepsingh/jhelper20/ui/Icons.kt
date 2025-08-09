package com.github.rishabhdeepsingh.jhelper20.ui

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

object Icons {
    @JvmField
    val JHelperIcon: Icon = load("/icons/pluginIcon.svg")

    @JvmStatic
    fun load(path: String): Icon {
        return IconLoader.getIcon(path, Icons::class.java)
    }

}