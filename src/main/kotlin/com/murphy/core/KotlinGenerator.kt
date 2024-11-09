package com.murphy.core

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiNamedElement
import org.jetbrains.kotlin.idea.isMainFunction
import org.jetbrains.kotlin.idea.util.isAnonymousFunction
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtObjectDeclaration

object KotlinGenerator : AbstractGenerator() {
    override val name: String get() = "Kotlin"

    override fun process(first: Project, second: ProgressIndicator, data: List<PsiNamedElement>) {
        super.process(first, second, data)
        if (config.classRule.isNotEmpty()) {
            data.psiFilter<KtObjectDeclaration> { !it.isObjectLiteral() && !it.isCompanion() }
                .renameEach(RefactorType.KtObject)
            data.psiFilter<KtClass>().renameEach(RefactorType.KtClass)
            data.psiFilter<KtFile> { it.classes.size != 1 || it.hasTopLevelCallables() }
                .renameEach(RefactorType.KtFile)
        }
        if (config.functionRule.isNotEmpty()) {
            data.psiFilter<KtNamedFunction> {
                !it.hasModifier(KtTokens.OVERRIDE_KEYWORD) && !it.isMainFunction() && !it.isAnonymousFunction
            }.renameEach(RefactorType.KtFunction)
        }
    }
}