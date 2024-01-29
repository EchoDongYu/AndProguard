package com.murphy.action

import com.ibm.icu.text.SimpleDateFormat
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiJavaFile
import com.murphy.core.MappingGenerator
import com.murphy.core.computeTime
import com.murphy.core.fileList
import com.murphy.ui.MappingDialog
import com.murphy.util.PLUGIN_NAME
import com.murphy.util.notifyError
import com.murphy.util.notifyInfo
import com.murphy.util.notifyWarn
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile
import java.util.*

class AndMapAction : AnAction() {
    override fun actionPerformed(action: AnActionEvent) {
        val psi = action.getData(PlatformDataKeys.PSI_ELEMENT) ?: return
        val inputDialog = MappingDialog(action.project)
        inputDialog.setOkClickListener {
            val mapGen = MappingGenerator(it)
            val dateStart = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
            println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>> $dateStart [Refactor Start] <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<")
            when (psi) {
                is KtClassOrObject -> mapGen.processMapping(psi)
                is PsiClass -> mapGen.processMapping(psi)
                is PsiDirectory -> {
                    val fileList = psi.fileList()
                    if (fileList.isEmpty()) {
                        notifyWarn(action.project, "Nothing to do")
                        return@setOkClickListener
                    }
                    val startTime = System.currentTimeMillis()
                    val size = fileList.size
                    val total: Double = size.toDouble()
                    var count = 0
                    ProgressManager.getInstance().run(object : Task.Modal(action.project, PLUGIN_NAME, false) {
                        override fun run(indicator: ProgressIndicator) {
                            val iterator = fileList.iterator()
                            while (iterator.hasNext()) {
                                ApplicationManager.getApplication().invokeAndWait {
                                    when (val next = iterator.next()) {
                                        is KtFile -> mapGen.processMapping(next)
                                        is PsiJavaFile -> mapGen.processMapping(next)
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
                        }
                    })
                }
            }
            val dateEnd = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
            println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>> $dateEnd [Refactor End] <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<")
        }
        inputDialog.show()
    }
}
