package com.murphy.core

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.murphy.util.PLUGIN_NAME
import com.murphy.util.notifyError
import org.jetbrains.kotlin.idea.base.psi.childrenDfsSequence
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * 在 Smart Mode 下同步执行 action。
 * - 如果当前不在 Dumb Mode，直接通过 invokeAndWait 在 EDT 上执行
 * - 如果在 Dumb Mode，通过 runWhenSmart + CountDownLatch 等待索引完成后同步执行
 */
fun DumbService.executeOnSmartMode(action: () -> Unit) {
    val latch = CountDownLatch(1)
    ApplicationManager.getApplication().invokeLater {
        if (this.isDumb) {
            this.runWhenSmart {
                try {
                    action()
                } finally {
                    latch.countDown()
                }
            }
        } else {
            try {
                action()
            } finally {
                latch.countDown()
            }
        }
    }
    if (!latch.await(60, TimeUnit.SECONDS)) {
        throw ProcessCanceledException()
    }
}

fun obfuscateByCheck(
    myProject: Project,
    myPsi: PsiElement,
    check: CustomCheck,
    onFinished: () -> Unit
) = runBackgroundableTask(PLUGIN_NAME, myProject) { indicator ->
    try {
        indicator.text = "Find element"
        val list = runReadAction {
            val elements = myPsi.childrenDfsSequence().filterIsInstance<PsiNamedElement>().toList()
            val xmlElements = RenamableXmlElement.findElements(myProject, check, elements)
            val codeElements = RenamableCodeElement.findElements(check, elements)
            xmlElements + codeElements
        }
        indicator.text = "Create naming"
        val service = DumbService.getInstance(myProject)
        val namingPool = NamingPool.createNamingPool(list)
        val total = list.count()
        list.forEachIndexed { index, item ->
            indicator.fraction = index / total.toDouble()
            indicator.text = "${item.currentName} ${index + 1}/$total"
            val newName = item.namingIndex?.let { namingPool.getOrNull(it) }
            service.executeOnSmartMode { item.performRename(myProject, newName) }
        }
        onFinished()
    } catch (e: Exception) {
        if (e is ProcessCanceledException) throw e
        else notifyError(myProject, "${e.message}")
        e.printStackTrace()
    }
}

data class CustomCheck(
    val ktFile: Boolean = true,
    val clazz: Boolean = true,
    val function: Boolean = true,
    val variable: Boolean = true,
    val resource: Boolean = true,
    val directory: Boolean = true
)

fun computeTime(startTime: Long): String {
    val time = System.currentTimeMillis() - startTime
    val days = TimeUnit.MILLISECONDS.toDays(time)
    val toHours = TimeUnit.MILLISECONDS.toHours(time)
    val toMinutes = TimeUnit.MILLISECONDS.toMinutes(time)
    val toSeconds = TimeUnit.MILLISECONDS.toSeconds(time)
    val hours = toHours - TimeUnit.DAYS.toHours(days)
    val minutes = toMinutes - TimeUnit.HOURS.toMinutes(toHours)
    val seconds = toSeconds - TimeUnit.MINUTES.toSeconds(toMinutes)
    return buildString {
        if (days > 0) append(days).append(" d ")
        if (hours > 0) append(hours).append(" h ")
        if (minutes > 0) append(minutes).append(" m ")
        if (seconds >= 0) append(seconds).append(" s ")
    }
}