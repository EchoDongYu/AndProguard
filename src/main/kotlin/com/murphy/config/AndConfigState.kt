package com.murphy.config

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import com.murphy.util.NamingCombo
import com.murphy.util.NamingNode
import com.murphy.util.parseNode

@State(
    name = "AndProguardConfigState",
    storages = [Storage("AndProguardConfigState.xml")],
)
class AndConfigState : PersistentStateComponent<AndConfigState> {
    var classRule: String = "([^CLW2]{5,12}){2,3}"
    var functionRule: String = "[^LW2]{5,10}([^CLW2]{5,10}){1,2}"
    var propertyRule: String = "[^LW2]{5,12}([^CLW2]{5,12}){0,1}"
    var resourceRule: String = "[^LW2]{5,12}([_][^LW2]{5,12}){0,1}"
    var resFileRule: String = "[^LW2]{5,12}([_][^LW2]{5,12}){1,2}"
    var directoryRule: String = "[^LW2]{5,12}"

    /**
     * @Scope：Java/Kotlin
     * 是否忽略数据内容不处理：
     * 1. Java Bean： Getter/Setter 函数名，以及类中相对应的成员变量名
     * 2. Data Class： 主构造方法的参数名
     */
    var skipData: Boolean = true
    var digitWeight = 0.15
    var underlineWeight = 0.07
    var comboWeight = 0.2
    var repeatFactor = 0.3
    var combinations = "ch,sh,th,wh,ph,cl,fl,pl,bl,tr,dr,gr,br,fr,st,sp,sk,sc,sn,sw,ck,ng,nd," +
            "nt,ft,lt,rt,mp,lp,rk,ai,au,ei,eu,ie,oi,ou,oo,ea,ee,oa,qu,un,re,in,de,ed,ly,er," +
            "str,thr,spl,scr,spr,shr,sch,tch,nth,rch,nch,lch,rth,mpt,lpt,ing,iou,eea,eau"
    private lateinit var namingNodes: List<NamingNode>

    val randomClassName get() = namingNodes[0].randomNaming
    val randomFunctionName get() = namingNodes[1].randomNaming
    val randomPropertyName get() = namingNodes[2].randomNaming
    val randomResourceName get() = namingNodes[3].randomNaming
    val randomResFileName get() = namingNodes[4].randomNaming
    val randomDirectoryName get() = namingNodes[5].randomNaming

    override fun getState(): AndConfigState = this

    override fun loadState(state: AndConfigState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    override fun initializeComponent() {
        super.initializeComponent()
        initNamingConfig()
    }

    fun initNamingConfig() {
        NamingCombo.initCombo(combinations)
        namingNodes = listOf(
            classRule.parseNode(),
            functionRule.parseNode(),
            propertyRule.parseNode(),
            resourceRule.parseNode(),
            resFileRule.parseNode(),
            directoryRule.parseNode()
        )
    }

    companion object {
        fun getInstance(): AndConfigState {
            return ApplicationManager.getApplication().getService(AndConfigState::class.java)
        }
    }
}