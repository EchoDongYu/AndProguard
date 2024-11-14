package com.murphy.core

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import org.jetbrains.kotlin.idea.core.getPackage

object DirectoryGenerator : AbstractGenerator<PsiDirectory>() {
    override val name: String get() = "Directory"

    override fun process(first: Project, second: ProgressIndicator, data: List<PsiDirectory>) {
        super.process(first, second, data)
        if (config.directoryRule.isNotEmpty()) {
            data.mapNotNull { service.dumbReadAction { it.getPackage() } }
                .renameEach(RefactorType.PsiDirectory)
        }
    }
}