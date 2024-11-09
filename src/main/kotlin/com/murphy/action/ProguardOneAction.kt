package com.murphy.action

import com.android.resources.ResourceType
import com.android.tools.idea.databinding.util.DataBindingUtil
import com.android.tools.idea.res.psi.ResourceReferencePsiElement
import com.ibm.icu.text.SimpleDateFormat
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiBinaryFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.impl.source.PsiClassImpl
import com.intellij.psi.impl.source.PsiEnumConstantImpl
import com.intellij.psi.impl.source.PsiFieldImpl
import com.intellij.psi.impl.source.PsiMethodImpl
import com.intellij.psi.impl.source.PsiParameterImpl
import com.intellij.psi.impl.source.tree.java.PsiLocalVariableImpl
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlFile
import com.murphy.config.AndConfigState
import com.murphy.core.*
import com.murphy.core.ResourceGenerator.excludedFileType
import com.murphy.core.ResourceGenerator.includedAttrType
import com.murphy.core.findIdReference
import com.murphy.core.findLayoutReference
import com.murphy.core.rename
import com.murphy.core.renameX
import com.murphy.util.KOTLIN_SUFFIX
import com.murphy.util.notifyInfo
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtProperty
import java.util.*

class ProguardOneAction : AnAction() {

    override fun actionPerformed(action: AnActionEvent) {
        val psi = action.getData(PlatformDataKeys.PSI_ELEMENT) ?: return
        val project = action.project ?: return
        val dateStart = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
        println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>> $dateStart [Refactor Start] <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<")
        val startTime = System.currentTimeMillis()
        val config = AndConfigState.getInstance()
        config.initRandomNode()
        if (psi is PsiNamedElement) {
            when (psi) {
                is PsiDirectory -> if (config.directoryRule.isNotEmpty()) {
                    psi.rename(config.randomDirectoryName, "Directory")
                }

                is PsiBinaryFile -> if (config.resFileRule.isNotEmpty()) {
                    psi.rename(config.randomResFileName, "BinaryFile")
                }

                is XmlFile -> if (config.resFileRule.isNotEmpty()) {
                    val resPsi = ResourceReferencePsiElement.create(psi)
                    val service = DumbService.getInstance(project)
                    val scope = GlobalSearchScope.projectScope(project)
                    val resourceType = resPsi?.resourceReference?.resourceType ?: return
                    if (resourceType == ResourceType.LAYOUT) {
                        val psiReferences = service.dumbReadAction { resPsi.findLayoutReference(scope) }
                        val newName = config.randomResFileName
                        psi.rename(newName, "Layout")
                        psiReferences?.run {
                            if (isNotEmpty()) {
                                val newRefName = DataBindingUtil.convertFileNameToJavaClassName(newName) + "Binding"
                                println(String.format("[LayoutBinding] >>> %s", newRefName))
                                handleReferenceRename(project, newRefName)
                            }
                        }
                    } else if (!excludedFileType.contains(resourceType)) {
                        psi.rename(config.randomResFileName, "File")
                    }
                }

                is PsiParameterImpl, is PsiFieldImpl, is PsiLocalVariableImpl,
                is KtProperty, is KtParameter -> if (config.propertyRule.isNotEmpty()) {
                    psi.rename(config.randomPropertyName, "Field")
                }

                is KtNamedFunction, is PsiMethodImpl -> if (config.functionRule.isNotEmpty()) {
                    psi.rename(config.randomFunctionName, "Method")
                }

                is KtObjectDeclaration, is KtClass,
                is PsiClassImpl, is PsiEnumConstantImpl -> if (config.classRule.isNotEmpty()) {
                    psi.rename(config.randomClassName, "Class")
                }

                is KtFile -> if (config.classRule.isNotEmpty()) {
                    psi.rename(config.randomClassName + KOTLIN_SUFFIX, "KtFile")
                }

                is ResourceReferencePsiElement -> if (config.resourceRule.isNotEmpty()) {
                    val resPsi = ResourceReferencePsiElement.create(psi)
                    val service = DumbService.getInstance(project)
                    val scope = GlobalSearchScope.projectScope(project)
                    val resourceType = resPsi?.resourceReference?.resourceType ?: return
                    if (resourceType == ResourceType.ID) {
                        val psiReferences = service.dumbReadAction { psi.findIdReference(scope) }
                        val delegate = psi.delegate
                        val newName = config.randomResourceName
                        if (delegate is XmlAttributeValue) {
                            delegate.renameX(newName, "IdAttribute")
                            psiReferences?.run {
                                if (isNotEmpty()) {
                                    val newRefName = DataBindingUtil.convertAndroidIdToJavaFieldName(newName)
                                    println(String.format("[IdBinding] >>> %s", newRefName))
                                    handleReferenceRename(project, newRefName)
                                }
                            }
                        }
                    } else if (includedAttrType.contains(resourceType)) {
                        val delegate = psi.delegate
                        if (delegate is XmlAttributeValue) {
                            delegate.renameX(config.randomResourceName, "Attribute")
                        }
                    }
                }

                else -> notifyInfo(action.project, "PsiNamedElement ${psi.javaClass.name} $psi")
            }
        } else {
            notifyInfo(action.project, "PsiElement ${psi.javaClass.name} $psi")
        }
        notifyInfo(action.project, "refactor finished, take ${computeTime(startTime)}")
        val dateEnd = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
        println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>> $dateEnd [Refactor End] <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<")
    }

}