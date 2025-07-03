package com.murphy.core

import com.intellij.json.psi.JsonFile
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiAnonymousClass
import com.intellij.psi.PsiBinaryFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.impl.source.*
import com.intellij.psi.impl.source.tree.java.PsiLocalVariableImpl
import com.intellij.psi.util.PsiMethodUtil.isMainMethod
import com.murphy.action.CustomCheck
import org.jetbrains.kotlin.idea.isMainFunction
import org.jetbrains.kotlin.idea.util.isAnonymousFunction
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*

class RenamableCodeElement(
    override val element: PsiNamedElement,
    override val currentName: String?,
    val prohibit: Boolean,
    val priority: Int
) : RenamableElement<PsiNamedElement> {
    override val namingIndex: Int? = when (element) {
        is KtFile, is KtClass,
        is KtObjectDeclaration,
        is PsiClassImpl,
        is PsiEnumConstantImpl -> 0

        is KtNamedFunction,
        is PsiMethodImpl -> 1

        is KtVariableDeclaration,
        is KtParameter,
        is PsiParameterImpl,
        is PsiFieldImpl,
        is PsiLocalVariableImpl -> 2

        is PsiBinaryFile, is JsonFile -> 4
        is PsiDirectory -> 5
        else -> null
    }

    override fun performRename(project: Project, name: String?) {
        val newName = name?.let { if (element is KtFile) "$it.kt" else it } ?: return
        runRename(project, element, newName)
    }

    companion object {
        fun findElements(
            skipData: Boolean,
            check: CustomCheck,
            sequence: Sequence<PsiNamedElement>
        ): List<RenamableCodeElement> {
            val skipElements = sequence.findSkipElements()
            val targetElements = if (skipData) sequence - skipElements.toSet() else sequence
            return targetElements.map { it.toRenamableCodeElement(skipData, check) }
                .filterNot { it.currentName == null || it.prohibit || it.priority < 0 }
                .sortedByDescending { it.priority }
                .distinctBy { if (it.element is PsiBinaryFile) it.currentName else it }
                .toList()
        }

        private fun Sequence<PsiNamedElement>.findSkipElements(): Sequence<PsiNamedElement> {
            val skipJava = filterIsInstance<PsiMethodImpl>()
                .filter { it.isGetterOrSetter }
                .mapNotNull { it.fieldOfGetterOrSetter }
            val skipKotlin = filterIsInstance<KtClass>()
                .filter { it.isData() }
                .map { it.primaryConstructorParameters }
                .flatten()
            return skipJava + skipKotlin
        }

        fun PsiNamedElement.toRenamableCodeElement(skipData: Boolean, check: CustomCheck): RenamableCodeElement {
            val currentName = if (isValid) name else null
            val prohibit = when (this) {
                is KtParameter, is KtVariableDeclaration -> hasModifier(KtTokens.OVERRIDE_KEYWORD)
                is KtNamedFunction -> hasModifier(KtTokens.OVERRIDE_KEYWORD) || isMainFunction() || isAnonymousFunction
                is KtObjectDeclaration -> isObjectLiteral() || isCompanion()
                is KtFile -> {
                    val className = currentName?.substringBefore('.')
                    val psiClasses = classes
                    val sameName = psiClasses.size >= 2
                            && psiClasses.any { it.name == className }
                            && psiClasses.any { it.name == "${className}Kt" }
                    val topLevel = psiClasses.size == 1 && !hasTopLevelCallables()
                    sameName || topLevel
                }

                is PsiMethodImpl -> {
                    val skip = skipData && isGetterOrSetter
                    val notAllow = findSuperMethods().isNotEmpty() || isConstructor || isMainMethod(this)
                    skip || notAllow
                }

                is PsiAnonymousClass -> true
                else -> false
            }
            val priority = priority(this, check)
            return RenamableCodeElement(this, currentName, prohibit, priority)
        }

        private fun priority(element: PsiNamedElement, check: CustomCheck) = when (element) {
            is KtVariableDeclaration -> if (check.variable) 405 else -1
            is KtParameter -> if (check.variable) 404 else -1
            is PsiLocalVariableImpl -> if (check.variable) 403 else -1
            is PsiParameterImpl -> if (check.variable) 402 else -1
            is PsiFieldImpl -> if (check.variable) 401 else -1
            is KtNamedFunction -> if (check.function) 302 else -1
            is PsiMethodImpl -> if (check.function) 301 else -1
            is PsiEnumConstantImpl -> if (check.clazz) 204 else -1
            is PsiClassImpl -> if (check.clazz) 203 else -1
            is KtObjectDeclaration -> if (check.clazz) 202 else -1
            is KtClass -> if (check.clazz) 201 else -1
            is KtFile -> if (check.ktFile) 104 else -1
            is PsiBinaryFile -> if (check.resource) 103 else -1
            is JsonFile -> if (check.resource) 102 else -1
            is PsiDirectory -> if (check.directory) 101 else -1
            else -> -1
        }
    }
}