package com.murphy.core

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiNamedElement
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtProperty
import kotlin.collections.filterIsInstance

object KotlinPreGenerator : AbstractGenerator() {
    override val name: String get() = "KotlinPre"

    override fun process(project: Project, list: List<PsiNamedElement>, indicator: ProgressIndicator) {
        indicator.fraction = 0.001
        indicator.text = "Refactor $name..."
        val dService = DumbService.getInstance(project)
        val skipElements: MutableSet<KtParameter> = HashSet()
        if (skipData) {
            list.filterIsInstance<KtClass>().filter { dService.dumbReadAction { it.isData() } }.alsoReset().forEach {
                skipElements.addAll(it.primaryConstructorParameters)
            }
        }
        if (config.fieldRule.isNotEmpty()) {
            list.filterIsInstance<KtProperty>().alsoReset().forEach {
                if (dService.dumbReadAction { !it.hasModifier(KtTokens.OVERRIDE_KEYWORD) })
                    it.rename(config.randomFieldName, "Property", indicator.increase)
            }
            list.filterIsInstance<KtParameter>().alsoReset().forEach {
                if (dService.dumbReadAction { !it.hasModifier(KtTokens.OVERRIDE_KEYWORD) && !skipElements.contains(it) })
                    it.rename(config.randomFieldName, "Parameter", indicator.increase)
            }
        }
        skipElements.clear()
    }
}