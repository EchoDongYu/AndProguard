package com.murphy.util

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

fun notifyInfo(project: Project?, content: String) {
    NotificationGroupManager.getInstance()
        .getNotificationGroup("AndGuard Notification")
        .createNotification("AndGuard finished!", content, NotificationType.INFORMATION)
        .notify(project)
}

fun notifyWarn(project: Project?, content: String) {
    NotificationGroupManager.getInstance()
        .getNotificationGroup("AndGuard Notification")
        .createNotification("AndGuard warning!", content, NotificationType.WARNING)
        .notify(project)
}

fun notifyError(project: Project?, content: String) {
    NotificationGroupManager.getInstance()
        .getNotificationGroup("AndGuard Notification")
        .createNotification("AndGuard failed!", content, NotificationType.ERROR)
        .notify(project)
}