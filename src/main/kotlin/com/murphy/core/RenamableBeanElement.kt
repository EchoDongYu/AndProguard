package com.murphy.core

import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.impl.source.PsiFieldImpl
import com.intellij.psi.impl.source.tree.java.PsiLiteralExpressionImpl
import com.intellij.psi.util.PropertyUtilBase.*
import com.intellij.psi.util.PsiLiteralUtil.getStringLiteralContent
import com.murphy.util.LogUtil
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import org.jetbrains.kotlin.psi.psiUtil.plainContent

class RenamableBeanElement(
    override val pointer: SmartPsiElementPointer<PsiElement>,
    override val currentName: String?,
    val newName: String
) : RenamableElement<PsiElement> {
    override val namingIndex: Int = -1

    override fun performRename(project: Project, name: String?) {
        val element = pointer.element ?: return
        if (element is PsiLanguageInjectionHost) {
            LogUtil.info(project, "[${element.javaClass.simpleName}] $currentName >>> $newName")
            element.updateText("\"$newName\"")
        } else {
            runRename(project, element, newName)
        }
    }

    companion object {
        fun findElements(
            project: Project,
            map: Map<String, String>,
            sequence: Sequence<PsiElement>
        ): List<RenamableBeanElement> {
            val pointerManager = SmartPointerManager.getInstance(project)

            fun renamableBeanElement(element: PsiElement, oldName: String?, newName: String): RenamableBeanElement {
                val pointer = pointerManager.createSmartPsiElementPointer(element)
                return RenamableBeanElement(pointer, oldName, newName)
            }

            return sequence.mapNotNull { psi ->
                when (psi) {
                    is PsiFieldImpl -> {
                        val oldName = psi.name
                        val newName = map[oldName] ?: return@mapNotNull null
                        buildList {
                            add(renamableBeanElement(psi, oldName, newName))
                            findSetterForField(psi)?.let {
                                add(renamableBeanElement(it, it.name, suggestSetterName(newName)))
                            }
                            findGetterForField(psi)?.let {
                                add(renamableBeanElement(it, it.name, suggestGetterName(newName, psi.type)))
                            }
                        }
                    }

                    is KtClass -> {
                        val element: List<PsiNamedElement> = psi.getProperties() + psi.getPrimaryConstructorParameters()
                        element.mapNotNull {
                            val oldName = it.name ?: return@mapNotNull null
                            val newName = map[oldName] ?: return@mapNotNull null
                            renamableBeanElement(it, oldName, newName)
                        }
                    }

                    is PsiLiteralExpressionImpl, is KtStringTemplateExpression -> {
                        val content = when (psi) {
                            is PsiLiteralExpressionImpl -> getStringLiteralContent(psi)
                            is KtStringTemplateExpression -> psi.plainContent
                            else -> null
                        } ?: return@mapNotNull null
                        val newName = map[content] ?: return@mapNotNull null
                        listOf(renamableBeanElement(psi, content, newName))
                    }

                    else -> null
                }
            }.flatten().toList()
        }
    }
}