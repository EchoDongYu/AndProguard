package com.murphy.core

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiNamedElement
import com.murphy.util.KOTLIN_SUFFIX
import org.jetbrains.kotlin.idea.isMainFunction
import org.jetbrains.kotlin.idea.util.isAnonymousFunction
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*
import kotlin.collections.filterIsInstance

object KotlinGenerator : AbstractGenerator() {
    override val name: String get() = "Kotlin"

    override fun process(project: Project, list: List<PsiNamedElement>, indicator: ProgressIndicator) {
        indicator.fraction = 0.001
        indicator.text = "Refactor $name..."
        val dService = DumbService.getInstance(project)
        if (config.classRule.isNotEmpty()) {
            list.filterIsInstance<KtObjectDeclaration>().alsoReset().forEach {
                if (dService.dumbReadAction { !it.isObjectLiteral() && !it.isCompanion() })
                    it.rename(config.randomClassName, "Object", indicator.increase)
            }
            list.filterIsInstance<KtClass>().alsoReset().forEach {
                it.rename(config.randomClassName, "Class", indicator.increase)
            }
            list.filterIsInstance<KtFile>().alsoReset().forEach {
                if (dService.dumbReadAction { it.classes.size != 1 || it.hasTopLevelCallables() })
                    it.rename(config.randomClassName + KOTLIN_SUFFIX, "File", indicator.increase)
            }
        }
        if (config.functionRule.isNotEmpty()) {
            list.filterIsInstance<KtNamedFunction>().alsoReset().forEach {
                if (dService.dumbReadAction { !it.hasModifier(KtTokens.OVERRIDE_KEYWORD) && !it.isMainFunction() && !it.isAnonymousFunction })
                    it.rename(config.randomFunctionName, "Function", indicator.increase)
            }
        }
    }
}
