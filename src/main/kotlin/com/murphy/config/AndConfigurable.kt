package com.murphy.config

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.ui.dsl.builder.*
import javax.swing.JSlider

class AndConfigurable : BoundConfigurable("AndProguard Config") {
    private val config by lazy { AndConfigState.getInstance() }

    // Mutable state copies for DSL bindings
    private var classRule = ""
    private var functionRule = ""
    private var propertyRule = ""
    private var resourceRule = ""
    private var layoutRule = ""
    private var directoryRule = ""
    private var skipData = true
    private var digitWeight = 0.15
    private var underlineWeight = 0.03
    private var comboWeight = 0.2
    private var repeatFactor = 0.3
    private var combinations = ""

    // Slider references for manual reset
    private var digitSliderRef: JSlider? = null
    private var underlineSliderRef: JSlider? = null
    private var comboSliderRef: JSlider? = null
    private var repeatSliderRef: JSlider? = null

    init {
        loadFromState()
    }

    private fun loadFromState() {
        val s = config.state
        classRule = s.classRule
        functionRule = s.functionRule
        propertyRule = s.propertyRule
        resourceRule = s.resourceRule
        layoutRule = s.layoutRule
        directoryRule = s.directoryRule
        skipData = s.skipData
        digitWeight = s.digitWeight
        underlineWeight = s.underlineWeight
        comboWeight = s.comboWeight
        repeatFactor = s.repeatFactor
        combinations = s.combinations
    }

    override fun createPanel() = panel {
        group("Custom Naming Rule") {
            row("Class:") {
                textField().columns(COLUMNS_LARGE).align(Align.FILL).bindText(::classRule)
            }
            row("Function:") {
                textField().columns(COLUMNS_LARGE).align(Align.FILL).bindText(::functionRule)
            }
            row("Property:") {
                textField().columns(COLUMNS_LARGE).align(Align.FILL).bindText(::propertyRule)
            }
            row("Resource:") {
                textField().columns(COLUMNS_LARGE).align(Align.FILL).bindText(::resourceRule)
            }
            row("Layout:") {
                textField().columns(COLUMNS_LARGE).align(Align.FILL).bindText(::layoutRule)
            }
            row("Directory:") {
                textField().columns(COLUMNS_LARGE).align(Align.FILL).bindText(::directoryRule)
            }
        }

        row { checkBox("Skip Java bean or Kotlin data").bindSelected(::skipData) }

        group("Weight Settings") {
            row("DigitWeight:") {
                val tf = textField()
                    .columns(COLUMNS_SHORT)
                    .bindText(
                        { digitWeight.toString() },
                        { digitWeight = it.toDoubleOrNull() ?: digitWeight }
                    )
                    .component
                slider(0, 100, 1, 10).align(Align.FILL).applyToComponent {
                    digitSliderRef = this
                    value = (digitWeight * 100).toInt()
                    addChangeListener { tf.text = (value / 100.0).toString() }
                }
            }
            row("UnderlineWeight:") {
                val tf = textField()
                    .columns(COLUMNS_SHORT)
                    .bindText(
                        { underlineWeight.toString() },
                        { underlineWeight = it.toDoubleOrNull() ?: underlineWeight }
                    )
                    .component
                slider(0, 100, 1, 10).align(Align.FILL).applyToComponent {
                    underlineSliderRef = this
                    value = (underlineWeight * 100).toInt()
                    addChangeListener { tf.text = (value / 100.0).toString() }
                }
            }
            row("ComboWeight:") {
                val tf = textField()
                    .columns(COLUMNS_SHORT)
                    .bindText(
                        { comboWeight.toString() },
                        { comboWeight = it.toDoubleOrNull() ?: comboWeight }
                    )
                    .component
                slider(0, 100, 1, 10).align(Align.FILL).applyToComponent {
                    comboSliderRef = this
                    value = (comboWeight * 100).toInt()
                    addChangeListener { tf.text = (value / 100.0).toString() }
                }
            }
            row("RepeatFactor:") {
                val tf = textField()
                    .columns(COLUMNS_SHORT)
                    .bindText(
                        { repeatFactor.toString() },
                        { repeatFactor = it.toDoubleOrNull() ?: repeatFactor }
                    )
                    .component
                slider(0, 100, 1, 10).align(Align.FILL).applyToComponent {
                    repeatSliderRef = this
                    value = (repeatFactor * 100).toInt()
                    addChangeListener { tf.text = (value / 100.0).toString() }
                }
            }
        }

        row("Combinations:") {
            textArea()
                .align(Align.FILL)
                .applyToComponent {
                    rows = 5
                    lineWrap = true
                    wrapStyleWord = true
                }
                .bindText(::combinations)
        }.resizableRow()
    }

    override fun reset() {
        loadFromState()
        super.reset()
        // Reset sliders manually since they have no DSL binding
        digitSliderRef?.value = (digitWeight * 100).toInt()
        underlineSliderRef?.value = (underlineWeight * 100).toInt()
        comboSliderRef?.value = (comboWeight * 100).toInt()
        repeatSliderRef?.value = (repeatFactor * 100).toInt()
    }

    override fun apply() {
        super.apply()
        config.loadState(
            AndConfigState.State(
                classRule = classRule,
                functionRule = functionRule,
                propertyRule = propertyRule,
                resourceRule = resourceRule,
                layoutRule = layoutRule,
                directoryRule = directoryRule,
                skipData = skipData,
                digitWeight = digitWeight,
                underlineWeight = underlineWeight,
                comboWeight = comboWeight,
                repeatFactor = repeatFactor,
                combinations = combinations
            )
        )
    }
}