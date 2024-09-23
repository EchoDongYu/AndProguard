package com.murphy.config

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import com.murphy.util.RandomNode
import com.murphy.util.RandomNode.Companion.parseNode

@State(
    name = "AndProguardConfigState",
    storages = [Storage("AndProguardConfigState.xml")],
)
class AndConfigState : PersistentStateComponent<AndConfigState> {
    var classRule: String = "{[1000](1)[0100](6,12)}(2,3)"
    var methodRule: String = "[0100](7,13){[1000](1)[0100](6,12)}(0,2)"
    var fieldRule: String = "[0100](7,13){[1000](1)[0100](6,12)}(0,1)"
    var resourceRule: String = "[0100](7,11){<_>[0100](7,11)}(0,1)"
    var fileResRule: String = "[0100](7,11){<_>[0100](7,11)}(1,2)"
    var folderRule: String = ""

    /**
     * @Scope：Java/Kotlin
     * 是否忽略数据内容不处理：
     * 1. Java Bean： Getter/Setter 函数名，以及类中相对应的成员变量名
     * 2. Data Class： 主构造方法的参数名
     */
    var skipData: Boolean = true
    private lateinit var randomNodeList: List<RandomNode>

    val randomClassName get() = randomNodeList[0].randomString
    val randomMethodName get() = randomNodeList[1].randomString
    val randomFieldName get() = randomNodeList[2].randomString
    val randomResourceName get() = randomNodeList[3].randomString
    val randomFileResName get() = randomNodeList[4].randomString
    val randomFolderName get() = randomNodeList[5].randomString

    override fun getState(): AndConfigState = this

    override fun loadState(state: AndConfigState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    fun initRandomNode() {
        randomNodeList = listOf(
            classRule.parseNode(),
            methodRule.parseNode(),
            fieldRule.parseNode(),
            resourceRule.parseNode(),
            fileResRule.parseNode(),
            folderRule.parseNode()
        )
    }

    companion object {
        fun getInstance(): AndConfigState {
            return ApplicationManager.getApplication().getService(AndConfigState::class.java)
        }
    }
}