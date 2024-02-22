package com.murphy.config

import com.intellij.openapi.options.Configurable
import com.murphy.AndGuardForm
import javax.swing.JComponent

class AndGuardConfigurable : Configurable {
    private val form by lazy {
        val state = AndGuardCoinfigState.getInstance()
        AndGuardForm(
            state.skipData,
            state.classRule,
            state.methodRule,
            state.fieldRule,
            state.idResRule,
            state.layoutResRule,
            state.excludePath
        )
    }

    override fun createComponent(): JComponent? {
        return form.panel
    }

    override fun isModified(): Boolean {
        val state = AndGuardCoinfigState.getInstance()
        return state.classRule != form.classRule || state.methodRule != form.methodRule ||
                state.fieldRule != form.fieldRule || state.idResRule != form.idResRule ||
                state.layoutResRule != form.layoutResRule || state.skipData != form.skipData ||
                state.excludePath != form.excludePath
    }

    override fun apply() {
        val state = AndGuardCoinfigState.getInstance()
        state.classRule = form.classRule
        state.methodRule = form.methodRule
        state.fieldRule = form.fieldRule
        state.idResRule = form.idResRule
        state.layoutResRule = form.layoutResRule
        state.skipData = form.skipData
        state.excludePath = form.excludePath
    }

    override fun reset() {
        val state = AndGuardCoinfigState.getInstance()
        form.classRule = state.classRule
        form.methodRule = state.methodRule
        form.fieldRule = state.fieldRule
        form.idResRule = state.idResRule
        form.layoutResRule = state.layoutResRule
        form.skipData = state.skipData
        form.excludePath = state.excludePath
    }

    override fun getDisplayName(): String = "AndGuard Config"
}