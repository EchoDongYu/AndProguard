package com.murphy.util

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

fun notifyInfo(project: Project?, content: String) {
    NotificationGroupManager.getInstance()
        .getNotificationGroup("AndProguard Notification")
        .createNotification(title = "AndProguard finished!", content = content, type = NotificationType.INFORMATION)
        .notify(project)
}

fun notifyWarn(project: Project?, content: String) {
    NotificationGroupManager.getInstance()
        .getNotificationGroup("AndProguard Notification")
        .createNotification(title = "AndProguard warning!", content = content, type = NotificationType.WARNING)
        .notify(project)
}

fun notifyError(project: Project?, content: String) {
    NotificationGroupManager.getInstance()
        .getNotificationGroup("AndProguard Notification")
        .createNotification(title = "AndProguard failed!", content = content, type = NotificationType.ERROR)
        .notify(project)
}