package com.murphy.config

import com.intellij.openapi.options.Configurable
import com.murphy.ui.AndProguardForm
import javax.swing.JComponent

class AndConfigurable : Configurable {
    private val form by lazy {
        val state = AndConfigState.getInstance()
        AndProguardForm(
            state.skipData,
            state.classRule,
            state.functionRule,
            state.propertyRule,
            state.resourceRule,
            state.resFileRule,
            state.directoryRule
        )
    }

    override fun createComponent(): JComponent? {
        return form.panel
    }

    override fun isModified(): Boolean {
        val state = AndConfigState.getInstance()
        return state.classRule != form.classRule || state.functionRule != form.functionRule ||
                state.propertyRule != form.propertyRule || state.resourceRule != form.resourceRule ||
                state.resFileRule != form.resFileRule || state.directoryRule != form.directoryRule ||
                state.skipData != form.skipData
    }

    override fun apply() {
        val state = AndConfigState.getInstance()
        state.classRule = form.classRule
        state.functionRule = form.functionRule
        state.propertyRule = form.propertyRule
        state.resourceRule = form.resourceRule
        state.resFileRule = form.resFileRule
        state.directoryRule = form.directoryRule
        state.skipData = form.skipData
    }

    override fun reset() {
        val state = AndConfigState.getInstance()
        form.classRule = state.classRule
        form.functionRule = state.functionRule
        form.propertyRule = state.propertyRule
        form.resourceRule = state.resourceRule
        form.resFileRule = state.resFileRule
        form.directoryRule = state.directoryRule
        form.skipData = state.skipData
    }

    override fun getDisplayName(): String = "AndProguard Config"
}