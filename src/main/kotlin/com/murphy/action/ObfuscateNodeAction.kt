package com.murphy.action

import com.android.tools.idea.res.psi.ResourceReferencePsiElement
import com.intellij.json.psi.JsonFile
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiBinaryFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.impl.source.*
import com.intellij.psi.impl.source.tree.java.PsiLocalVariableImpl
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlFile
import com.murphy.config.AndConfigState
import com.murphy.core.RenamableCodeElement.Companion.toRenamableCodeElement
import com.murphy.core.RenamableElement
import com.murphy.core.RenamableXmlElement.Companion.toRenamableXmlElement
import com.murphy.util.PLUGIN_NAME
import com.murphy.util.notifyWarn
import org.jetbrains.kotlin.psi.*

class ObfuscateNodeAction : AnAction() {

    override fun actionPerformed(action: AnActionEvent) {
        val myPsi = action.getData(PlatformDataKeys.PSI_ELEMENT) ?: return
        val myProject = action.project ?: return
        if (myPsi !is PsiNamedElement) {
            notifyWarn(myProject, "PsiElement ${myPsi.javaClass.name} $myPsi")
            return
        }
        runBackgroundableTask(PLUGIN_NAME, myProject) {
            val config = AndConfigState.getInstance()
            val service = DumbService.getInstance(myProject)
            val renamableElement = runReadAction { toRenamableElement(myPsi, myProject) }
            if (renamableElement == null) {
                notifyWarn(myProject, "PsiNamedElement ${myPsi.javaClass.name} $myPsi")
                return@runBackgroundableTask
            }
            val runnable = Runnable {
                val newName = renamableElement.namingIndex?.let { config.namingNodes[it].randomNaming }
                renamableElement.performRename(myProject, newName)
            }
            ApplicationManager.getApplication().invokeAndWait {
                if (service.isDumb) {
                    service.smartInvokeLater(runnable)
                } else {
                    runnable.run()
                }
            }
        }
    }

    private fun toRenamableElement(myPsi: PsiNamedElement, project: Project): RenamableElement<*>? {
        return when (myPsi) {
            is XmlAttributeValue,
            is XmlFile -> ResourceReferencePsiElement.create(myPsi)?.let { toRenamableElement(it, project) }

            is ResourceReferencePsiElement -> myPsi.toRenamableXmlElement(GlobalSearchScope.projectScope(project))

            is KtFile, is KtClass,
            is KtObjectDeclaration,
            is PsiClassImpl,
            is PsiEnumConstantImpl,
            is KtNamedFunction,
            is PsiMethodImpl,
            is KtVariableDeclaration,
            is KtParameter,
            is PsiParameterImpl,
            is PsiFieldImpl,
            is PsiLocalVariableImpl,
            is PsiDirectory,
            is PsiBinaryFile, is JsonFile -> myPsi.toRenamableCodeElement(false, CustomCheck())

            else -> null
        }
    }
}