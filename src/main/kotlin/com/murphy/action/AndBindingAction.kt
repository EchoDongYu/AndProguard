package com.murphy.action

import com.android.resources.ResourceType
import com.android.tools.idea.databinding.module.LayoutBindingModuleCache
import com.android.tools.idea.databinding.util.DataBindingUtil
import com.android.tools.idea.res.psi.ResourceReferencePsiElement
import com.ibm.icu.text.SimpleDateFormat
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlFile
import com.murphy.core.childrenDfsSequence
import com.murphy.core.computeTime
import com.murphy.core.rename
import com.murphy.util.*
import org.jetbrains.android.facet.AndroidFacet
import java.util.*

class AndBindingAction : AnAction() {

    override fun actionPerformed(action: AnActionEvent) {
        val psi = action.getData(PlatformDataKeys.PSI_ELEMENT) ?: return
        if (psi !is XmlFile && psi !is PsiDirectory) return
        val project = action.project ?: return
        val dateStart = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
        println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>> $dateStart [Refactor Start] <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<")
        val startTime = System.currentTimeMillis()
        var count = 0
        ProgressManager.getInstance().run(object : Task.Modal(project, PLUGIN_NAME, false) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = false
                indicator.fraction = 0.0
                indicator.text = "Waiting search binding element..."
                val sequenceId = runReadAction { psi.seqResId }
                val sequenceLayout = runReadAction { psi.seqLayout }
                val size = sequenceId.size + sequenceLayout.size
                val total: Double = size.toDouble()
                sequenceId.forEach {
                    renameResId(project, it.first, it.second)
                    indicator.fraction = ++count / total
                    indicator.text = "$count element of $size element"
                }
                sequenceLayout.forEach {
                    renameLayout(project, it.first, it.second)
                    indicator.fraction = ++count / total
                    indicator.text = "$count element of $size element"
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

    private val PsiElement.seqLayout
        get() = childrenDfsSequence()
            .filterIsInstance<XmlFile>()
            .mapNotNull { it.layoutMap }
            .toList()

    private val PsiElement.seqResId
        get() = childrenDfsSequence()
            .filterIsInstance<XmlAttributeValue>()
            .mapNotNull { it.resIdMap }
            .distinctBy { it.first.resourceReference.name }
            .toList()

    private val XmlFile.layoutMap
        get(): Pair<XmlFile, List<PsiReference>>? {
            val resPsi = ResourceReferencePsiElement.create(this) ?: return null
            if (resPsi.resourceReference.resourceType != ResourceType.LAYOUT) return null
            val facet = AndroidFacet.getInstance(this) ?: return null
            val scope = GlobalSearchScope.projectScope(project)
            val bindingModuleCache = LayoutBindingModuleCache.getInstance(facet)
            val groups = bindingModuleCache.bindingLayoutGroups
            val className = DataBindingUtil.convertFileNameToJavaClassName(resPsi.resourceReference.name) + "Binding"
            val layoutGroup = groups.firstOrNull { it.mainLayout.className == className } ?: return null
            val psiReferences = bindingModuleCache.getLightBindingClasses(layoutGroup)
                .map { ReferencesSearch.search(it, scope).findAll() }
                .flatten()
            return Pair(this, psiReferences)
        }

    private val XmlAttributeValue.resIdMap
        get(): Pair<ResourceReferencePsiElement, List<PsiReference>>? {
            val resPsi = ResourceReferencePsiElement.create(this) ?: return null
            if (resPsi.resourceReference.resourceType != ResourceType.ID) return null
            val facet = AndroidFacet.getInstance(this) ?: return null
            val scope = GlobalSearchScope.projectScope(project)
            val bindingModuleCache = LayoutBindingModuleCache.getInstance(facet)
            val groups = bindingModuleCache.bindingLayoutGroups
            val lightClasses = groups.flatMap { group -> bindingModuleCache.getLightBindingClasses(group) }
            val fieldName = DataBindingUtil.convertAndroidIdToJavaFieldName(resPsi.resourceReference.name)
            val psiReferences = lightClasses.mapNotNull { it.allFields.find { field -> field.name == fieldName } }
                .map { field -> ReferencesSearch.search(field, scope).findAll() }
                .flatten()
            return Pair(resPsi, psiReferences)
        }

    private fun renameLayout(
        project: Project,
        psiElement: XmlFile,
        list: List<PsiReference>
    ) {
        val newName = randomResFileName
        ApplicationManager.getApplication().invokeAndWait {
            psiElement.rename(newName, "XmlFile")
        }
        if (list.isNotEmpty()) {
            val newRefName = DataBindingUtil.convertFileNameToJavaClassName(newName) + "Binding"
            println(String.format("[LayoutViewBinding] >>> %s", newRefName))
            WriteCommandAction.runWriteCommandAction(project) {
                list.forEach { ref -> ref.handleElementRename(newRefName) }
            }
        }
    }

    private fun renameResId(
        project: Project,
        psiElement: ResourceReferencePsiElement,
        list: List<PsiReference>
    ) {
        val newName = randomResIdName
        ApplicationManager.getApplication().invokeAndWait {
            psiElement.rename(newName, "XmlId")
        }
        if (list.isNotEmpty()) {
            val newRefName = DataBindingUtil.convertAndroidIdToJavaFieldName(newName)
            println(String.format("[IdViewBinding] >>> %s", newRefName))
            WriteCommandAction.runWriteCommandAction(project) {
                list.forEach { ref -> ref.handleElementRename(newRefName) }
            }
        }
    }

}
