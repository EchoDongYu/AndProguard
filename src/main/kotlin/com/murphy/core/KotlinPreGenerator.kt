package com.murphy.core

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiNamedElement
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtVariableDeclaration

object KotlinPreGenerator : BatchGenerator() {
    override val name: String get() = "KotlinPre"

    override fun process(first: Project, second: ProgressIndicator, data: List<PsiNamedElement>) {
        super.process(first, second, data)
        val skipElements: MutableSet<KtParameter> = HashSet()
        if (config.skipData) {
            data.psiFilter<KtClass> { it.isData() }
                .alsoReset("FindData")
                .forEach {
                    service.dumbReadAction { it.primaryConstructorParameters }.run { skipElements.addAll(this) }
                    increase()
                }
        }
        if (config.propertyRule.isNotEmpty()) {
            data.psiFilter<KtVariableDeclaration> { !it.hasModifier(KtTokens.OVERRIDE_KEYWORD) }
                .renameEach(RefactorType.KtVariable)
            data.psiFilter<KtParameter> {
                !it.hasModifier(KtTokens.OVERRIDE_KEYWORD) && !skipElements.contains(it)
            }.renameEach(RefactorType.KtParameter)
        }
        skipElements.clear()
    }
}