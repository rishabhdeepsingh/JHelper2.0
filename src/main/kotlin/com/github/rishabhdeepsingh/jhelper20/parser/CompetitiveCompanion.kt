package com.github.rishabhdeepsingh.jhelper20.parser

import com.github.rishabhdeepsingh.jhelper20.task.Task
import com.github.rishabhdeepsingh.jhelper20.task.Test
import kotlinx.serialization.json.Json
import javax.swing.Icon
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonArray


class CompetitiveCompanion : Parser {
    override val icon: Icon?
        get() = null

    override val name: String
        get() = "CompetitiveCompanion"

    override fun parseJsonTask(json: String): Task? {
        return try {
            val jsonObject = Json.decodeFromString<JsonObject>(json)

            // Extract basic task information
            val name = jsonObject["name"]?.jsonPrimitive?.content ?: ""
            val group = jsonObject["group"]?.jsonPrimitive?.content ?: ""

            // Parse tests
            val testsArray = jsonObject["tests"]?.jsonArray ?: return null
            val tests = testsArray.mapNotNull { testElement ->
                testElement.jsonObject.let { testObj ->
                    val input = testObj["input"]?.jsonPrimitive?.content?.trim() ?: return@mapNotNull null
                    val output = testObj["output"]?.jsonPrimitive?.content?.trim() ?: return@mapNotNull null
                    Test(input = input, output = output, active = true)
                }
            }

            Task(
                name = name, tests = tests, contestName = group
            )
        } catch (e: Exception) {
            null
        }
    }
}