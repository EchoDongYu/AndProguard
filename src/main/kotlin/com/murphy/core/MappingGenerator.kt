package com.murphy.core

import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.impl.source.PsiFieldImpl
import com.intellij.psi.impl.source.PsiMethodImpl
import com.intellij.psi.impl.source.PsiParameterImpl
import com.murphy.util.randomFieldName
import org.jetbrains.kotlin.psi.KtClass

class MappingGenerator(private val nameMap: Map<String, String>) {
    fun processMapping(psi: PsiNamedElement) {
        println("============================== ${psi.name} ==============================")
        psi.childrenDfsSequence().filterIsInstance<PsiNamedElement>()
            .partition { it is PsiFieldImpl }
            .run {
                second.forEach {
                    when (it) {
                        is PsiMethodImpl -> processJavaMethod(it)
                        is KtClass -> processKtClass(it)
                        is PsiParameterImpl -> {
                            if (nameMap.contains(it.name)) {
                                it.rename(randomFieldName, "Parameter")
                            }
                        }
                    }
                }
                first.forEach { processJavaField(it) }
            }
    }

    private fun processKtClass(psi: KtClass) {
        if (!psi.isData() || psi.primaryConstructorParameters.isEmpty()) return
        psi.primaryConstructorParameters.forEach {
            if (!nameMap.contains(it.name)) return@forEach
            val newName = nameMap[it.name] ?: return@forEach
            it.rename(newName, "Parameter")
        }
    }

    private fun processJavaMethod(psi: PsiMethodImpl) {
        if (!psi.isGetterOrSetter() || psi.findSuperMethods().isNotEmpty() || psi.isConstructor) return
        val name = psi.getFieldOfGetterOrSetter()?.name ?: return
        if (!nameMap.contains(name)) return
        val newName = StringUtil.capitalize(nameMap[name] ?: return)
        val newMethodName = psi.name.let {
            if (it.startsWith("set")) "set$newName"
            else if (it.startsWith("get")) "get$newName"
            else if (it.startsWith("is")) "is$newName"
            else null
        } ?: return
        psi.rename(newMethodName, "Method")
    }

    private fun processJavaField(psi: PsiNamedElement) {
        if (psi !is PsiFieldImpl || !nameMap.contains(psi.name)) return
        val newName = nameMap[psi.name] ?: return
        psi.rename(newName, "Field")
    }
}