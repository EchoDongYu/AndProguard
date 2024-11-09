package com.murphy.core

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiBinaryFile
import com.intellij.psi.PsiNamedElement

object BinaryFileGenerator : AbstractGenerator() {
    override val name: String get() = "BinaryFile"

    override fun process(first: Project, second: ProgressIndicator, data: List<PsiNamedElement>) {
        super.process(first, second, data)
        if (config.resFileRule.isNotEmpty()) {
            data.psiFilter<PsiBinaryFile>()
                .distinctBy { service.dumbReadAction { it.name } }
                .renameEach(RefactorType.PsiBinaryFile)
        }
    }
}