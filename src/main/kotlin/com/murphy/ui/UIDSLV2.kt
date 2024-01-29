package com.murphy.ui

import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Component
import java.awt.ComponentOrientation
import java.awt.Container
import java.awt.event.ActionEvent
import javax.swing.*

/**
 * check and handle the parent Component and children Component
 */
fun checkAddView(parent: Any, vararg children: Component) {
    when (parent) {
        is AuxLayout -> {
            for (child in children) {
                parent.addComponent(child)
            }
        }

        is Container -> {
            for (child in children) {
                parent.add(child)
            }
        }
    }
}

/**
 * check and handle the parent Component and child Component with constraintsInParent
 */
fun checkAddView(parent: Any, child: Component, constraintsInParent: Any?) {
    when (parent) {
        is AuxLayout -> {
            parent.addComponent(child)
        }

        is Container -> {
            parent.add(child, constraintsInParent)
        }
    }
}

/**
 * auxiliary layout： help to add child in specific position
 */
interface AuxLayout {
    fun addComponent(comp: Component)
}

/**
 * JHorizontalLinearLayout： Box with BoxLayout.X_AXIS
 */
class JHorizontalLinearLayout : Box(BoxLayout.X_AXIS) {
    /**
     * fill the remaining space for linear layout,like android empty space with weight value
     */
    fun fillSpace() {
        add(createHorizontalGlue())
    }

    /**
     * fill the fixed space for linear layout
     */
    fun fixedSpace(spaceWidth: Int) {
        add(createHorizontalStrut(JBUI.scale(spaceWidth)))
    }
}

/**
 * JVerticalLinearLayout: Box with BoxLayout.Y_AXIS
 */
class JVerticalLinearLayout : Box(BoxLayout.Y_AXIS) {

    /**
     * Space height between lines
     */
    private val lineSpaceHeight = 10

    /**
     * fill the fixed space for linear layout
     */
    fun fixedSpace(spaceHeight: Int) {
        super.add(createVerticalStrut(JBUI.scale(spaceHeight)))
    }

    override fun add(comp: Component?): Component {
        fixedSpace(lineSpaceHeight)
        return super.add(comp)
    }

    /**
     * add component with align left style
     */
    inner class AlignLeftContainer : AuxLayout {
        override fun addComponent(comp: Component) {
            val jPanel = JPanel().apply {
                layout = BoxLayout(this, BoxLayout.LINE_AXIS)
                componentOrientation = ComponentOrientation.LEFT_TO_RIGHT
                val horizontalBox = Box(BoxLayout.X_AXIS)
                horizontalBox.add(comp)
                horizontalBox.add(createHorizontalGlue())
                add(horizontalBox)
            }
            add(jPanel)
        }
    }
}

/**
 * SimpleBorderLayout：JPanel with BorderLayout()
 */
class SimpleBorderLayout : JPanel(BorderLayout()) {
    private var hasPutCenter = false

    fun putCenterFill(comp: Component) {
        if (hasPutCenter) {
            throw IllegalAccessError("Only Could put center fill one time")
        }
        add(comp, BorderLayout.CENTER)
        hasPutCenter = true
    }
}

/**
 * generate a jHorizontalLinearLayout but return with jpanel
 */
fun Any.jHorizontalLinearLayout(init: JHorizontalLinearLayout.() -> Unit): JPanel {
    val horizontalBox = JHorizontalLinearLayout()
    val jPanel = JPanel().apply {
        layout = BoxLayout(this, BoxLayout.LINE_AXIS)
        componentOrientation = ComponentOrientation.LEFT_TO_RIGHT
        horizontalBox.init()
        add(horizontalBox)
    }
    checkAddView(this, jPanel)
    return jPanel
}

/**
 * generate a jVerticalLinearLayout  but return with jpanel
 */
fun Any.jVerticalLinearLayout(
    constraintsInParent: Any? = BorderLayout.CENTER,
    addToParent: Boolean = true,
    init: JVerticalLinearLayout.() -> Unit
): JPanel {

    val jVerticalLinearLayout = JVerticalLinearLayout()
    val jPanel = JPanel().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        componentOrientation = ComponentOrientation.LEFT_TO_RIGHT
        jVerticalLinearLayout.init()
        add(jVerticalLinearLayout)
    }
    if (addToParent) {
        checkAddView(this, jPanel, constraintsInParent)
    }
    return jPanel
}

/**
 * generate a JLabel component
 */
fun Any.jLabel(text: String, textSize: Float = 13f, init: JLabel.() -> Unit = {}): JLabel {
    val jLabel = JLabel(text).apply {
        font = font.deriveFont(textSize)
    }
    jLabel.init()
    checkAddView(this, jLabel)
    return jLabel
}

/**
 * generate a JButton component
 */
fun Any.jButton(text: String = "", clickListener: () -> Unit, init: JButton.() -> Unit = {}): JButton {
    val jButton = JButton(text)
    jButton.init()
    jButton.addActionListener(object : AbstractAction() {
        override fun actionPerformed(p0: ActionEvent?) {
            clickListener()
        }
    })
    checkAddView(this, jButton)
    return jButton
}

/**
 * generate a border layout which for easy adding inner views
 */
fun Any.jBorderLayout(init: SimpleBorderLayout.() -> Unit): JPanel {
    return SimpleBorderLayout().apply {
        init()
        checkAddView(this@jBorderLayout, this@apply)
    }
}

/**
 * the components in alignLeftComponent will be align Left
 *
 * for example：
 *
 * jVerticalLinearLayout{
 *    alignLeftComponent {
 *        jLabel("test")
 *    }
 *}
 */
fun JVerticalLinearLayout.alignLeftComponent(init: JVerticalLinearLayout.AlignLeftContainer.() -> Unit): JVerticalLinearLayout.AlignLeftContainer {
    return AlignLeftContainer().apply(init)
}