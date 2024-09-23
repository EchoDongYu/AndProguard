package com.murphy.core

import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.psi.PsiNamedElement
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtProperty
import kotlin.collections.filterIsInstance

object KotlinPreGenerator : AbstractGenerator() {
    override val name: String get() = "KotlinPre"

    override fun process(list: List<PsiNamedElement>, indicator: ProgressIndicator) {
        indicator.fraction = 0.0
        indicator.text = "Refactor $name..."
        val skipElements: MutableSet<KtParameter> = HashSet()
        if (skipData) {
            list.filterIsInstance<KtClass>().filter { runReadAction { it.isData() } }.alsoReset().forEach {
                skipElements.addAll(it.primaryConstructorParameters)
                indicator.increase("Find skipElements")
            }
        }
        if (config.fieldRule.isNotEmpty()) {
            list.filterIsInstance<KtProperty>().alsoReset().forEach {
                if (runReadAction { !it.hasModifier(KtTokens.OVERRIDE_KEYWORD) })
                    it.rename(config.randomFieldName, "Property")
                indicator.increase("Property")
            }
            list.filterIsInstance<KtParameter>().alsoReset().forEach {
                if (runReadAction { !it.hasModifier(KtTokens.OVERRIDE_KEYWORD) && !skipElements.contains(it) })
                    it.rename(config.randomFieldName, "Parameter")
                indicator.increase("Parameter")
            }
        }
        skipElements.clear()
    }
}