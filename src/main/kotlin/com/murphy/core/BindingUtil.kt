package com.murphy.core

import com.android.tools.idea.databinding.module.LayoutBindingModuleCache
import com.android.tools.idea.databinding.util.DataBindingUtil
import com.android.tools.idea.res.psi.ResourceReferencePsiElement
import com.android.tools.idea.util.androidFacet
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiReference
import com.intellij.psi.search.SearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.murphy.util.PLUGIN_NAME

fun ResourceReferencePsiElement.findIdReference(scope: SearchScope): List<PsiReference>? {
    val bindingModuleCache = LayoutBindingModuleCache.getInstance(delegate.androidFacet ?: return null)
    val groups = bindingModuleCache.bindingLayoutGroups
    val fieldName = DataBindingUtil.convertAndroidIdToJavaFieldName(resourceReference.name)
    return groups.flatMap { bindingModuleCache.getLightBindingClasses(it) }
        .mapNotNull { it -> it.allFields.find { field -> field.name == fieldName } }
        .map { ReferencesSearch.search(it, scope).findAll() }
        .flatten()
}

fun ResourceReferencePsiElement.findLayoutReference(scope: SearchScope): List<PsiReference>? {
    val bindingModuleCache = LayoutBindingModuleCache.getInstance(delegate.androidFacet ?: return null)
    val groups = bindingModuleCache.bindingLayoutGroups
    val className = DataBindingUtil.convertFileNameToJavaClassName(resourceReference.name) + "Binding"
    val layoutGroup = groups.firstOrNull { it.mainLayout.className == className }
    return bindingModuleCache.getLightBindingClasses(layoutGroup ?: return null)
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