package com.murphy.config

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import com.murphy.util.naming_consonants
import com.murphy.util.randomString
import com.murphy.util.naming_vowels
import dk.brics.automaton.Automaton
import dk.brics.automaton.RegExp

@State(
    name = "AndProguardConfigState",
    storages = [Storage("AndProguardConfigState.xml")],
)
class AndConfigState : PersistentStateComponent<AndConfigState> {
    var classRule: String = "(([#V]([#c][#v]){3,6}|[#C]([#v][#c]){3,6})){2,3}"
    var functionRule: String = "[#c]?([#v][#c]){3,6}(([#V]([#c][#v]){3,6}|[#C]([#v][#c]){3,6})){1,2}"
    var propertyRule: String = "[#c]?([#v][#c]){3,6}(([#V]([#c][#v]){3,6}|[#C]([#v][#c]){3,6})){1,2}"
    var resourceRule: String = "[#c]?([#v][#c]){3,6}(([#V]([#c][#v]){3,6}|[#C]([#v][#c]){3,6})){1,2}"
    var resFileRule: String = "[#c]?([#v][#c]){3,6}(([#V]([#c][#v]){3,6}|[#C]([#v][#c]){3,6})){1,2}"
    var directoryRule: String = "[#c]?([#v][#c]){2,6}[#v]"

    /**
     * @Scope：Java/Kotlin
     * 是否忽略数据内容不处理：
     * 1. Java Bean： Getter/Setter 函数名，以及类中相对应的成员变量名
     * 2. Data Class： 主构造方法的参数名
     */
    var skipData: Boolean = true
    private lateinit var randomNodeList: List<Automaton>

    val randomClassName get() = randomNodeList[0].randomString
    val randomFunctionName get() = randomNodeList[1].randomString
    val randomPropertyName get() = randomNodeList[2].randomString
    val randomResourceName get() = randomNodeList[3].randomString
    val randomResFileName get() = randomNodeList[4].randomString
    val randomDirectoryName get() = randomNodeList[5].randomString

    override fun getState(): AndConfigState = this

    override fun loadState(state: AndConfigState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    /**
     * 校验正则表达式的有效性，并构建自动机
     */
    fun initRandomNode() {
        val upperVowels = naming_vowels.uppercase()
        val upperConsonants = naming_consonants.uppercase()
        randomNodeList = listOf(
            classRule,
            functionRule,
            propertyRule,
            resourceRule,
            resFileRule,
            directoryRule
        ).map {
            it.replace("[#v]", "[$naming_vowels]", ignoreCase = false)
                .replace("[#c]", "[$naming_consonants]", ignoreCase = false)
                .replace("[#V]", "[$upperVowels]", ignoreCase = false)
                .replace("[#C]", "[$upperConsonants]", ignoreCase = false)
        }.map { RegExp(it).toAutomaton() }.onEach {
            if (!it.isFinite) throw IllegalArgumentException("Regex must be finite: $it")
        }
    }

    companion object {
        fun getInstance(): AndConfigState {
            return ApplicationManager.getApplication().getService(AndConfigState::class.java)
        }
    }
}