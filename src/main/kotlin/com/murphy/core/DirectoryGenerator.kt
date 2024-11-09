package com.murphy.core

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiNamedElement

object DirectoryGenerator : AbstractGenerator() {
    override val name: String get() = "Directory"

    override fun process(first: Project, second: ProgressIndicator, data: List<PsiNamedElement>) {
        super.process(first, second, data)
        if (config.directoryRule.isNotEmpty()) {
            data.psiFilter<PsiDirectory>().renameEach(RefactorType.PsiDirectory)
        }
    }
}