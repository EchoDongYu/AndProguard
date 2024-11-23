package com.murphy.ui;

import javax.swing.*;

public class AndProguardForm {
    private JPanel rootPane;
    private JCheckBox skipData;
    private JTextField classRule;
    private JTextField functionRule;
    private JTextField propertyRule;
    private JTextField resourceRule;
    private JTextField resFileRule;
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

    public AndProguardForm(boolean skipData, String[] rule, Double[] weight, String combinations) {
        this.skipData.setSelected(skipData);
        this.classRule.setText(rule[0]);
        this.functionRule.setText(rule[1]);
        this.propertyRule.setText(rule[2]);
        this.resourceRule.setText(rule[3]);
        this.resFileRule.setText(rule[4]);
        this.directoryRule.setText(rule[5]);
        this.combinations.setText(combinations);
        setDigitWeight(weight[0]);
        setUnderlineWeight(weight[1]);
        setComboWeight(weight[2]);
        setRepeatFactor(weight[3]);
        this.digitSlider.setValue((int) (weight[0] * 100));
        this.underlineSlider.setValue((int) (weight[1] * 100));
        this.comboSlider.setValue((int) (weight[2] * 100));
        this.repeatSlider.setValue((int) (weight[3] * 100));
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

    public String getResFileRule() {
        return resFileRule.getText();
    }

    public void setResFileRule(String resFileRule) {
        this.resFileRule.setText(resFileRule);
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
