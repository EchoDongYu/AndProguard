package com.murphy.core

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import com.intellij.psi.*
import com.intellij.psi.impl.getFieldOfGetter
import com.intellij.psi.impl.getFieldOfSetter
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.refactoring.RefactoringFactory
import kotlinx.coroutines.Runnable
import java.util.concurrent.TimeUnit

fun PsiElement.childrenDfsSequence(): Sequence<PsiElement> =
    sequence {
        suspend fun SequenceScope<PsiElement>.visit(element: PsiElement) {
            element.children.forEach { visit(it) }
            yield(element)
        }
        visit(this@childrenDfsSequence)
    }

inline fun <reified T> DumbService.dumbReadAction(r: Computable<T>): T {
    return runReadActionInSmartMode<T> { r.compute() }
}

inline fun <reified T> PsiElement.dumbReadAction(r: Computable<T>): T {
    return DumbService.getInstance(project).dumbReadAction(r)
}

inline fun <reified T> Project.dumbReadAction(r: Computable<T>): T {
    return DumbService.getInstance(this).dumbReadAction(r)
}

fun PsiNamedElement.rename(newName: String, desc: String, onFinished: ((String) -> Unit)? = null) {
    val dumbService = DumbService.getInstance(project)
    val pair = dumbService.dumbReadAction { Pair(!isValid, name) }
    if (pair.first) return
    println(String.format("[$desc] %s >>> %s", pair.second, newName))
    val runnable = Runnable {
        RefactoringFactory.getInstance(project)
            .createRename(this, newName, false, false)
            .run()
    }
    ApplicationManager.getApplication().invokeAndWait {
        if (dumbService.isDumb) {
            dumbService.runWhenSmart { dumbService.smartInvokeLater(runnable) }
        } else {
            runnable.run()
        }
    }
    onFinished?.invoke(desc)
}

fun XmlAttributeValue.renameX(newName: String, desc: String, onFinished: ((String) -> Unit)? = null) {
    val dumbService = DumbService.getInstance(project)
    val pair = dumbService.dumbReadAction { Pair(!isValid, value) }
    if (pair.first) return
    println(String.format("[$desc] %s >>> %s", pair.second, newName))
    val runnable = Runnable {
        RefactoringFactory.getInstance(project)
            .createRename(this, newName, false, false)
            .run()
    }
    ApplicationManager.getApplication().invokeAndWait {
        if (dumbService.isDumb) {
            dumbService.runWhenSmart { dumbService.smartInvokeLater(runnable) }
        } else {
            runnable.run()
        }
    }
    onFinished?.invoke(desc)
}

val PsiMethod.isSetter: Boolean
    get() = dumbReadAction { name.startsWith("set") }
val PsiMethod.isGetterOrSetter
    get() = dumbReadAction { name.run { startsWith("set") || startsWith("get") || startsWith("is") } }
val PsiMethod.fieldOfGetterOrSetter
    get() = dumbReadAction { if (isSetter) getFieldOfSetter(this) else getFieldOfGetter(this) }

fun computeTime(startTime: Long): String {
    val time = System.currentTimeMillis() - startTime
    val days = TimeUnit.MILLISECONDS.toDays(time)
    val toHours = TimeUnit.MILLISECONDS.toHours(time)
    val toMinutes = TimeUnit.MILLISECONDS.toMinutes(time)
    val toSeconds = TimeUnit.MILLISECONDS.toSeconds(time)
    val hours = toHours - TimeUnit.DAYS.toHours(days)
    val minutes = toMinutes - TimeUnit.HOURS.toMinutes(toHours)
    val seconds = toSeconds - TimeUnit.MINUTES.toSeconds(toMinutes)
    val strBuilder = StringBuilder()
    if (days > 0) strBuilder.append(days).append(" d ")
    if (hours > 0) strBuilder.append(hours).append(" h ")
    if (minutes > 0) strBuilder.append(minutes).append(" m ")
    if (seconds >= 0) strBuilder.append(seconds).append(" s ")
    return strBuilder.toString()
}