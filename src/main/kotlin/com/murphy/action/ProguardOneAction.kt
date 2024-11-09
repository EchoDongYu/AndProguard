package com.murphy.action

import com.ibm.icu.text.SimpleDateFormat
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.psi.PsiBinaryFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.impl.source.PsiClassImpl
import com.intellij.psi.impl.source.PsiFieldImpl
import com.intellij.psi.impl.source.PsiMethodImpl
import com.intellij.psi.impl.source.PsiParameterImpl
import com.intellij.psi.impl.source.tree.java.PsiLocalVariableImpl
import com.intellij.psi.xml.XmlFile
import com.murphy.config.AndConfigState
import com.murphy.core.*
import com.murphy.core.rename
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
        val dateStart = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
        println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>> $dateStart [Refactor Start] <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<")
        val startTime = System.currentTimeMillis()
        val config = AndConfigState.getInstance()
        config.initRandomNode()
        if (psi is PsiNamedElement) {
            when (psi) {
                is PsiDirectory -> if (config.directoryRule.isNotEmpty()) {
                    psi.rename(config.randomDirectoryName, "Folder")
                }

                is XmlFile, is PsiBinaryFile -> if (config.resFileRule.isNotEmpty()) {
                    psi.rename(config.randomResFileName, "FileRes")
                }

                is PsiParameterImpl, is PsiFieldImpl, is PsiLocalVariableImpl,
                is KtProperty, is KtParameter -> if (config.propertyRule.isNotEmpty()) {
                    psi.rename(config.randomPropertyName, "Field")
                }

                is KtNamedFunction, is PsiMethodImpl -> if (config.functionRule.isNotEmpty()) {
                    psi.rename(config.randomFunctionName, "Method")
                }

                is KtObjectDeclaration, is KtClass, is KtFile,
                is PsiClassImpl -> if (config.classRule.isNotEmpty()) {
                    psi.rename(config.randomClassName, "Class")
                }
            }
        }
        notifyInfo(action.project, "refactor finished, take ${computeTime(startTime)}")
        val dateEnd = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
        println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>> $dateEnd [Refactor End] <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<")
    }

}