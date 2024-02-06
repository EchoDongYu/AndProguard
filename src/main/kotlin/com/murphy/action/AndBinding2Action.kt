package com.murphy.action

import com.android.resources.ResourceType
import com.android.tools.idea.databinding.module.LayoutBindingModuleCache
import com.android.tools.idea.databinding.util.DataBindingUtil
import com.android.tools.idea.res.psi.ResourceReferencePsiElement
import com.android.tools.idea.util.androidFacet
import com.ibm.icu.text.SimpleDateFormat
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiReference
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlFile
import com.murphy.core.childrenDfsSequence
import com.murphy.core.computeTime
import com.murphy.core.rename
import com.murphy.util.*
import java.util.*

class AndBinding2Action : AnAction() {

    override fun actionPerformed(action: AnActionEvent) {
        val psi = action.getData(PlatformDataKeys.PSI_ELEMENT) ?: return
        if (psi !is XmlFile && psi !is PsiDirectory) return
        val project = action.project ?: return
        val scope = GlobalSearchScope.projectScope(project)
        val dateStart = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
        println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>> $dateStart [Refactor Start] <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<")
        val startTime = System.currentTimeMillis()
        var count = 0
        ProgressManager.getInstance().run(object : Task.Modal(project, PLUGIN_NAME, false) {
            override fun run(indicator: ProgressIndicator) {
                val sequenceId = psi.childrenDfsSequence()
                    .filterIsInstance<XmlAttributeValue>()
                    .mapNotNull { ResourceReferencePsiElement.create(it) }
                    .filter { it.resourceReference.resourceType == ResourceType.ID }
                    .distinctBy { it.resourceReference.name }
                    .mapNotNull { idMap(it, scope) }
                val sizeId = sequenceId.count()
                val totalId: Double = sizeId.toDouble()
                sequenceId.iterator().forEach {
                    refactorId(it.first, it.second)
                    indicator.fraction = ++count / totalId
                    indicator.text = "$count element of $sizeId element"
                }
                val sequenceLayout = psi.childrenDfsSequence()
                    .filterIsInstance<XmlFile>()
                    .mapNotNull { ResourceReferencePsiElement.create(it) }
                    .filter { it.resourceReference.resourceType == ResourceType.LAYOUT }
                    .mapNotNull { layoutMap(it, scope) }
                val sizeLayout = sequenceLayout.count()
                val totalLayout: Double = sizeLayout.toDouble()
                sequenceLayout.iterator().forEach {
                    refactorLayout(it.first, it.second)
                    indicator.fraction = ++count / totalLayout
                    indicator.text = "$count element of $sizeLayout element"
                }
            }

            override fun onSuccess() {
                notifyInfo(project, "refactor finished, take ${computeTime(startTime)}")
            }

            override fun onThrowable(error: Throwable) {
                notifyError(project, "${error.message}")
            }
        })
        val dateEnd = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
        println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>> $dateEnd [Refactor End] <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<")
    }

    private fun refactorLayout(psiElement: ResourceReferencePsiElement, list: List<PsiReference>) {
        CommandProcessor.getInstance().runUndoTransparentAction {
            val newName = randomResFileName
            psiElement.rename(newName, "XmlFile")
            ApplicationManager.getApplication().runWriteAction {
                if (list.isNotEmpty()) {
                    val newRefName = DataBindingUtil.convertFileNameToJavaClassName(newName) + "Binding"
                    println(String.format("[LayoutViewBinding] >>> %s", newRefName))
                    list.forEach { ref -> ref.handleElementRename(newRefName) }
                }
            }
        }
    }

    private fun refactorId(psiElement: ResourceReferencePsiElement, list: List<PsiReference>) {
        CommandProcessor.getInstance().runUndoTransparentAction {
            val newName = randomResIdName
            psiElement.rename(newName, "XmlId")
            ApplicationManager.getApplication().runWriteAction {
                if (list.isNotEmpty()) {
                    val newRefName = DataBindingUtil.convertAndroidIdToJavaFieldName(newName)
                    println(String.format("[IdViewBinding] >>> %s", newRefName))
                    list.forEach { ref -> ref.handleElementRename(newRefName) }
                }
            }
        }
    }

    private fun layoutMap(
        psiElement: ResourceReferencePsiElement,
        scope: GlobalSearchScope
    ): Pair<ResourceReferencePsiElement, List<PsiReference>>? {
        val facet = psiElement.androidFacet ?: return null
        val bindingModuleCache = LayoutBindingModuleCache.getInstance(facet)
        val groups = bindingModuleCache.bindingLayoutGroups
        val className = DataBindingUtil.convertFileNameToJavaClassName(psiElement.resourceReference.name) + "Binding"
        val layoutGroup = groups.firstOrNull { it.mainLayout.className == className } ?: return null
        val psiReferences = bindingModuleCache.getLightBindingClasses(layoutGroup)
            .map { ReferencesSearch.search(it, scope).findAll() }
            .flatten()
        return Pair(psiElement, psiReferences)
    }

    private fun idMap(
        psiElement: ResourceReferencePsiElement,
        scope: GlobalSearchScope
    ): Pair<ResourceReferencePsiElement, List<PsiReference>>? {
        val facet = psiElement.androidFacet ?: return null
        val bindingModuleCache = LayoutBindingModuleCache.getInstance(facet)
        val groups = bindingModuleCache.bindingLayoutGroups
        val lightClasses = groups.flatMap { group -> bindingModuleCache.getLightBindingClasses(group) }
        val fieldName = DataBindingUtil.convertAndroidIdToJavaFieldName(psiElement.resourceReference.name)
        val psiReferences = lightClasses.mapNotNull { it.allFields.find { field -> field.name == fieldName } }
            .map { field -> ReferencesSearch.search(field, scope).findAll() }
            .flatten()
        return Pair(psiElement, psiReferences)
    }

}
