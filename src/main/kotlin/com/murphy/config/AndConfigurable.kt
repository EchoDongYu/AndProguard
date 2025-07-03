package com.murphy.config

import com.intellij.openapi.options.Configurable
import com.murphy.ui.AndProguardConfig
import javax.swing.JComponent

class AndConfigurable : Configurable {
    private val config by lazy { AndConfigState.getInstance() }
    private val state get() = config.state
    private val form by lazy { AndProguardConfig(state) }

    override fun createComponent(): JComponent? = form.panel

    override fun isModified(): Boolean = form.toState() != state

    override fun apply() = config.loadState(form.toState())

    override fun reset() = form.copyFrom(state)

    override fun getDisplayName(): String = "AndProguard Config"

    private fun AndProguardConfig.copyFrom(state: AndConfigState.State) {
        classRule = state.classRule
        functionRule = state.functionRule
        propertyRule = state.propertyRule
        resourceRule = state.resourceRule
        layoutRule = state.layoutRule
        directoryRule = state.directoryRule
        skipData = state.skipData
        combinations = state.combinations
        digitWeight = state.digitWeight
        underlineWeight = state.underlineWeight
        comboWeight = state.comboWeight
        repeatFactor = state.repeatFactor
    }

    private fun AndProguardConfig.toState(): AndConfigState.State {
        return AndConfigState.State(
            classRule = classRule,
            functionRule = functionRule,
            propertyRule = propertyRule,
            resourceRule = resourceRule,
            layoutRule = layoutRule,
            directoryRule = directoryRule,
            skipData = skipData,
            combinations = combinations,
            digitWeight = digitWeight,
            underlineWeight = underlineWeight,
            comboWeight = comboWeight,
            repeatFactor = repeatFactor
        )
    }
}