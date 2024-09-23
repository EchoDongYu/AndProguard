package com.murphy.core

import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.psi.PsiNamedElement
import com.murphy.util.KOTLIN_SUFFIX
import org.jetbrains.kotlin.idea.isMainFunction
import org.jetbrains.kotlin.idea.util.isAnonymousFunction
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*
import kotlin.collections.filterIsInstance

object KotlinGenerator : AbstractGenerator() {
    override val name: String get() = "Kotlin"

    override fun process(list: List<PsiNamedElement>, indicator: ProgressIndicator) {
        indicator.fraction = 0.0
        indicator.text = "Refactor $name..."
        if (config.methodRule.isNotEmpty()) {
            list.filterIsInstance<KtNamedFunction>().alsoReset().forEach {
                if (runReadAction { !it.hasModifier(KtTokens.OVERRIDE_KEYWORD) && !it.isMainFunction() && !it.isAnonymousFunction })
                    it.rename(config.randomMethodName, "Function")
                indicator.increase("Function")
            }
        }
        if (config.classRule.isNotEmpty()) {
            list.filterIsInstance<KtObjectDeclaration>().alsoReset().forEach {
                if (runReadAction { !it.isObjectLiteral() && !it.isCompanion() })
                    it.rename(config.randomClassName, "Object")
                indicator.increase("Object")
            }
            list.filterIsInstance<KtClass>().alsoReset().forEach {
                it.rename(config.randomClassName, "Class")
                indicator.increase("Class")
            }
            list.filterIsInstance<KtFile>().alsoReset().forEach {
                if (runReadAction { it.classes.size != 1 || it.hasTopLevelCallables() })
                    it.rename(config.randomClassName + KOTLIN_SUFFIX, "File")
                indicator.increase("File")
            }
        }
    }
}
