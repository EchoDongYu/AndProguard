package com.murphy.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.murphy.config.CustomDialog
import com.murphy.core.computeTime
import com.murphy.core.obfuscateByCheck
import com.murphy.util.LogUtil
import com.murphy.util.notifyInfo

class ObfuscateCustomAction : AnAction() {

    override fun actionPerformed(action: AnActionEvent) {
        val myPsi = action.getData(PlatformDataKeys.PSI_ELEMENT) ?: return
        val myProject = action.project ?: return
        val label = "Obfuscate Custom"
        LogUtil.logRecord(myProject, label, false)
        ApplicationManager.getApplication().invokeLater {
            val dialog = CustomDialog()
            dialog.setListener {
                val startTime = System.currentTimeMillis()
                obfuscateByCheck(myProject, myPsi, it) {
                    LogUtil.logRecord(myProject, label, true)
                    notifyInfo(myProject, "refactor finished, take ${computeTime(startTime)}")
                }
            }
            dialog.show()
        }
    }

}