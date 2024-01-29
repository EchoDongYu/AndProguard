package com.murphy.core

import com.intellij.psi.PsiField
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.impl.source.*
import com.intellij.psi.impl.source.tree.java.PsiLocalVariableImpl
import com.murphy.util.SKIP_DATA
import com.murphy.util.randomClassName
import com.murphy.util.randomFieldName
import com.murphy.util.randomMethodName
import org.jetbrains.kotlin.j2k.isMainMethod

fun processJava(psi: PsiNamedElement) {
    println("============================== ${psi.name} ==============================")
    val elementSeq = psi.childrenDfsSequence().filterIsInstance<PsiNamedElement>()
    elementSeq.partition { it is PsiFieldImpl }.run {
        val skipElements: MutableSet<PsiField> = HashSet()
        second.forEach {
            when (it) {
                is PsiAnonymousClassImpl -> return@forEach
                is PsiClassImpl -> {
                    it.renameReference()
                    it.rename(randomClassName, "Class")
                }

                is PsiMethodImpl -> {
                    val getterOrSetter = it.isGetterOrSetter()
                    if (SKIP_DATA && getterOrSetter) it.getFieldOfGetterOrSetter()?.let { e -> skipElements.add(e) }
                    if (!getterOrSetter && it.findSuperMethods().isEmpty() && !it.isConstructor && !it.isMainMethod())
                        it.rename(randomMethodName, "Method")
                }

                is PsiParameterImpl -> it.rename(randomFieldName, "Parameter")
                is PsiLocalVariableImpl -> it.rename(randomFieldName, "Variable")
            }
        }
        first.subtract(skipElements).forEach {
            it.rename(randomFieldName, "Field")
        }
    }
}