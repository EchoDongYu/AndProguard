package com.murphy.core

import com.android.resources.ResourceType
import com.android.tools.idea.databinding.util.DataBindingUtil
import com.android.tools.idea.res.psi.ResourceReferencePsiElement
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReference
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlFile
import com.murphy.util.LogUtil
import org.jetbrains.kotlin.idea.base.psi.childrenDfsSequence

class RenamableXmlElement(
    override val element: ResourceReferencePsiElement,
    override val currentName: String?,
    private val references: List<PsiReference>?
) : RenamableElement<ResourceReferencePsiElement> {
    override val namingIndex: Int? = when (element.delegate) {
        is XmlAttributeValue -> 3
        is XmlFile -> 4
        else -> null
    }

    override fun performRename(project: Project, name: String?) {
        val newName = name ?: return
        runRename(project, element.delegate, newName)
        references?.run {
            val newRefName = when (element.delegate) {
                is XmlAttributeValue -> DataBindingUtil.convertAndroidIdToJavaFieldName(newName)
                else -> DataBindingUtil.convertFileNameToJavaClassName(newName) + "Binding"
            }
            LogUtil.info(project, String.format("[Binding] >>> %s", newRefName))
            handleReferenceRename(project, newRefName)
        }
    }

    companion object {
        fun findElements(project: Project, sequence: Sequence<PsiNamedElement>): List<RenamableXmlElement> {
            val scope = GlobalSearchScope.projectScope(project)
            return findXmlElements(sequence).map { it.toRenamableXmlElement(scope) }
                .filter { it.currentName != null }
                .toList()
        }

        val includedAttrType = arrayOf(
            ResourceType.STRING, ResourceType.INTEGER, ResourceType.ARRAY,
            ResourceType.STYLE, ResourceType.COLOR, ResourceType.DIMEN, ResourceType.ID
        )

        val excludedFileType = arrayOf(
            ResourceType.STRING, ResourceType.INTEGER, ResourceType.ATTR,
            ResourceType.STYLE, ResourceType.COLOR, ResourceType.DIMEN, ResourceType.MIPMAP
        )

        fun findXmlElements(sequence: Sequence<PsiNamedElement>): Sequence<ResourceReferencePsiElement> {
            val fileSequence = sequence.filterIsInstance<XmlFile>()
                .mapNotNull { ResourceReferencePsiElement.create(it) }
                .filterNot { excludedFileType.contains(it.resourceReference.resourceType) }
            val attrSequence = sequence.filterIsInstance<XmlFile>().map { file ->
                file.childrenDfsSequence()
                    .filterIsInstance<XmlAttributeValue>()
                    .mapNotNull { ResourceReferencePsiElement.create(it) }
            }.flatten()
                .distinctBy { it.resourceReference.resourceUrl }
                .filter { includedAttrType.contains(it.resourceReference.resourceType) }
            return attrSequence + fileSequence
        }

        fun ResourceReferencePsiElement.toRenamableXmlElement(scope: SearchScope): RenamableXmlElement {
            val type = resourceReference.resourceType
            val currentName = if (isValid) name else null
            val psiReferences = when (type) {
                ResourceType.ID -> findIdReference(scope)
                ResourceType.LAYOUT -> findLayoutReference(scope)
                else -> null
            }?.takeIf { it.isNotEmpty() }
            return RenamableXmlElement(this, currentName, psiReferences)
        }
    }
}