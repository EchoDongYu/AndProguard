package com.murphy.action

import com.android.resources.ResourceType
import com.android.tools.idea.res.psi.ResourceReferencePsiElement
import com.ibm.icu.text.SimpleDateFormat
import com.intellij.json.psi.JsonFile
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.psi.PsiBinaryFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.impl.source.PsiClassImpl
import com.intellij.psi.impl.source.PsiEnumConstantImpl
import com.intellij.psi.impl.source.PsiFieldImpl
import com.intellij.psi.impl.source.PsiMethodImpl
import com.intellij.psi.impl.source.PsiParameterImpl
import com.intellij.psi.impl.source.tree.java.PsiLocalVariableImpl
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlFile
import com.murphy.config.AndConfigState
import com.murphy.core.*
import com.murphy.core.ResourceGenerator.excludedFileType
import com.murphy.core.ResourceGenerator.includedAttrType
import com.murphy.core.rename
import com.murphy.core.renameX
import com.murphy.util.KOTLIN_SUFFIX
import com.murphy.util.notifyInfo
import com.murphy.util.notifyWarn
import org.jetbrains.kotlin.idea.core.getPackage
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtVariableDeclaration
import java.util.*

class ProguardNodeAction : AnAction() {

    override fun actionPerformed(action: AnActionEvent) {
        val myPsi = action.getData(PlatformDataKeys.PSI_ELEMENT) ?: return
        val myProject = action.project ?: return
        val dateStart = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
        fun PsiNamedElement.rename(newName: String, desc: String) = rename(newName, desc, myProject)
        fun XmlAttributeValue.renameX(newName: String, desc: String) = renameX(newName, desc, myProject)
        println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>> $dateStart [Refactor Start] <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<")
        val startTime = System.currentTimeMillis()
        val config = AndConfigState.getInstance().apply { initRandomNode() }
        if (myPsi !is PsiNamedElement) {
            notifyWarn(myProject, "PsiElement ${myPsi.javaClass.name} $myPsi")
            return
        }
        when (myPsi) {
            is PsiDirectory -> {
                if (config.directoryRule.isEmpty()) return
                myPsi.getPackage()?.rename(config.randomDirectoryName, "Directory")
            }

            is PsiBinaryFile, is JsonFile -> {
                if (config.resFileRule.isEmpty()) return
                myPsi.rename(config.randomResFileName, "ResourceFile")
            }

            is XmlFile -> {
                if (config.resFileRule.isEmpty()) return
                val resPsi = ResourceReferencePsiElement.create(myPsi) ?: return
                val resourceType = resPsi.resourceReference.resourceType
                if (resourceType == ResourceType.LAYOUT) {
                    resPsi.renameLayout(config.randomResFileName, myProject)
                } else if (!excludedFileType.contains(resourceType)) {
                    myPsi.rename(config.randomResFileName, "XmlFile")
                }
            }

            is PsiParameterImpl, is PsiFieldImpl, is PsiLocalVariableImpl,
            is KtVariableDeclaration, is KtParameter -> {
                if (config.propertyRule.isEmpty()) return
                myPsi.rename(config.randomPropertyName, "Property")
            }

            is KtNamedFunction, is PsiMethodImpl -> {
                if (config.functionRule.isEmpty()) return
                myPsi.rename(config.randomFunctionName, "Function")
            }

            is KtObjectDeclaration, is KtClass,
            is PsiClassImpl, is PsiEnumConstantImpl -> {
                if (config.classRule.isEmpty()) return
                myPsi.rename(config.randomClassName, "Class")
            }

            is KtFile -> {
                if (config.classRule.isEmpty()) return
                myPsi.rename(config.randomClassName + KOTLIN_SUFFIX, "KtFile")
            }

            is ResourceReferencePsiElement -> {
                if (config.resourceRule.isEmpty()) return
                val resourceType = myPsi.resourceReference.resourceType
                if (resourceType == ResourceType.ID) {
                    myPsi.renameId(config.randomResourceName, myProject)
                } else if (includedAttrType.contains(resourceType)) {
                    val delegate = myPsi.delegate
                    if (delegate is XmlAttributeValue) {
                        delegate.renameX(config.randomResourceName, "XmlAttribute")
                    }
                }
            }

            else -> notifyWarn(myProject, "PsiNamedElement ${myPsi.javaClass.name} $myPsi")
        }
        notifyInfo(myProject, "refactor finished, take ${computeTime(startTime)}")
        val dateEnd = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
        println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>> $dateEnd [Refactor End] <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<")
    }

}