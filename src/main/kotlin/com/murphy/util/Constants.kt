package com.murphy.util

const val PLUGIN_NAME = "AndGuard"

const val UPPERCASE_LETTER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"

const val LOWERCASE_LETTER = "abcdefghijklmnopqrstuvwxyz"

const val KOTLIN_SUFFIX = ".kt"

const val XML_SUFFIX = ".xml"

/**
 * @Scope：Java/Kotlin
 * 是否忽略数据内容不处理：
 * 1. Java Bean： Getter/Setter 函数名，以及类中相对应的成员变量名
 * 2. Data Class： 主构造方法的参数名
 */
const val SKIP_DATA = true