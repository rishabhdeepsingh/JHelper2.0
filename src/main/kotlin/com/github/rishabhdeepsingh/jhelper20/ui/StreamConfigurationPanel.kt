package com.github.rishabhdeepsingh.jhelper20.ui

import com.github.rishabhdeepsingh.jhelper20.task.StreamConfiguration
import com.github.rishabhdeepsingh.jhelper20.task.StreamType
import com.intellij.openapi.ui.ComboBox
import org.jdesktop.swingx.VerticalLayout
import javax.swing.JPanel
import javax.swing.JTextField

/**
 * Panel for configuration input or output for Task.
 */
class StreamConfigurationPanel(
    val configuration: StreamConfiguration,
    allowedTypes: List<StreamType>,
    defaultFileName: String,
    val listener: SizeChangedListener?,
) : JPanel(VerticalLayout()) {
    private val type = ComboBox(allowedTypes.toTypedArray())
    private val fileName: JTextField =
        JTextField(if (configuration.type?.hasStringParameter == true) configuration.fileName else defaultFileName)

    init {
        fileName.isVisible = (type.selectedItem as? StreamType)?.hasStringParameter == true

        type.selectedItem = configuration.type
        type.addActionListener {
            fileName.isVisible = (type.selectedItem as? StreamType)?.hasStringParameter == true
            listener?.sizeChanged()
        }

        add(type)
        add(fileName)
    }

    val streamConfiguration: StreamConfiguration
        get() = StreamConfiguration(type.selectedItem as StreamType, fileName.text)

    fun interface SizeChangedListener {
        fun sizeChanged()
    }
}