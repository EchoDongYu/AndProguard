package com.murphy.core

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.psi.PsiNamedElement
import com.murphy.config.AndProguardConfigState
import com.murphy.util.KOTLIN_SUFFIX
import org.jetbrains.kotlin.idea.isMainFunction
import org.jetbrains.kotlin.idea.util.isAnonymousFunction
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*
import kotlin.sequences.forEach

fun Sequence<PsiNamedElement>.processKotlin(indicator: ProgressIndicator) {
    indicator.fraction = 0.0
    indicator.text = "Refactor kotlin..."
    val namedDfs = filterIsInstance<KtNamedDeclaration>()
    val fileDfs = filterIsInstance<KtFile>()
    val classDfs = namedDfs.filterIsInstance<KtClass>()
    var count = 0
    var total = 0
    fun ProgressIndicator.increase(label: String) {
        fraction = ++count / total.toDouble()
        text = "Kotlin $count of $total [$label]"
    }

    fun <T> Sequence<T>.alsoReset() = also {
        it.count().takeIf { it > 0 }?.let {
            count = 0
            total = it
        }
    }

    val config = AndProguardConfigState.getInstance()
    val skipElements: MutableSet<KtParameter> = HashSet()
    if (config.skipData) {
        classDfs.filter { it.isData() }.alsoReset().forEach {
            skipElements.addAll(it.primaryConstructorParameters)
            indicator.increase("Find skipElements")
        }
    }
    if (config.fieldRule.isNotEmpty()) {
        namedDfs.filterIsInstance<KtProperty>().alsoReset().forEach {
            if (!it.hasModifier(KtTokens.OVERRIDE_KEYWORD))
                it.rename(config.randomFieldName, "Property")
            indicator.increase("Property")
        }
        namedDfs.filterIsInstance<KtParameter>().alsoReset().forEach {
            if (!it.hasModifier(KtTokens.OVERRIDE_KEYWORD) && !skipElements.contains(it))
                it.rename(config.randomFieldName, "Parameter")
            indicator.increase("Parameter")
        }
    }
    if (config.methodRule.isNotEmpty()) {
        namedDfs.filterIsInstance<KtNamedFunction>().alsoReset().forEach {
            if (!it.hasModifier(KtTokens.OVERRIDE_KEYWORD) && !it.isMainFunction() && !it.isAnonymousFunction)
                it.rename(config.randomMethodName, "Function")
            indicator.increase("Function")
        }
    }
    if (config.classRule.isNotEmpty()) {
        namedDfs.filterIsInstance<KtObjectDeclaration>().alsoReset().forEach {
            if (!it.isObjectLiteral() && !it.isCompanion())
                it.rename(config.randomClassName, "Object")
            indicator.increase("Object")
        }
        classDfs.alsoReset().forEach {
            it.rename(config.randomClassName, "Class")
            indicator.increase("Class")
        }
        fileDfs.alsoReset().forEach {
            if (it.classes.size != 1 || it.hasTopLevelCallables())
                it.rename(config.randomClassName + KOTLIN_SUFFIX, "File")
            indicator.increase("File")
        }
    }
}