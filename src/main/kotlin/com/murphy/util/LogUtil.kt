package com.murphy.util

import com.intellij.openapi.project.Project
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object LogUtil {
    /**
     * 记录日志信息
     * @param message 日志内容
     */
    fun info(myProject: Project, message: String) {
        val formatNow = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        println(message)
        writeToFile(message, "${myProject.basePath}/mapping_log_$formatNow.txt")
    }

    fun logRecord(myProject: Project, label: String, end: Boolean) {
        val formatNow = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        val flag = if (end) "End" else "Start"
        val message = ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>> $flag [$label] $formatNow <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<"
        info(myProject, message)
    }

    /**
     * 写入日志到指定文件
     * @param message 日志内容
     * @param filePath 日志文件路径
     */
    private fun writeToFile(message: String, filePath: String) {
        try {
            val file = File(filePath)
            if (!file.exists()) file.createNewFile()

            BufferedWriter(FileWriter(file, true)).use { writer ->
                writer.write(message)
                writer.newLine()
            }
        } catch (e: IOException) {
            println("Failed to write log to file. ${e.message}")
        }
    }
}
