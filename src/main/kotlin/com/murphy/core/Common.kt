package com.murphy.core

import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.*
import com.intellij.psi.impl.getFieldOfGetter
import com.intellij.psi.impl.getFieldOfSetter
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.refactoring.RefactoringFactory
import java.util.concurrent.TimeUnit

fun PsiElement.childrenDfsSequence(): Sequence<PsiElement> =
    sequence {
        suspend fun SequenceScope<PsiElement>.visit(element: PsiElement) {
            element.children.forEach { visit(it) }
            yield(element)
        }
        visit(this@childrenDfsSequence)
    }

fun PsiNamedElement.rename(newName: String, desc: String) {
    if (!isValid) return
    println(String.format("[$desc] %s >>> %s", name, newName))
    ApplicationManager.getApplication().invokeAndWait {
        RefactoringFactory.getInstance(project)
            .createRename(this, newName, false, false)
            .run()
    }
}

fun XmlAttributeValue.rename(newName: String, desc: String) {
    if (!isValid) return
    println(String.format("[$desc] %s >>> %s", value, newName))
    ApplicationManager.getApplication().invokeAndWait {
        RefactoringFactory.getInstance(project)
            .createRename(this, newName, false, false)
            .run()
    }
}

fun PsiMethod.isSetter() = name.startsWith("set")
fun PsiMethod.isGetterOrSetter() = name.run { startsWith("set") || startsWith("get") || startsWith("is") }
fun PsiMethod.getFieldOfGetterOrSetter() = if (isSetter()) getFieldOfSetter(this) else getFieldOfGetter(this)

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