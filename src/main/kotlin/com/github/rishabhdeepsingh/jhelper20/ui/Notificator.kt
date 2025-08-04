package com.github.rishabhdeepsingh.jhelper20.ui


import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType

object Notificator {
    private val GROUP: NotificationGroup = NotificationGroupManager.getInstance().getNotificationGroup("Plugin Error")

    fun showNotification(content: String?, notificationType: NotificationType?) {
        showNotification("", content, notificationType)
    }

    fun showNotification(title: String?, content: String?, notificationType: NotificationType?) {
        if (title == null) return
        if (content == null) return
        if (notificationType == null) return
        GROUP.createNotification(title, content, notificationType).notify(null)
    }

    fun warn(title: String?, content: String?) {
        NotificationGroupManager.getInstance().getNotificationGroup("Plugin Error")
            .createNotification(title ?: "", content ?: "", NotificationType.WARNING).notify(null)
    }

    fun info(title: String?, content: String?) {
        NotificationGroupManager.getInstance().getNotificationGroup("IDE and Plugin Updates")
            .createNotification(title ?: "", content ?: "", NotificationType.INFORMATION).notify(null)
    }
}