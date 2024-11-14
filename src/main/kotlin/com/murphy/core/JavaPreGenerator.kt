package com.murphy.core

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiField
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.impl.source.PsiFieldImpl
import com.intellij.psi.impl.source.PsiMethodImpl
import com.intellij.psi.impl.source.PsiParameterImpl
import com.intellij.psi.impl.source.tree.java.PsiLocalVariableImpl

object JavaPreGenerator : BatchGenerator() {
    override val name: String get() = "JavaPre"

    override fun process(first: Project, second: ProgressIndicator, data: List<PsiNamedElement>) {
        super.process(first, second, data)
        val skipElements: MutableSet<PsiField> = HashSet()
        if (config.skipData) {
            data.psiFilter<PsiMethodImpl> { it.isGetterOrSetter }
                .alsoReset("FindBean")
                .forEach {
                    service.dumbReadAction { it.fieldOfGetterOrSetter }?.run { skipElements.add(this) }
                    increase()
                }
        }
        if (config.propertyRule.isNotEmpty()) {
            data.psiFilter<PsiParameterImpl>().renameEach(RefactorType.PsiParameter)
            data.psiFilter<PsiFieldImpl> { !skipElements.contains(it) }.renameEach(RefactorType.PsiField)
            data.psiFilter<PsiLocalVariableImpl>().renameEach(RefactorType.PsiVariable)
        }
        skipElements.clear()
    }
}