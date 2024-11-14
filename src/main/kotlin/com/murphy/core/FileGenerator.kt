package com.murphy.core

import com.intellij.json.psi.JsonFile
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiBinaryFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiNamedElement

object FileGenerator : BatchGenerator() {
    override val name: String get() = "FileResource"

    override fun process(first: Project, second: ProgressIndicator, data: List<PsiNamedElement>) {
        super.process(first, second, data)
        if (config.resFileRule.isNotEmpty()) {
            data.psiFilter<PsiFile> { it is PsiBinaryFile || it is JsonFile }
                .distinctBy { service.dumbReadAction { it.name /* 包含文件后缀 */ } }
                .renameEach(RefactorType.PsiResourceFile)
        }
    }
}