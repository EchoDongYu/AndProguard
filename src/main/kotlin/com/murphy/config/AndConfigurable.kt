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
            state.methodRule,
            state.fieldRule,
            state.resourceRule,
            state.fileResRule,
            state.folderRule
        )
    }

    override fun createComponent(): JComponent? {
        return form.panel
    }

    override fun isModified(): Boolean {
        val state = AndConfigState.getInstance()
        return state.classRule != form.classRule || state.methodRule != form.methodRule ||
                state.fieldRule != form.fieldRule || state.resourceRule != form.resourceRule ||
                state.fileResRule != form.fileResRule || state.folderRule != form.folderRule ||
                state.skipData != form.skipData
    }

    override fun apply() {
        val state = AndConfigState.getInstance()
        state.classRule = form.classRule
        state.methodRule = form.methodRule
        state.fieldRule = form.fieldRule
        state.resourceRule = form.resourceRule
        state.fileResRule = form.fileResRule
        state.folderRule = form.folderRule
        state.skipData = form.skipData
    }

    override fun reset() {
        val state = AndConfigState.getInstance()
        form.classRule = state.classRule
        form.methodRule = state.methodRule
        form.fieldRule = state.fieldRule
        form.resourceRule = state.resourceRule
        form.fileResRule = state.fileResRule
        form.folderRule = state.folderRule
        form.skipData = state.skipData
    }

    override fun getDisplayName(): String = "AndProguard Config"
}