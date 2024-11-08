package com.murphy.core

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiNamedElement

object FolderGenerator : AbstractGenerator() {
    override val name: String get() = "Folder"

    override fun process(project: Project, list: List<PsiNamedElement>, indicator: ProgressIndicator) {
        indicator.fraction = 0.001
        indicator.text = "Refactor $name..."
        if (config.folderRule.isNotEmpty()) {
            list.filterIsInstance<PsiDirectory>().alsoReset().forEach {
                it.rename(config.randomFolderName, "Folder", indicator.increase)
            }
        }
    }
}