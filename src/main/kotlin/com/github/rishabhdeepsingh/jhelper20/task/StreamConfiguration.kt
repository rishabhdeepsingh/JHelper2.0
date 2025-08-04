package com.github.rishabhdeepsingh.jhelper20.task

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.Locale

class StreamConfiguration(val type: StreamType?, val fileName: String?) {
    @JsonCreator
    constructor(@JsonProperty("type") type: StreamType?) : this(type, null)

    fun getFileName(taskId: String, extension: String?): String? {
        if (type == StreamType.CUSTOM) {
            return fileName
        }
        if (type == StreamType.TASK_ID) {
            return taskId.lowercase(Locale.getDefault()) + extension
        }
        return null
    }

    companion object {
        val STANDARD: StreamConfiguration = StreamConfiguration(StreamType.STANDARD)
        val TASK_ID: StreamConfiguration = StreamConfiguration(StreamType.TASK_ID)

        val OUTPUT_TYPES = listOf<StreamType>(
            StreamType.STANDARD, StreamType.TASK_ID, StreamType.CUSTOM
        )
    }
}