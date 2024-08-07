package com.murphy.core

import com.intellij.psi.PsiField
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.impl.source.*
import com.intellij.psi.impl.source.tree.java.PsiLocalVariableImpl
import com.murphy.config.AndProguardConfigState
import org.jetbrains.kotlin.j2k.isMainMethod

fun processJava(psi: PsiNamedElement) {
    println("============================== ${psi.name} ==============================")
    val config = AndProguardConfigState.getInstance()
    val skipElements: MutableSet<PsiField> = HashSet()
    psi.childrenDfsSequence().filterIsInstance<PsiMethodImpl>().forEach {
        val skip = config.skipData && it.isGetterOrSetter()
        if (skip) it.getFieldOfGetterOrSetter()?.let { e -> skipElements.add(e) }
    }
    psi.childrenDfsSequence().filterIsInstance<PsiNamedElement>().toList().reversed().forEach {
        if (it.isValid.not()) return@forEach
        when (it) {
            is PsiAnonymousClassImpl -> return@forEach
            is PsiClassImpl -> {
                it.renameReference()
                it.rename(config.randomClassName, "Class")
            }

            is PsiMethodImpl -> {
                val skip = config.skipData && it.isGetterOrSetter()
                if (!skip && it.findSuperMethods().isEmpty() && !it.isConstructor && !it.isMainMethod())
                    it.rename(config.randomMethodName, "Method")
            }

            is PsiParameterImpl -> it.rename(config.randomFieldName, "Parameter")
            is PsiLocalVariableImpl -> it.rename(config.randomFieldName, "Variable")
            is PsiFieldImpl -> if (!skipElements.contains(it)) it.rename(config.randomFieldName, "Field")
        }
    }
}