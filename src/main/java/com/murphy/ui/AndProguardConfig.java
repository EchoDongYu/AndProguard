package com.murphy.ui;

import com.murphy.config.AndConfigState;

import javax.swing.*;

public class AndProguardConfig {
    private JPanel rootPane;
    private JCheckBox skipData;
    private JTextField classRule;
    private JTextField functionRule;
    private JTextField propertyRule;
    private JTextField resourceRule;
    private JTextField layoutRule;
    private JTextField directoryRule;
    private JTextField digitWeight;
    private JTextField underlineWeight;
    private JTextField comboWeight;
    private JTextField repeatFactor;
    private JTextArea combinations;
    private JSlider digitSlider;
    private JSlider underlineSlider;
    private JSlider comboSlider;
    private JSlider repeatSlider;

    public AndProguardConfig(AndConfigState.State state) {
        this.skipData.setSelected(state.getSkipData());
        this.classRule.setText(state.getClassRule());
        this.functionRule.setText(state.getFunctionRule());
        this.propertyRule.setText(state.getPropertyRule());
        this.resourceRule.setText(state.getResourceRule());
        this.layoutRule.setText(state.getLayoutRule());
        this.directoryRule.setText(state.getDirectoryRule());
        this.combinations.setText(state.getCombinations());
        setDigitWeight(state.getDigitWeight());
        setUnderlineWeight(state.getUnderlineWeight());
        setComboWeight(state.getComboWeight());
        setRepeatFactor(state.getRepeatFactor());
        this.digitSlider.setValue((int) (state.getDigitWeight() * 100));
        this.underlineSlider.setValue((int) (state.getUnderlineWeight() * 100));
        this.comboSlider.setValue((int) (state.getComboWeight() * 100));
        this.repeatSlider.setValue((int) (state.getRepeatFactor() * 100));
        this.digitSlider.addChangeListener(it -> {
            JSlider source = (JSlider) it.getSource();
            setDigitWeight(source.getModel().getValue() / 100.0);
        });
        this.underlineSlider.addChangeListener(it -> {
            JSlider source = (JSlider) it.getSource();
            setUnderlineWeight(source.getModel().getValue() / 100.0);
        });
        this.comboSlider.addChangeListener(it -> {
            JSlider source = (JSlider) it.getSource();
            setComboWeight(source.getModel().getValue() / 100.0);
        });
        this.repeatSlider.addChangeListener(it -> {
            JSlider source = (JSlider) it.getSource();
            setRepeatFactor(source.getModel().getValue() / 100.0);
        });
    }

    public JPanel getPanel() {
        return rootPane;
    }

    public String getClassRule() {
        return classRule.getText();
    }

    public void setClassRule(String classRule) {
        this.classRule.setText(classRule);
    }

    public String getFunctionRule() {
        return functionRule.getText();
    }

    public void setFunctionRule(String functionRule) {
        this.functionRule.setText(functionRule);
    }

    public String getPropertyRule() {
        return propertyRule.getText();
    }

    public void setPropertyRule(String propertyRule) {
        this.propertyRule.setText(propertyRule);
    }

    public String getResourceRule() {
        return resourceRule.getText();
    }

    public void setResourceRule(String resourceRule) {
        this.resourceRule.setText(resourceRule);
    }

    public String getLayoutRule() {
        return layoutRule.getText();
    }

    public void setLayoutRule(String resFileRule) {
        this.layoutRule.setText(resFileRule);
    }

    public String getDirectoryRule() {
        return directoryRule.getText();
    }

    public void setDirectoryRule(String directoryRule) {
        this.directoryRule.setText(directoryRule);
    }

    public boolean getSkipData() {
        return skipData.isSelected();
    }

    public void setSkipData(boolean skipData) {
        this.skipData.setSelected(skipData);
    }

    public double getDigitWeight() {
        return Double.parseDouble(digitWeight.getText());
    }

    public void setDigitWeight(double digitWeight) {
        this.digitWeight.setText(String.valueOf(digitWeight));
    }

    public double getUnderlineWeight() {
        return Double.parseDouble(underlineWeight.getText());
    }

    public void setUnderlineWeight(double underlineWeight) {
        this.underlineWeight.setText(String.valueOf(underlineWeight));
    }

    public double getComboWeight() {
        return Double.parseDouble(comboWeight.getText());
    }

    public void setComboWeight(double comboWeight) {
        this.comboWeight.setText(String.valueOf(comboWeight));
    }

    public double getRepeatFactor() {
        return Double.parseDouble(repeatFactor.getText());
    }

    public void setRepeatFactor(double repeatFactor) {
        this.repeatFactor.setText(String.valueOf(repeatFactor));
    }

    public String getCombinations() {
        return combinations.getText();
    }

    public void setCombinations(String combinations) {
        this.combinations.setText(combinations);
    }
}
