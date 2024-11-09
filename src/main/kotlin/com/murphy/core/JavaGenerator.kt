package com.murphy.core

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.impl.source.*
import org.jetbrains.kotlin.j2k.isMainMethod
import kotlin.collections.filterIsInstance

object JavaGenerator : AbstractGenerator() {
    override val name: String get() = "Java"

    override fun process(project: Project, list: List<PsiNamedElement>, indicator: ProgressIndicator) {
        indicator.fraction = 0.001
        indicator.text = "Refactor $name..."
        if (config.functionRule.isNotEmpty()) {
            list.filterIsInstance<PsiMethodImpl>().alsoReset().forEach {
                val skip = skipData && it.isGetterOrSetter
                val canRefactor = project.dumbReadAction {
                    it.findSuperMethods().isEmpty() && !it.isConstructor && !it.isMainMethod()
                }
                if (!skip && canRefactor)
                    it.rename(config.randomFunctionName, "Method", indicator.increase)
            }
        }
        if (config.classRule.isNotEmpty()) {
            list.filterIsInstance<PsiClassImpl>().alsoReset().forEach {
                if (it !is PsiAnonymousClassImpl)
                    it.rename(config.randomClassName, "Class", indicator.increase)
            }
        }
    }
}
