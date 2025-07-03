package com.murphy.core

import com.intellij.psi.PsiMethod
import com.intellij.psi.impl.getFieldOfGetter
import com.intellij.psi.impl.getFieldOfSetter
import java.util.concurrent.TimeUnit

private val PsiMethod.isSetter: Boolean
    get() = name.startsWith("set")
val PsiMethod.isGetterOrSetter
    get() = name.run { startsWith("set") || startsWith("get") || startsWith("is") }
val PsiMethod.fieldOfGetterOrSetter
    get() = if (isSetter) getFieldOfSetter(this) else getFieldOfGetter(this)

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