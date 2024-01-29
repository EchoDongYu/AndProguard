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
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.ChildRole
import com.intellij.psi.impl.source.tree.java.PsiReferenceExpressionImpl
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlFile
import com.murphy.util.XML_SUFFIX
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
    val newRefName = DataBindingUtil.convertAndroidIdToJavaFieldName(newName)
    val scope = GlobalSearchScope.projectScope(project)
    val bindingRef = relevantFields.map { ReferencesSearch.search(it, scope).findAll() }
        .flatten()
        .map { it.element }
    val (xmlRef, classRef) = ReferencesSearch.search(resPsi, scope).findAll()
        .map { it.element }
        .partition { it is XmlAttributeValue }
    CommandProcessor.getInstance().runUndoTransparentAction {
        ApplicationManager.getApplication().runWriteAction {
            if (bindingRef.isNotEmpty()) {
                println(String.format("[IdViewBinding] %s >>> %s", fieldName, newRefName))
                bindingRef.forEach { it.replaceSelf(newRefName) }
            }
            if (classRef.isNotEmpty()) {
                println(String.format("[IdClass] %s >>> %s", resPsi.name, newName))
                classRef.forEach {
                    when (it) {
                        is KtNameReferenceExpression -> it.replaceSelf(newName)
                        is PsiReferenceExpressionImpl -> it.replaceSelfOnJava(newName)
                    }
                }
            }
            if (xmlRef.isNotEmpty()) {
                println(String.format("[IdRes] %s >>> %s", resPsi.name, newName))
                xmlRef.map { it.parent }
                    .filterIsInstance<XmlAttribute>()
                    .forEach {
                        if (it.name == ANDROID_NS_NAME_PREFIX + ATTR_ID)
                            it.setValue(NEW_ID_PREFIX + newName)
                        else it.setValue(ID_PREFIX + newName)
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
    val newRefName = DataBindingUtil.convertFileNameToJavaClassName(newName) + "Binding"
    val scope = GlobalSearchScope.projectScope(project)
    val bindingRef = lightBindingClasses.map { ReferencesSearch.search(it, scope).findAll() }
        .flatten()
        .map { it.element }
    val (xmlRef, classRef) = ReferencesSearch.search(resPsi, scope).findAll()
        .map { it.element }
        .partition { it is XmlAttributeValue }
    CommandProcessor.getInstance().runUndoTransparentAction {
        ApplicationManager.getApplication().runWriteAction {
            if (bindingRef.isNotEmpty()) {
                println(String.format("[LayoutViewBinding] %s >>> %s", className, newRefName))
                bindingRef.forEach { it.replaceSelf(newRefName) }
            }
            if (classRef.isNotEmpty()) {
                println(String.format("[LayoutClass] %s >>> %s", resPsi.name, newName))
                classRef.forEach {
                    when (it) {
                        is KtNameReferenceExpression -> it.replaceSelf(newName)
                        is PsiReferenceExpressionImpl -> it.replaceSelfOnJava(newName)
                    }
                }
            }
            if (xmlRef.isNotEmpty()) {
                println(String.format("[LayoutRes] %s >>> %s", resPsi.name, newName))
                xmlRef.map { it.parent }
                    .filterIsInstance<XmlAttribute>()
                    .forEach { it.setValue(LAYOUT_RESOURCE_PREFIX + newName) }
            }
            val newFileName = newName + XML_SUFFIX
            println(String.format("[LayoutFile] %s >>> %s", name, newFileName))
            name = newFileName
        }
    }
}

private fun PsiElement.replaceSelf(newName: String) {
    val factory = JavaPsiFacade.getElementFactory(manager.project)
    val newNameIdentifier = factory.createIdentifier(newName)
    replace(newNameIdentifier)
}

private fun PsiReferenceExpressionImpl.replaceSelfOnJava(newName: String) {
    val oldIdentifier = findChildByRoleAsPsiElement(ChildRole.REFERENCE_NAME) ?: return
    val identifier = JavaPsiFacade.getElementFactory(getProject()).createIdentifier(newName)
    oldIdentifier.replace(identifier)
}