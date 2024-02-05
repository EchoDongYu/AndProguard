package com.murphy.core

import com.android.SdkConstants.*
import com.android.resources.ResourceType
import com.android.tools.idea.databinding.module.LayoutBindingModuleCache
import com.android.tools.idea.databinding.util.DataBindingUtil
import com.android.tools.idea.res.psi.ResourceReferencePsiElement
import com.android.tools.idea.util.androidFacet
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiJavaCodeReferenceElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlFile
import com.murphy.util.randomResFileName
import com.murphy.util.randomResIdName
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import java.util.*

fun processViewBinding(psi: XmlFile, idList: MutableList<String> = LinkedList()) {
    println("============================== ${psi.name} ==============================")
    psi.childrenDfsSequence()
        .filterIsInstance<XmlAttribute>()
        .filter { it.name == ANDROID_NS_NAME_PREFIX + ATTR_ID && !idList.contains(it.value) }
        .mapNotNull { it.valueElement }
        .forEach {
            val newName = randomResIdName
            idList.add(NEW_ID_PREFIX + newName)
            it.processResId(newName)
        }
    psi.processResFile()
}

private fun XmlAttributeValue.processResId(newName: String) {
    val resPsi = ResourceReferencePsiElement.create(this) ?: return
    if (resPsi.resourceReference.resourceType != ResourceType.ID) return
    val androidFacet = androidFacet ?: return
    val bindingModuleCache = LayoutBindingModuleCache.getInstance(androidFacet)
    val groups = bindingModuleCache.bindingLayoutGroups
    val lightClasses = groups.flatMap { group -> bindingModuleCache.getLightBindingClasses(group) }
    val fieldName = DataBindingUtil.convertAndroidIdToJavaFieldName(resPsi.resourceReference.name)
    val relevantFields = lightClasses.mapNotNull { clazz -> clazz.allFields.find { it.name == fieldName } }
    val scope = GlobalSearchScope.projectScope(project)
    val bindingRef = relevantFields.map { ReferencesSearch.search(it, scope).findAll() }
        .flatten()
        .map { it.element }
    CommandProcessor.getInstance().runUndoTransparentAction {
        rename(newName, "XmlId")
        ApplicationManager.getApplication().runWriteAction {
            if (bindingRef.isNotEmpty()) {
                val newRefName = DataBindingUtil.convertAndroidIdToJavaFieldName(newName)
                println(String.format("[IdViewBinding] %s >>> %s", fieldName, newRefName))
                bindingRef.forEach {
                    when (it) {
                        is KtNameReferenceExpression -> it.replaceSelfOnKotlin(newRefName)
                        is PsiJavaCodeReferenceElement -> it.handleElementRename(newRefName)
                    }
                }
            }
        }
    }
}

private fun XmlFile.processResFile() {
    val resPsi = ResourceReferencePsiElement.create(this) ?: return
    if (resPsi.resourceReference.resourceType != ResourceType.LAYOUT) return
    val androidFacet = androidFacet ?: return
    val bindingModuleCache = LayoutBindingModuleCache.getInstance(androidFacet)
    val groups = bindingModuleCache.bindingLayoutGroups
    val className = DataBindingUtil.convertFileNameToJavaClassName(name) + "Binding"
    val layoutGroup = groups.firstOrNull { it.mainLayout.className == className } ?: return
    val lightBindingClasses = bindingModuleCache.getLightBindingClasses(layoutGroup)
    val newName = randomResFileName
    val scope = GlobalSearchScope.projectScope(project)
    val bindingRef = lightBindingClasses.map { ReferencesSearch.search(it, scope).findAll() }
        .flatten()
        .map { it.element }
    CommandProcessor.getInstance().runUndoTransparentAction {
        rename(newName, "XmlFile")
        ApplicationManager.getApplication().runWriteAction {
            if (bindingRef.isNotEmpty()) {
                val newRefName = DataBindingUtil.convertFileNameToJavaClassName(newName) + "Binding"
                println(String.format("[LayoutViewBinding] %s >>> %s", className, newRefName))
                bindingRef.forEach {
                    when (it) {
                        is KtNameReferenceExpression -> it.replaceSelfOnKotlin(newRefName)
                        is PsiJavaCodeReferenceElement -> it.handleElementRename(newRefName)
                    }
                }
            }
        }
    }
}

private fun KtNameReferenceExpression.replaceSelfOnKotlin(newName: String) {
    val oldIdentifier = getReferencedNameElement()
    val identifier = JavaPsiFacade.getElementFactory(getProject()).createIdentifier(newName)
    oldIdentifier.replace(identifier)
}