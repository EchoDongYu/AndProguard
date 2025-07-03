package com.murphy.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.murphy.config.AndConfigState
import com.murphy.core.NamingPool
import com.murphy.core.RenamableCodeElement
import com.murphy.core.RenamableXmlElement
import com.murphy.core.computeTime
import com.murphy.ui.CustomDialog
import com.murphy.util.LogUtil
import com.murphy.util.PLUGIN_NAME
import com.murphy.util.notifyError
import com.murphy.util.notifyInfo
import org.jetbrains.kotlin.idea.base.psi.childrenDfsSequence

class ObfuscateCustomAction : AnAction() {

    override fun actionPerformed(action: AnActionEvent) {
        val myPsi = action.getData(PlatformDataKeys.PSI_ELEMENT) ?: return
        val myProject = action.project ?: return
        val label = "Obfuscate Custom"
        LogUtil.logRecord(myProject, label, false)
        val startTime = System.currentTimeMillis()
        ApplicationManager.getApplication().invokeLater {
            val dialog = CustomDialog()
            dialog.setListener {
                obfuscateByCheck(myProject, myPsi, it) {
                    LogUtil.logRecord(myProject, label, true)
                    notifyInfo(myProject, "refactor finished, take ${computeTime(startTime)}")
                }
            }
            dialog.show()
        }
    }

}

fun obfuscateByCheck(
    myProject: Project,
    myPsi: PsiElement,
    check: CustomCheck,
    onFinished: () -> Unit
) = runBackgroundableTask(PLUGIN_NAME, myProject) { indicator ->
    try {
        val config = AndConfigState.getInstance()
        val service = DumbService.getInstance(myProject)
        indicator.text = "Find element"
        val list = runReadAction {
            val sequence = myPsi.childrenDfsSequence().filterIsInstance<PsiNamedElement>()
            val xmlElements = if (check.resource) RenamableXmlElement.findElements(myProject, sequence) else emptyList()
            val codeElements = RenamableCodeElement.findElements(config.state.skipData, check, sequence)
            xmlElements + codeElements
        }
        indicator.text = "Create naming"
        val namingPool = NamingPool.createNamingPool(list)
        val total = list.count()
        val runnable = Runnable {
            list.forEachIndexed { index, item ->
                indicator.fraction = index / total.toDouble()
                indicator.text = "${item.currentName} ${index + 1}/$total"
                val newName = item.namingIndex?.let { namingPool.getOrNull(it) }
                item.performRename(myProject, newName)
            }
        }
        ApplicationManager.getApplication().invokeAndWait {
            if (service.isDumb) {
                service.smartInvokeLater(runnable)
            } else {
                runnable.run()
            }
        }
        onFinished()
    } catch (e: Exception) {
        if (e is ProcessCanceledException) throw e
        else notifyError(myProject, "${e.message}")
        e.printStackTrace()
    }
}

data class CustomCheck(
    val ktFile: Boolean = true,
    val clazz: Boolean = true,
    val function: Boolean = true,
    val variable: Boolean = true,
    val resource: Boolean = true,
    val directory: Boolean = true
)