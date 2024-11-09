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
    private JRadioButton alsoDir;
    private JRadioButton onlyDir;
    private JRadioButton notDir;

    public AndProguardForm(boolean skipData, String classRule, String functionRule, String propertyRule, String resourceRule, String resFileRule, String directoryRule) {
        this.skipData.setSelected(skipData);
        this.classRule.setText(classRule);
        this.functionRule.setText(functionRule);
        this.propertyRule.setText(propertyRule);
        this.resourceRule.setText(resourceRule);
        this.resFileRule.setText(resFileRule);
        this.directoryRule.setText(directoryRule);
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

    public int getDirectoryMode() {
        if (this.notDir.isSelected()) return 1;
        else if (this.onlyDir.isSelected()) return 2;
        else if (this.alsoDir.isSelected()) return 3;
        else return 1;
    }

    public void setDirectoryMode(int value) {
        notDir.setSelected(value != 2 && value != 3);
        onlyDir.setSelected(value == 2);
        alsoDir.setSelected(value == 3);
    }
}
