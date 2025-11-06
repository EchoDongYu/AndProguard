package com.murphy.core

import com.android.resources.ResourceType
import com.android.tools.idea.databinding.module.LayoutBindingModuleCache
import com.android.tools.idea.databinding.util.DataBindingUtil
import com.android.tools.idea.res.psi.ResourceReferencePsiElement
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReference
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlFile
import com.murphy.util.LogUtil
import com.murphy.util.PLUGIN_NAME
import org.jetbrains.android.facet.AndroidFacet
import org.jetbrains.kotlin.idea.base.psi.childrenDfsSequence

class RenamableXmlElement(
    override val pointer: SmartPsiElementPointer<ResourceReferencePsiElement>,
    override val currentName: String?,
    private val references: List<PsiReference>?
) : RenamableElement<ResourceReferencePsiElement> {
    override val namingIndex: Int? = when (pointer.element?.delegate) {
        is XmlAttributeValue -> 3
        is XmlFile -> 4
        else -> null
    }

    override fun performRename(project: Project, name: String?) {
        val newName = name ?: return
        val delegate = pointer.element?.delegate ?: return
        runRename(project, delegate, newName)
        references?.run {
            val newRefName = when (delegate) {
                is XmlAttributeValue -> DataBindingUtil.convertAndroidIdToJavaFieldName(newName)
                else -> DataBindingUtil.convertFileNameToJavaClassName(newName) + "Binding"
            }
            LogUtil.info(project, String.format("[Binding] >>> %s", newRefName))
            handleReferenceRename(project, newRefName)
        }
    }

    companion object {
        fun findElements(
            project: Project,
            check: CustomCheck,
            elements: List<PsiNamedElement>
        ): List<RenamableXmlElement> {
            if (!check.resource) return emptyList()
            val scope = GlobalSearchScope.projectScope(project)
            return findXmlElements(elements).mapNotNull { it.toRenamableXmlElement(scope) }.toList()
        }

        val includedAttrType = arrayOf(
            ResourceType.STRING, ResourceType.INTEGER, ResourceType.ARRAY,
            ResourceType.STYLE, ResourceType.COLOR, ResourceType.DIMEN, ResourceType.ID
        )

        val excludedFileType = arrayOf(
            ResourceType.STRING, ResourceType.INTEGER, ResourceType.ATTR,
            ResourceType.STYLE, ResourceType.COLOR, ResourceType.DIMEN, ResourceType.MIPMAP
        )

        fun findXmlElements(elements: List<PsiNamedElement>): List<ResourceReferencePsiElement> {
            val allElements = elements.filterIsInstance<XmlFile>()
            val fileElements = allElements.mapNotNull { ResourceReferencePsiElement.create(it) }
                .filterNot { excludedFileType.contains(it.resourceReference.resourceType) }
            val attrElements = allElements.map { file ->
                file.childrenDfsSequence()
                    .filterIsInstance<XmlAttributeValue>()
                    .mapNotNull { ResourceReferencePsiElement.create(it) }
                    .toList()
            }.flatten()
                .distinctBy { it.resourceReference.resourceUrl }
                .filter { includedAttrType.contains(it.resourceReference.resourceType) }
            return attrElements + fileElements
        }

        fun ResourceReferencePsiElement.toRenamableXmlElement(scope: SearchScope): RenamableXmlElement? {
            val type = resourceReference.resourceType
            val currentName = (if (isValid) name else null) ?: return null
            val psiReferences = when (type) {
                ResourceType.ID -> findIdReference(scope)
                ResourceType.LAYOUT -> findLayoutReference(scope)
                else -> null
            }?.takeIf { it.isNotEmpty() }
            val pointer = SmartPointerManager.getInstance(project).createSmartPsiElementPointer(this)
            return RenamableXmlElement(pointer, currentName, psiReferences)
        }

        fun ResourceReferencePsiElement.findIdReference(scope: SearchScope): List<PsiReference>? {
            val androidFacet = AndroidFacet.getInstance(delegate) ?: return null
            val bindingModuleCache = LayoutBindingModuleCache.getInstance(androidFacet)
            val groups = bindingModuleCache.bindingLayoutGroups
            val fieldName = DataBindingUtil.convertAndroidIdToJavaFieldName(resourceReference.name)
            return groups.flatMap { group -> bindingModuleCache.getLightBindingClasses { group == it } }
                .mapNotNull { it.allFields.find { field -> field.name == fieldName } }
                .map { ReferencesSearch.search(it, scope).findAll() }
                .flatten()
        }

        fun ResourceReferencePsiElement.findLayoutReference(scope: SearchScope): List<PsiReference>? {
            val androidFacet = AndroidFacet.getInstance(delegate) ?: return null
            val bindingModuleCache = LayoutBindingModuleCache.getInstance(androidFacet)
            val groups = bindingModuleCache.bindingLayoutGroups
            val className = DataBindingUtil.convertFileNameToJavaClassName(resourceReference.name) + "Binding"
            val layoutGroup = groups.firstOrNull { it.mainLayout.className == className }
            return bindingModuleCache.getLightBindingClasses { it == layoutGroup }
                .map { ReferencesSearch.search(it, scope).findAll() }
                .flatten()
        }

        fun List<PsiReference>.handleReferenceRename(project: Project, newRefName: String) {
            WriteCommandAction.writeCommandAction(project)
                .withName(PLUGIN_NAME)
                .run<Throwable> {
                    forEach { it.handleElementRename(newRefName) }
                }
        }
    }
}