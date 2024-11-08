package com.murphy.core

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiField
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.impl.source.PsiFieldImpl
import com.intellij.psi.impl.source.PsiMethodImpl
import com.intellij.psi.impl.source.PsiParameterImpl
import com.intellij.psi.impl.source.tree.java.PsiLocalVariableImpl
import kotlin.collections.filterIsInstance

object JavaPreGenerator : AbstractGenerator() {
    override val name: String get() = "JavaPre"

    override fun process(project: Project, list: List<PsiNamedElement>, indicator: ProgressIndicator) {
        indicator.fraction = 0.001
        indicator.text = "Refactor $name..."
        val skipElements: MutableSet<PsiField> = HashSet()
        if (skipData) {
            list.filterIsInstance<PsiMethodImpl>().filter { it.isGetterOrSetter }.alsoReset().forEach {
                it.fieldOfGetterOrSetter?.run { skipElements.add(this) }
            }
        }
        if (config.fieldRule.isNotEmpty()) {
            list.filterIsInstance<PsiParameterImpl>().alsoReset().forEach {
                it.rename(config.randomFieldName, "Parameter", indicator.increase)
            }
            list.filterIsInstance<PsiFieldImpl>().alsoReset().forEach {
                if (!skipElements.contains(it))
                    it.rename(config.randomFieldName, "Field", indicator.increase)
            }
            list.filterIsInstance<PsiLocalVariableImpl>().alsoReset().forEach {
                it.rename(config.randomFieldName, "Variable", indicator.increase)
            }
        }
        skipElements.clear()
    }
}