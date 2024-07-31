package com.murphy.core

import com.intellij.psi.PsiNamedElement
import com.murphy.config.AndProguardConfigState
import com.murphy.util.KOTLIN_SUFFIX
import org.jetbrains.kotlin.idea.isMainFunction
import org.jetbrains.kotlin.idea.util.isAnonymousFunction
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*

fun processKotlin(psi: PsiNamedElement) {
    println("============================== ${psi.name} ==============================")
    val config = AndProguardConfigState.getInstance()
    val skipElements: MutableSet<KtParameter> = HashSet()
    psi.childrenDfsSequence().filterIsInstance<KtClass>().forEach {
        if (config.skipData && it.isData())
            skipElements.addAll(it.primaryConstructorParameters)
    }
    psi.childrenDfsSequence().filterIsInstance<PsiNamedElement>().toList().reversed().forEach {
        if (it.isValid.not()) return@forEach
        when (it) {
            is KtFile -> {
                if (it.classes.size != 1 || it.hasTopLevelCallables())
                    it.rename(config.randomClassName + KOTLIN_SUFFIX, "File")
            }

            is KtClass -> {
                it.renameReference()
                it.rename(config.randomClassName, "Class")
            }

            is KtObjectDeclaration -> {
                if (!it.isObjectLiteral() && !it.isCompanion())
                    it.rename(config.randomClassName, "Object Class")
            }

            is KtNamedFunction -> {
                if (!it.hasModifier(KtTokens.OVERRIDE_KEYWORD) && !it.isMainFunction() && !it.isAnonymousFunction)
                    it.rename(config.randomMethodName, "Function")
            }

            is KtProperty -> {
                if (!it.hasModifier(KtTokens.OVERRIDE_KEYWORD))
                    it.rename(config.randomFieldName, "Property")
            }

            is KtParameter -> {
                if (!it.hasModifier(KtTokens.OVERRIDE_KEYWORD) && !skipElements.contains(it))
                    it.rename(config.randomFieldName, "Parameter")
            }
        }
    }
}