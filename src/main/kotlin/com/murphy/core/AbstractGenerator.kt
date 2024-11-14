package com.murphy.core

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.xml.XmlAttributeValue
import com.murphy.config.AndConfigState

abstract class AbstractGenerator<T> {
    protected lateinit var myProject: Project
    private lateinit var indicator: ProgressIndicator
    protected val scope by lazy { GlobalSearchScope.projectScope(myProject) }
    protected val service by lazy { DumbService.getInstance(myProject) }
    protected val config by lazy { AndConfigState.getInstance() }
    protected abstract val name: String
    private lateinit var label: String
    private var count: Int = 0
    private var total: Int = 0

    open fun process(first: Project, second: ProgressIndicator, data: List<T>) {
        myProject = first
        indicator = second.apply {
            fraction = 0.0
            text = "Refactor $name..."
        }
    }

    protected fun increase() {
        indicator.fraction = ++count / total.toDouble()
        indicator.text = "$name $count of $total [$label]"
    }

    protected fun <T> List<T>.alsoReset(name: String) = also {
        it.count().takeIf { it > 0 }?.let {
            count = 0
            total = it
            label = name
        }
    }

    protected inline fun <reified T> List<PsiElement>.psiFilter(
        noinline predicate: ((T) -> Boolean)? = null
    ) = service.dumbReadAction {
        if (predicate == null) filterIsInstance<T>()
        else filterIsInstance<T>().filter(predicate)
    }

    protected fun List<PsiNamedElement>.renameEach(type: RefactorType) {
        alsoReset(type.name).forEach {
            it.rename(type.randomName(config), type.name)
            increase()
        }
    }

    fun PsiNamedElement.rename(newName: String, desc: String) = rename(newName, desc, myProject, service)

    fun XmlAttributeValue.renameX(newName: String, desc: String) = renameX(newName, desc, myProject, service)
}