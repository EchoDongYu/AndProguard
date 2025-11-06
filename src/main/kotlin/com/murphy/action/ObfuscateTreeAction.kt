package com.murphy.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.murphy.core.CustomCheck
import com.murphy.core.computeTime
import com.murphy.core.obfuscateByCheck
import com.murphy.util.LogUtil
import com.murphy.util.notifyInfo

class ObfuscateTreeAction : AnAction() {

    override fun actionPerformed(action: AnActionEvent) {
        val myPsi = action.getData(PlatformDataKeys.PSI_ELEMENT) ?: return
        val myProject = action.project ?: return
        val label = "Obfuscate Tree"
        LogUtil.logRecord(myProject, label, false)
        val startTime = System.currentTimeMillis()
        obfuscateByCheck(myProject, myPsi, CustomCheck(directory = false)) {
            LogUtil.logRecord(myProject, label, true)
            notifyInfo(myProject, "refactor finished, take ${computeTime(startTime)}")
        }
    }

}