package com.github.rishabhdeepsingh.jhelper20.network

import com.github.rishabhdeepsingh.jhelper20.common.CommonUtils.getStringFromInputStream
import com.github.rishabhdeepsingh.jhelper20.ui.Notificator
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.util.Consumer
import java.io.IOException
import java.net.ServerSocket
import java.net.SocketAddress
import kotlin.text.toRegex

/**
 * Simple HTTP Server.
 * Passes every request without headers to given Consumer
 */
class SimpleHttpServer(endpoint: SocketAddress?, consumer: Consumer<String>) : Runnable {
    private val consumer: Consumer<String>
    private val serverSocket = ServerSocket()

    init {
        serverSocket.bind(endpoint)
        this.consumer = consumer
    }

    override fun run() {
        while (true) {
            try {
                if (serverSocket.isClosed) {
                    return
                }
                serverSocket.accept().use { socket ->
                    val inputStream = socket.getInputStream()
                    val request = getStringFromInputStream(inputStream)
                    val strings = request.split("\n\n".toRegex(), limit = 2).toTypedArray()

                    //ignore headers
                    if (strings.size < 2) {
                        Notificator.showNotification(
                            "ChromeParser", "Got response without body. Ignore.", NotificationType.INFORMATION
                        )
                        return@use
                    }
                    val text = strings[1]
                    ApplicationManager.getApplication().invokeLater(
                        { consumer.consume(text) }, ModalityState.defaultModalityState()
                    )
                }
            } catch (_: IOException) {
            }
        }
    }

    fun stop() {
        try {
            serverSocket.close()
        } catch (_: IOException) {
        }
    }
}