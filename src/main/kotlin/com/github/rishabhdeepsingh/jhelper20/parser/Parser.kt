package com.github.rishabhdeepsingh.jhelper20.parser

import com.github.rishabhdeepsingh.jhelper20.task.Task
import javax.swing.Icon


interface Parser {
    val icon: Icon?

    val name: String?

    fun parseJsonTask(json: String): Task?
}