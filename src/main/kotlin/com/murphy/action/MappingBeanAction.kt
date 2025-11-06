package com.murphy.action

import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.PsiManager
import com.murphy.core.RenamableBeanElement
import com.murphy.core.computeTime
import com.murphy.core.executeOnSmartMode
import com.murphy.util.*
import org.jetbrains.kotlin.idea.base.psi.childrenDfsSequence

class MappingBeanAction : AnAction() {

    override fun actionPerformed(action: AnActionEvent) {
        val myPsi = action.getData(PlatformDataKeys.PSI_ELEMENT) ?: return
        val myProject = action.project ?: return
        val label = "JSON Mapping Interface"
        val beanMap = beanMap(myProject) ?: return
        val startTime = System.currentTimeMillis()
        runBackgroundableTask(PLUGIN_NAME, myProject) { indicator ->
            try {
                indicator.text = "Find element"
                val pair = runReadAction {
                    RenamableBeanElement.findElements(myProject, beanMap, myPsi.childrenDfsSequence())
                        .partition { it.pointer.element is PsiLanguageInjectionHost }
                }
                indicator.text = "Create naming"
                val total = pair.first.count() + pair.second.count()
                DumbService.getInstance(myProject).executeOnSmartMode {
                    pair.second.forEachIndexed { index, item ->
                        indicator.fraction = index / total.toDouble()
                        indicator.text = "${item.currentName} ${index + 1}/$total"
                        item.performRename(myProject, null)
                    }
                    WriteCommandAction.runWriteCommandAction(myProject) {
                        pair.first.forEachIndexed { index, item ->
                            indicator.fraction = index / total.toDouble()
                            indicator.text = "${item.currentName} ${index + 1}/$total"
                            item.performRename(myProject, null)
                        }
                    }
                }
                LogUtil.logRecord(myProject, label, true)
                notifyInfo(myProject, "refactor finished, take ${computeTime(startTime)}")
            } catch (e: Exception) {
                if (e is ProcessCanceledException) throw e
                else notifyError(myProject, "${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun beanMap(project: Project): Map<String, String>? {
        val descriptor = FileChooserDescriptor(true, false, false, false, false, false)
            .withFileFilter { it.extension == "json" }
        val chooseFile = FileChooser.chooseFile(descriptor, project, null)
        val jsonValue = runReadAction {
            val psiFile = chooseFile?.let { PsiManager.getInstance(project).findFile(it) }
            (psiFile as? JsonFile)?.topLevelValue
        }
        if (jsonValue !is JsonObject) {
            notifyWarn(project, "Only JsonObject is supported")
            return null
        }
        val map = runReadAction {
            jsonValue.propertyList.mapNotNull { property ->
                (property.value as? JsonStringLiteral)?.let { property.name to it.value }
            }.toMap()
        }
        if (map.isEmpty()) {
            notifyWarn(project, "Key-value isEmpty")
            return null
        }
        return map
    }

}