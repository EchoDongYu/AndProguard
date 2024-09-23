package com.murphy.core

import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.impl.source.*
import org.jetbrains.kotlin.j2k.isMainMethod
import kotlin.collections.filterIsInstance

object JavaGenerator : AbstractGenerator() {
    override val name: String get() = "Java"

    override fun process(list: List<PsiNamedElement>, indicator: ProgressIndicator) {
        indicator.fraction = 0.0
        indicator.text = "Refactor $name..."
        if (config.methodRule.isNotEmpty()) {
            list.filterIsInstance<PsiMethodImpl>().alsoReset().forEach {
                val skip = skipData && it.isGetterOrSetter
                val canRefactor = runReadAction {
                    it.findSuperMethods().isEmpty() && !it.isConstructor && !it.isMainMethod()
                }
                if (!skip && canRefactor)
                    it.rename(config.randomMethodName, "Method")
                indicator.increase("Method")
            }
        }
        if (config.classRule.isNotEmpty()) {
            list.filterIsInstance<PsiClassImpl>().alsoReset().forEach {
                if (it !is PsiAnonymousClassImpl)
                    it.rename(config.randomClassName, "Class")
                indicator.increase("Class")
            }
        }
    }
}
