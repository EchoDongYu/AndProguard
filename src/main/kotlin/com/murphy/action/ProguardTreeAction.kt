package com.murphy.action

import com.ibm.icu.text.SimpleDateFormat
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiNamedElement
import com.murphy.config.AndConfigState
import com.murphy.core.*
import com.murphy.util.PLUGIN_NAME
import com.murphy.util.notifyError
import com.murphy.util.notifyInfo
import java.util.*

class ProguardTreeAction : AnAction() {

    override fun actionPerformed(action: AnActionEvent) {
        val myPsi = action.getData(PlatformDataKeys.PSI_ELEMENT) ?: return
        val myProject = action.project ?: return
        val dateStart = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
        println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>> $dateStart [Refactor Start] <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<")
        AndConfigState.getInstance().initRandomNode()
        val startTime = System.currentTimeMillis()
        val generators = arrayOf(
            KotlinPreGenerator, JavaPreGenerator,
            KotlinGenerator, JavaGenerator,
            ResourceGenerator, FileGenerator
        )
        ProgressManager.getInstance().run(object : Task.Modal(myProject, PLUGIN_NAME, false) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = false
                indicator.fraction = 0.0

                DumbService.getInstance(myProject).dumbReadAction {
                    myPsi.childrenDfsSequence().filterIsInstance<PsiNamedElement>().toList()
                }.run {
                    generators.forEach { it.process(myProject, indicator, this) }
                }
            }

            override fun onSuccess() {
                notifyInfo(myProject, "refactor finished, take ${computeTime(startTime)}")
            }

            override fun onThrowable(error: Throwable) {
                notifyError(myProject, "${error.message}")
                error.printStackTrace()
            }
        })
        val dateEnd = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
        println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>> $dateEnd [Refactor End] <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<")
    }

}