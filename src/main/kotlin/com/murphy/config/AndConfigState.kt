package com.murphy.config

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.murphy.util.NamingCombo
import com.murphy.util.NamingNode
import com.murphy.util.parseNode

@State(
    name = "AndProguardConfigState",
    storages = [Storage("andProguardConfig.xml")]
)
class AndConfigState : PersistentStateComponent<AndConfigState.State> {
    data class State(
        val classRule: String = "([^CLW2]{5,12}){2,3}",
        val functionRule: String = "[^LW2]{5,10}([^CLW2]{5,10}){1,2}",
        val propertyRule: String = "[^LW2]{5,12}([^CLW2]{5,12}){0,1}",
        val resourceRule: String = "[^LW2]{5,12}([_][^LW2]{5,12}){0,1}",
        val layoutRule: String = "[^LW2]{5,12}([_][^LW2]{5,12}){1,2}",
        val directoryRule: String = "[^LW2]{5,12}",
        val skipData: Boolean = true,
        val digitWeight: Double = 0.15,
        val underlineWeight: Double = 0.03,
        val comboWeight: Double = 0.2,
        val repeatFactor: Double = 0.3,
        val combinations: String = "ch,sh,th,wh,ph,cl,fl,pl,bl,tr,dr,gr,br,fr,st,sp,sk,sc,sn,sw,ck,ng,nd," +
                "nt,ft,lt,rt,mp,lp,rk,ai,au,ei,eu,ie,oi,ou,oo,ea,ee,oa,qu,un,re,in,de,ed,ly,er," +
                "str,thr,spl,scr,spr,shr,sch,tch,nth,rch,nch,lch,rth,mpt,lpt,ing,iou,eea,eau"
    )

    @Transient
    lateinit var namingNodes: List<NamingNode>

    private var myState = State()
    override fun getState(): AndConfigState.State = myState
    override fun loadState(state: AndConfigState.State) {
        myState = state
        initNamingConfig()
    }

    override fun noStateLoaded() {
        super.noStateLoaded()
        initNamingConfig()
    }

    private fun initNamingConfig() {
        NamingCombo.initCombo(myState.combinations)
        namingNodes = listOf(
            myState.classRule.parseNode(),
            myState.functionRule.parseNode(),
            myState.propertyRule.parseNode(),
            myState.resourceRule.parseNode(),
            myState.layoutRule.parseNode(),
            myState.directoryRule.parseNode()
        )
    }

    companion object {
        fun getInstance(): AndConfigState {
            return ApplicationManager.getApplication().getService(AndConfigState::class.java)
        }
    }
}