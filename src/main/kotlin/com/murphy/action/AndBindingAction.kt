package com.murphy.action

import com.ibm.icu.text.SimpleDateFormat
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.psi.PsiDirectory
import com.intellij.psi.xml.XmlFile
import com.murphy.core.computeTime
import com.murphy.core.fileList
import com.murphy.core.processViewBinding
import com.murphy.util.PLUGIN_NAME
import com.murphy.util.notifyError
import com.murphy.util.notifyInfo
import com.murphy.util.notifyWarn
import java.util.*

class AndBindingAction : AnAction() {

    override fun actionPerformed(action: AnActionEvent) {
        val psi = action.getData(PlatformDataKeys.PSI_ELEMENT) ?: return
        val dateStart = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
        println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>> $dateStart [Refactor Start] <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<")
        when (psi) {
            is XmlFile -> processViewBinding(psi)
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
                val idList: MutableList<String> = LinkedList()
                ProgressManager.getInstance().run(object : Task.Modal(action.project, PLUGIN_NAME, false) {
                    override fun run(indicator: ProgressIndicator) {
                        val iterator = fileList.iterator()
                        while (iterator.hasNext()) {
                            ApplicationManager.getApplication().invokeAndWait {
                                val next = iterator.next()
                                if (next is XmlFile) processViewBinding(next, idList)
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

                    override fun onFinished() {
                        idList.clear()
                    }
                })
            }
        }
        val dateEnd = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
        println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>> $dateEnd [Refactor End] <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<")
    }

}
