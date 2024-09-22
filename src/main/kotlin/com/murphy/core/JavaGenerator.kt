package com.murphy.core

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.psi.PsiField
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.impl.source.*
import com.intellij.psi.impl.source.tree.java.PsiLocalVariableImpl
import com.murphy.config.AndProguardConfigState
import org.jetbrains.kotlin.j2k.isMainMethod

fun Sequence<PsiNamedElement>.processJava(indicator: ProgressIndicator) {
    indicator.fraction = 0.0
    indicator.text = "Refactor java..."
    var count = 0
    var total = 0
    fun ProgressIndicator.increase(label: String) {
        fraction = ++count / total.toDouble()
        text = "Java $count of $total [$label]"
    }

    fun <T> Sequence<T>.alsoReset() = also {
        it.count().takeIf { it > 0 }?.let {
            count = 0
            total = it
        }
    }

    val config = AndProguardConfigState.getInstance()
    val skipElements: MutableSet<PsiField> = HashSet()
    val skipData = config.skipData
    if (skipData) {
        filterIsInstance<PsiMethodImpl>().filter { it.isGetterOrSetter() }.alsoReset().forEach {
            it.getFieldOfGetterOrSetter()?.run { skipElements.add(this) }
            indicator.increase("Find skipElements")
        }
    }
    if (config.fieldRule.isNotEmpty()) {
        filterIsInstance<PsiFieldImpl>().alsoReset().forEach {
            if (!skipElements.contains(it))
                it.rename(config.randomFieldName, "Field")
            indicator.increase("Field")
        }
        filterIsInstance<PsiLocalVariableImpl>().alsoReset().forEach {
            it.rename(config.randomFieldName, "Variable")
            indicator.increase("Variable")
        }
        filterIsInstance<PsiParameterImpl>().alsoReset().forEach {
            it.rename(config.randomFieldName, "Parameter")
            indicator.increase("Parameter")
        }
    }
    if (config.methodRule.isNotEmpty()) {
        filterIsInstance<PsiMethodImpl>().alsoReset().forEach {
            val skip = skipData && it.isGetterOrSetter()
            if (!skip && it.findSuperMethods().isEmpty() && !it.isConstructor && !it.isMainMethod())
                it.rename(config.randomMethodName, "Method")
            indicator.increase("Method")
        }
    }
    if (config.classRule.isNotEmpty()) {
        filterIsInstance<PsiClassImpl>().alsoReset().forEach {
            if (it !is PsiAnonymousClassImpl)
                it.rename(config.randomClassName, "Class")
            indicator.increase("Class")
        }
    }
}