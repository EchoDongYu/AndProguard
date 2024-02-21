package com.murphy.config

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import com.murphy.util.RandomNode
import com.murphy.util.RandomNode.Companion.parseNode

@State(
    name = "AndGuardCoinfigState",
    storages = [Storage("AndGuardCoinfigState.xml")],
)
class AndGuardCoinfigState : PersistentStateComponent<AndGuardCoinfigState> {
    var classRule: String = "{[1000](1)[0100](3,9)}(2,3)"
    var methodRule: String = "[0100](4,12){[1000](1)[0100](3,9)}(0,2)"
    var fieldRule: String = "[0100](3,9){[1000](1)[0100](3,8)}(0,1)"
    var idResRule: String = "[0100](4,7){<_>[0100](4,7)}(0,1)"
    var layoutResRule: String = "[0100](4,7){<_>[0100](4,7)}(1,2)"

    /**
     * @Scope：Java/Kotlin
     * 是否忽略数据内容不处理：
     * 1. Java Bean： Getter/Setter 函数名，以及类中相对应的成员变量名
     * 2. Data Class： 主构造方法的参数名
     */
    var skipData: Boolean = true
    private lateinit var randomNodeList: MutableList<RandomNode>

    val randomClassName get() = randomNodeList[0].randomString
    val randomMethodName get() = randomNodeList[1].randomString
    val randomFieldName get() = randomNodeList[2].randomString
    val randomIdResName get() = randomNodeList[3].randomString
    val randomLayoutResName get() = randomNodeList[4].randomString

    override fun getState(): AndGuardCoinfigState = this

    override fun loadState(state: AndGuardCoinfigState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    fun initRandomNode() {
        randomNodeList = mutableListOf(
            classRule.parseNode(),
            methodRule.parseNode(),
            fieldRule.parseNode(),
            idResRule.parseNode(),
            layoutResRule.parseNode()
        )
    }

    companion object {
        fun getInstance(): AndGuardCoinfigState {
            return ApplicationManager.getApplication().getService(AndGuardCoinfigState::class.java)
        }
    }
}