package com.murphy.core

import com.intellij.psi.PsiNamedElement
import com.murphy.util.*
import org.jetbrains.kotlin.idea.isMainFunction
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*

fun processKotlin(psi: PsiNamedElement) {
    println("============================== ${psi.name} ==============================")
    val elementSeq = psi.childrenDfsSequence().filterIsInstance<PsiNamedElement>()
    elementSeq.partition { it is KtParameter }.run {
        val skipElements: MutableSet<KtParameter> = HashSet()
        second.forEach {
            when (it) {
                is KtFile -> {
                    if (it.classes.size != 1 || it.hasTopLevelCallables())
                        it.rename(randomClassName + KOTLIN_SUFFIX, "File")
                }

                is KtClass -> {
                    if (SKIP_DATA && it.isData()) skipElements.addAll(it.primaryConstructorParameters)
                    it.renameReference()
                    it.rename(randomClassName, "Class")
                }

                is KtNamedFunction -> {
                    if (!it.hasModifier(KtTokens.OVERRIDE_KEYWORD) && !it.isMainFunction() && !it.isAnonymous)
                        it.rename(randomMethodName, "Function")
                }

                is KtProperty -> {
                    if (!it.hasModifier(KtTokens.OVERRIDE_KEYWORD))
                        it.rename(randomFieldName, "Property")
                }
            }
        }
        first.subtract(skipElements).forEach {
            if (!(it as KtParameter).hasModifier(KtTokens.OVERRIDE_KEYWORD))
                it.rename(randomFieldName, "Parameter")
        }
    }
}