package com.murphy.action

import com.ibm.icu.text.SimpleDateFormat
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.psi.PsiNamedElement
import com.murphy.config.AndProguardConfigState
import com.murphy.core.*
import com.murphy.util.PLUGIN_NAME
import com.murphy.util.notifyError
import com.murphy.util.notifyInfo
import java.util.*

class AndProguardAction : AnAction() {

    override fun actionPerformed(action: AnActionEvent) {
        val psi = action.getData(PlatformDataKeys.PSI_ELEMENT) ?: return
        val dateStart = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
        println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>> $dateStart [Refactor Start] <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<")
        AndProguardConfigState.getInstance().initRandomNode()
        val startTime = System.currentTimeMillis()
        ProgressManager.getInstance().run(object : Task.Modal(action.project, PLUGIN_NAME, false) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = false
                indicator.fraction = 0.0
                runReadAction { psi.childrenDfsSequence().filterIsInstance<PsiNamedElement>() }.run {
                    processKotlin(indicator)
                    processJava(indicator)
                    processXml(indicator)
                }
            }

            override fun onSuccess() {
                notifyInfo(project, "refactor finished, take ${computeTime(startTime)}")
            }

            override fun onThrowable(error: Throwable) {
                notifyError(action.project, "${error.message}")
                error.printStackTrace()
            }
        })
        val dateEnd = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
        println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>> $dateEnd [Refactor End] <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<")
    }

}