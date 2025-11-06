package com.murphy.config

import com.intellij.openapi.ui.DialogWrapper
import com.murphy.core.CustomCheck
import org.jdesktop.swingx.HorizontalLayout
import java.awt.BorderLayout
import javax.swing.BorderFactory
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JPanel

class CustomDialog : DialogWrapper(true) {
    private val ktFileCheckBox = JCheckBox("KtFile")
    private val classCheckBox = JCheckBox("Class")
    private val functionCheckBox = JCheckBox("Function")
    private val variableCheckBox = JCheckBox("Variable")
    private val resourceCheckBox = JCheckBox("Resource")
    private val directoryCheckBox = JCheckBox("Directory")

    private var listener: ActionListener? = null

    init {
        title = "Obfuscate Custom"
        init() // Required to initialize dialog properly
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout())
        val checkboxPanel = JPanel()
        checkboxPanel.setLayout(HorizontalLayout(10))
        checkboxPanel.add(ktFileCheckBox)
        checkboxPanel.add(classCheckBox)
        checkboxPanel.add(functionCheckBox)
        checkboxPanel.add(variableCheckBox)
        checkboxPanel.add(resourceCheckBox)
        checkboxPanel.add(directoryCheckBox)

        panel.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
        panel.add(checkboxPanel, BorderLayout.CENTER)
        return panel
    }

    override fun doOKAction() {
        listener?.onClick(
            CustomCheck(
                ktFile = ktFileCheckBox.isSelected,
                clazz = classCheckBox.isSelected,
                function = functionCheckBox.isSelected,
                variable = variableCheckBox.isSelected,
                resource = resourceCheckBox.isSelected,
                directory = directoryCheckBox.isSelected
            )
        )
        super.doOKAction() // closes the dialog
    }

    interface ActionListener {
        fun onClick(check: CustomCheck)
    }

    fun setListener(callback: (CustomCheck) -> Unit) {
        this.listener = object : ActionListener {
            override fun onClick(check: CustomCheck) {
                callback.invoke(check)
            }
        }
    }
}