package com.murphy.core

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.psi.PsiBinaryFile
import com.intellij.psi.PsiNamedElement
import kotlin.collections.filterIsInstance

object BinaryFileGenerator : AbstractGenerator() {
    override val name: String get() = "BinaryFile"

    override fun process(list: List<PsiNamedElement>, indicator: ProgressIndicator) {
        indicator.fraction = 0.0
        indicator.text = "Refactor $name..."
        if (config.fileResRule.isNotEmpty()) {
            list.filterIsInstance<PsiBinaryFile>().alsoReset().forEach {
                it.rename(config.randomFileResName, "BinaryFile")
                indicator.increase("BinaryFile")
            }
        }
    }
}