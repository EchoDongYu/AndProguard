package com.murphy.core

import com.intellij.psi.PsiField
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.impl.source.*
import com.intellij.psi.impl.source.tree.java.PsiLocalVariableImpl
import com.murphy.config.AndGuardCoinfigState
import org.jetbrains.kotlin.j2k.isMainMethod

fun processJava(psi: PsiNamedElement) {
    println("============================== ${psi.name} ==============================")
    val config = AndGuardCoinfigState.getInstance()
    val elementSeq = psi.childrenDfsSequence().filterIsInstance<PsiNamedElement>()
    elementSeq.partition { it is PsiFieldImpl }.run {
        val skipElements: MutableSet<PsiField> = HashSet()
        second.forEach {
            when (it) {
                is PsiAnonymousClassImpl -> return@forEach
                is PsiClassImpl -> {
                    it.renameReference()
                    it.rename(config.randomClassName, "Class")
                }

                is PsiMethodImpl -> {
                    val skip = config.skipData && it.isGetterOrSetter()
                    if (skip) it.getFieldOfGetterOrSetter()?.let { e -> skipElements.add(e) }
                    if (!skip && it.findSuperMethods().isEmpty() && !it.isConstructor && !it.isMainMethod())
                        it.rename(config.randomMethodName, "Method")
                }

                is PsiParameterImpl -> it.rename(config.randomFieldName, "Parameter")
                is PsiLocalVariableImpl -> it.rename(config.randomFieldName, "Variable")
            }
        }
        first.subtract(skipElements).forEach {
            it.rename(config.randomFieldName, "Field")
        }
    }
}