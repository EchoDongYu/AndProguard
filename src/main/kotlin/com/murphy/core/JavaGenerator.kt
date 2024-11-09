package com.murphy.core

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.impl.source.PsiAnonymousClassImpl
import com.intellij.psi.impl.source.PsiClassImpl
import com.intellij.psi.impl.source.PsiEnumConstantImpl
import com.intellij.psi.impl.source.PsiMethodImpl
import com.intellij.psi.util.PsiMethodUtil.isMainMethod

object JavaGenerator : AbstractGenerator() {
    override val name: String get() = "Java"

    override fun process(first: Project, second: ProgressIndicator, data: List<PsiNamedElement>) {
        super.process(first, second, data)
        if (config.functionRule.isNotEmpty()) {
            data.psiFilter<PsiMethodImpl> {
                val skip = config.skipData && it.isGetterOrSetter
                val allow = it.findSuperMethods().isEmpty() && !it.isConstructor && !isMainMethod(it)
                !skip && allow
            }.renameEach(RefactorType.PsiMethod)
        }
        if (config.classRule.isNotEmpty()) {
            data.psiFilter<PsiEnumConstantImpl>().renameEach(RefactorType.PsiEnumConstant)
            data.psiFilter<PsiClassImpl> { it !is PsiAnonymousClassImpl }
                .renameEach(RefactorType.PsiClass)
        }
    }
}