package com.murphy.action

import com.ibm.icu.text.SimpleDateFormat
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.psi.PsiBinaryFile
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.xml.XmlFile
import com.murphy.core.*
import com.murphy.util.*
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile
import java.util.*

class AndGuardAction : AnAction() {

    override fun actionPerformed(action: AnActionEvent) {
        val psi = action.getData(PlatformDataKeys.PSI_ELEMENT) ?: return
        val dateStart = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
        println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>> $dateStart [Refactor Start] <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<")
        when (psi) {
            is KtClassOrObject -> processKotlin(psi)
            is PsiClass -> processJava(psi)
            is XmlFile -> processXml(psi)
            is PsiBinaryFile -> psi.rename(randomResFileName, "File")
            is PsiDirectory -> {
                val fileList = psi.fileList()
                if (fileList.isEmpty()) {
                    notifyWarn(action.project, "Nothing to do")
                    return
                }
                val startTime = System.currentTimeMillis()
                val size = fileList.size
                val total: Double = size.toDouble()
                var count = 0
                val resIdList: MutableList<String> = LinkedList()
                ProgressManager.getInstance().run(object : Task.Modal(action.project, PLUGIN_NAME, false) {
                    override fun run(indicator: ProgressIndicator) {
                        indicator.isIndeterminate = false
                        val iterator = fileList.iterator()
                        while (iterator.hasNext()) {
                            ApplicationManager.getApplication().invokeAndWait {
                                when (val next = iterator.next()) {
                                    is PsiJavaFile -> processJava(next)
                                    is KtFile -> processKotlin(next)
                                    is XmlFile -> processXml(next, resIdList)
                                    is PsiBinaryFile -> next.rename(randomResFileName, "File")
                                }
                            }
                            indicator.fraction = ++count / total
                            indicator.text = "$count files of $size files"
                            iterator.remove()
                        }
                    }

                    override fun onSuccess() {
                        notifyInfo(action.project, "$size files refactor finished, take ${computeTime(startTime)}")
                    }

                    override fun onThrowable(error: Throwable) {
                        notifyError(action.project, "${error.message}")
                        error.printStackTrace()
                    }

                    override fun onFinished() {
                        resIdList.clear()
                    }
                })
            }
        }
        val dateEnd = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
        println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>> $dateEnd [Refactor End] <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<")
    }

}