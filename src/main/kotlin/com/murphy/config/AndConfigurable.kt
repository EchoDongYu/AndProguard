package com.murphy.config

import com.intellij.openapi.options.Configurable
import com.murphy.ui.AndProguardForm
import javax.swing.JComponent

class AndConfigurable : Configurable {
    private val state by lazy { AndConfigState.getInstance() }
    private val form by lazy {
        AndProguardForm(
            state.skipData,
            arrayOf(
                state.classRule, state.functionRule, state.propertyRule,
                state.resourceRule, state.resFileRule, state.directoryRule
            ),
            arrayOf(state.digitWeight, state.underlineWeight, state.comboWeight, state.repeatFactor),
            state.combinations
        )
    }

    override fun createComponent(): JComponent? {
        return form.panel
    }

    override fun isModified(): Boolean {
        return state.classRule != form.classRule || state.functionRule != form.functionRule ||
                state.propertyRule != form.propertyRule || state.resourceRule != form.resourceRule ||
                state.resFileRule != form.resFileRule || state.directoryRule != form.directoryRule ||
                state.skipData != form.skipData || state.combinations != form.combinations ||
                state.digitWeight != form.digitWeight || state.underlineWeight != form.underlineWeight ||
                state.comboWeight != form.comboWeight || state.repeatFactor != form.repeatFactor
    }

    override fun apply() {
        state.classRule = form.classRule
        state.functionRule = form.functionRule
        state.propertyRule = form.propertyRule
        state.resourceRule = form.resourceRule
        state.resFileRule = form.resFileRule
        state.directoryRule = form.directoryRule
        state.skipData = form.skipData
        state.combinations = form.combinations
        state.digitWeight = form.digitWeight
        state.underlineWeight = form.underlineWeight
        state.comboWeight = form.comboWeight
        state.repeatFactor = form.repeatFactor
        state.initNamingConfig()
    }

    override fun reset() {
        form.classRule = state.classRule
        form.functionRule = state.functionRule
        form.propertyRule = state.propertyRule
        form.resourceRule = state.resourceRule
        form.resFileRule = state.resFileRule
        form.directoryRule = state.directoryRule
        form.skipData = state.skipData
        form.combinations = state.combinations
        form.digitWeight = state.digitWeight
        form.underlineWeight = state.underlineWeight
        form.comboWeight = state.comboWeight
        form.repeatFactor = state.repeatFactor
    }

    override fun getDisplayName(): String = "AndProguard Config"
}