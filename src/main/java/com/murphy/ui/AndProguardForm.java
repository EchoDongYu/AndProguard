package com.murphy.ui;

import javax.swing.*;

public class AndProguardForm {
    private JPanel rootPane;
    private JCheckBox skipData;
    private JTextField classRule;
    private JTextField methodRule;
    private JTextField fieldRule;
    private JTextField resourceRule;
    private JTextField fileResRule;
    private JTextField folderRule;

    public AndProguardForm(boolean skipData, String classRule, String methodRule, String fieldRule, String resourceRule, String fileResRule, String folderRule) {
        this.skipData.setSelected(skipData);
        this.classRule.setText(classRule);
        this.methodRule.setText(methodRule);
        this.fieldRule.setText(fieldRule);
        this.resourceRule.setText(resourceRule);
        this.fileResRule.setText(fileResRule);
        this.folderRule.setText(folderRule);
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

    public String getMethodRule() {
        return methodRule.getText();
    }

    public void setMethodRule(String methodRule) {
        this.methodRule.setText(methodRule);
    }

    public String getFieldRule() {
        return fieldRule.getText();
    }

    public void setFieldRule(String fieldRule) {
        this.fieldRule.setText(fieldRule);
    }

    public String getResourceRule() {
        return resourceRule.getText();
    }

    public void setResourceRule(String resourceRule) {
        this.resourceRule.setText(resourceRule);
    }

    public String getFileResRule() {
        return fileResRule.getText();
    }

    public void setFileResRule(String fileResRule) {
        this.fileResRule.setText(fileResRule);
    }

    public String getFolderRule() {
        return folderRule.getText();
    }

    public void setFolderRule(String folderRule) {
        this.folderRule.setText(folderRule);
    }

    public boolean getSkipData() {
        return skipData.isSelected();
    }

    public void setSkipData(boolean skipData) {
        this.skipData.setSelected(skipData);
    }
}
