package com.murphy.core

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiNamedElement
import com.murphy.config.AndConfigState

abstract class AbstractGenerator {
    protected val config by lazy { AndConfigState.getInstance() }
    protected val skipData get() = config.skipData
    abstract val name: String
    var count: Int = 0
    var total: Int = 0

    abstract fun process(project: Project, list: List<PsiNamedElement>, indicator: ProgressIndicator)

    protected val ProgressIndicator.increase: (String) -> Unit
        get() = {
            fraction = ++count / total.toDouble()
            text = "$name $count of $total [$it]"
        }

    fun <T> List<T>.alsoReset() = also {
        it.count().takeIf { it > 0 }?.let {
            count = 0
            total = it
        }
    }
}