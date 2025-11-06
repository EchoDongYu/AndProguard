package com.murphy.core

import com.intellij.json.psi.JsonFile
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.impl.getFieldOfGetter
import com.intellij.psi.impl.getFieldOfSetter
import com.intellij.psi.util.PsiMethodUtil.isMainMethod
import com.murphy.config.AndConfigState
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*

class RenamableCodeElement(
    override val pointer: SmartPsiElementPointer<PsiNamedElement>,
    override val currentName: String?,
    val priority: Int
) : RenamableElement<PsiNamedElement> {
    override val namingIndex: Int? = when (pointer.element) {
        is KtFile, is KtClass,
        is KtObjectDeclaration,
        is PsiClass,
        is PsiEnumConstant -> 0

        is KtNamedFunction,
        is PsiMethod -> 1

        is KtVariableDeclaration,
        is KtParameter,
        is PsiParameter,
        is PsiField,
        is PsiLocalVariable -> 2

        is PsiBinaryFile, is JsonFile -> 4
        is PsiDirectory -> 5
        else -> null
    }

    override fun performRename(project: Project, name: String?) {
        val element = pointer.element ?: return
        val newName = name?.let { if (element is KtFile) "$it.kt" else it } ?: return
        runRename(project, element, newName)
    }

    companion object {
        fun findElements(check: CustomCheck, elements: List<PsiNamedElement>): List<RenamableCodeElement> {
            val skipData = AndConfigState.getInstance().state.skipData
            val skipElements = if (skipData) elements.findSkipElements().toSet() else emptySet()
            return elements.filterNot { skipElements.contains(it) }
                .mapNotNull { it.toRenamableCodeElement(skipData, check) }
                .sortedByDescending { it.priority }
                .distinctBy { if (it.pointer.element is PsiBinaryFile) it.currentName else it }
                .toList()
        }

        private fun List<PsiNamedElement>.findSkipElements(): List<PsiNamedElement> {
            val skipJava = filterIsInstance<PsiMethod>()
                .filter { it.isGetterOrSetter }
                .mapNotNull { it.fieldOfGetterOrSetter }
            val skipKotlin = filterIsInstance<KtClass>()
                .filter { it.isData() }
                .map { it.primaryConstructorParameters }
                .flatten()
            return skipJava + skipKotlin
        }

        fun PsiNamedElement.toRenamableCodeElement(skipData: Boolean, check: CustomCheck): RenamableCodeElement? {
            val currentName = (if (isValid) name else null) ?: return null
            val prohibit = when (this) {
                is KtParameter, is KtVariableDeclaration -> hasModifier(KtTokens.OVERRIDE_KEYWORD)
                is KtNamedFunction -> hasModifier(KtTokens.OVERRIDE_KEYWORD) || isMainFunctionK2() || nameIdentifier == null /* isAnonymousFunction */
                is KtObjectDeclaration -> isObjectLiteral() || isCompanion()
                is KtFile -> {
                    val className = currentName.substringBefore('.')
                    val psiClasses = classes
                    val sameName = psiClasses.size >= 2
                            && psiClasses.any { it.name == className }
                            && psiClasses.any { it.name == "${className}Kt" }
                    val topLevel = psiClasses.size == 1 && !hasTopLevelCallables()
                    sameName || topLevel
                }

                is PsiMethod -> {
                    val skip = skipData && isGetterOrSetter
                    val notAllow = findSuperMethods().isNotEmpty() || isConstructor || isMainMethod(this)
                    skip || notAllow
                }

                is PsiAnonymousClass -> true
                else -> false
            }
            val priority = priority(this, check)
            if (prohibit || priority < 0) return null
            val pointer = SmartPointerManager.getInstance(project).createSmartPsiElementPointer(this)
            return RenamableCodeElement(pointer, currentName, priority)
        }

        private fun priority(element: PsiNamedElement, check: CustomCheck) = when (element) {
            is KtFile -> if (check.ktFile) 104 else -1
            is PsiBinaryFile -> if (check.resource) 103 else -1
            is JsonFile -> if (check.resource) 102 else -1
            is PsiDirectory -> if (check.directory) 101 else -1
            is PsiEnumConstant -> if (check.clazz) 204 else -1
            is PsiClass -> if (check.clazz) 203 else -1
            is KtObjectDeclaration -> if (check.clazz) 202 else -1
            is KtClass -> if (check.clazz) 201 else -1
            is KtNamedFunction -> if (check.function) 302 else -1
            is PsiMethod -> if (check.function) 301 else -1
            is KtVariableDeclaration -> if (check.variable) 405 else -1
            is KtParameter -> if (check.variable) 404 else -1
            is PsiLocalVariable -> if (check.variable) 403 else -1
            is PsiParameter -> if (check.variable) 402 else -1
            is PsiField -> if (check.variable) 401 else -1
            else -> -1
        }

        private val PsiMethod.isSetter: Boolean
            get() = name.startsWith("set")

        val PsiMethod.isGetterOrSetter
            get() = name.run { startsWith("set") || startsWith("get") || startsWith("is") }

        val PsiMethod.fieldOfGetterOrSetter
            get() = if (isSetter) getFieldOfSetter(this) else getFieldOfGetter(this)

        private fun KtNamedFunction.isMainFunctionK2(): Boolean {
            if (name != "main" || !isTopLevel) return false
            val params = valueParameters
            return params.isEmpty() || (params.size == 1 && params[0].typeReference?.text == "Array<String>")
        }
    }
}