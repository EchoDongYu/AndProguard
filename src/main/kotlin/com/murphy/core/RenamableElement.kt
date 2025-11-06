package com.murphy.core

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.refactoring.RefactoringFactory
import com.murphy.util.LogUtil

interface RenamableElement<T : PsiElement> {
    val pointer: SmartPsiElementPointer<T>
    val namingIndex: Int?
    val currentName: String?
    fun performRename(project: Project, name: String?)

    fun runRename(project: Project, psiElement: PsiElement, newName: String) {
        if (!psiElement.isValid) {
            LogUtil.info(project, "[${psiElement.javaClass.simpleName}] $currentName is invalidated, skipping")
            return
        }
        LogUtil.info(project, "[${psiElement.javaClass.simpleName}] $currentName >>> $newName")
        RefactoringFactory.getInstance(project)
            .createRename(psiElement, newName, false, false)
            .run()
    }
}