package com.murphy.core

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.impl.source.PsiFieldImpl
import com.intellij.psi.impl.source.tree.java.PsiLiteralExpressionImpl
import com.intellij.psi.util.PropertyUtilBase.*
import com.intellij.psi.util.PsiLiteralUtil.getStringLiteralContent
import com.murphy.util.LogUtil
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import org.jetbrains.kotlin.psi.psiUtil.plainContent

class RenamableBeanElement(
    override val element: PsiElement,
    override val currentName: String?,
    val newName: String
) : RenamableElement<PsiElement> {
    override val namingIndex: Int? = -1

    override fun performRename(project: Project, name: String?) {
        if (element is PsiLanguageInjectionHost) {
            LogUtil.info(project, "[${element.javaClass.simpleName}] $currentName >>> $newName")
            element.updateText("\"$newName\"")
        } else {
            runRename(project, element, newName)
        }
    }

    companion object {
        fun findElements(map: Map<String, String>, sequence: Sequence<PsiElement>): List<RenamableBeanElement> {
            return sequence.mapNotNull { psi ->
                when (psi) {
                    is PsiFieldImpl -> {
                        val oldName = psi.name
                        val newName = map[oldName] ?: return@mapNotNull null
                        buildList {
                            add(RenamableBeanElement(psi, oldName, newName))
                            findSetterForField(psi)?.let {
                                add(RenamableBeanElement(it, it.name, suggestSetterName(newName)))
                            }
                            findGetterForField(psi)?.let {
                                add(RenamableBeanElement(it, it.name, suggestGetterName(newName, psi.type)))
                            }
                        }
                    }

                    is KtClass -> {
                        val element: List<PsiNamedElement> = psi.getProperties() + psi.getPrimaryConstructorParameters()
                        element.mapNotNull {
                            val oldName = it.name ?: return@mapNotNull null
                            val newName = map[oldName] ?: return@mapNotNull null
                            RenamableBeanElement(it, oldName, newName)
                        }
                    }

                    is PsiLiteralExpressionImpl, is KtStringTemplateExpression -> {
                        val content = when (psi) {
                            is PsiLiteralExpressionImpl -> getStringLiteralContent(psi)
                            is KtStringTemplateExpression -> psi.plainContent
                            else -> null
                        } ?: return@mapNotNull null
                        val newName = map[content] ?: return@mapNotNull null
                        listOf(RenamableBeanElement(psi, content, newName))
                    }

                    else -> null
                }
            }.flatten().toList()
        }
    }
}