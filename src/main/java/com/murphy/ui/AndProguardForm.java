package com.murphy.ui;

import javax.swing.*;

public class AndProguardForm {
    private JPanel rootPane;
    private JCheckBox skipData;
    private JTextField classRule;
    private JTextField methodRule;
    private JTextField fieldRule;
    private JTextField idResRule;
    private JTextField layoutResRule;
    private JTextField excludePath;

    public AndProguardForm(boolean skipData, String classRule, String methodRule, String fieldRule, String idResRule, String layoutResRule, String excludePath) {
        this.skipData.setSelected(skipData);
        this.classRule.setText(classRule);
        this.methodRule.setText(methodRule);
        this.fieldRule.setText(fieldRule);
        this.idResRule.setText(idResRule);
        this.layoutResRule.setText(layoutResRule);
        this.excludePath.setText(excludePath);
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

    public String getIdResRule() {
        return idResRule.getText();
    }

    public void setIdResRule(String idResRule) {
        this.idResRule.setText(idResRule);
    }

    public String getLayoutResRule() {
        return layoutResRule.getText();
    }

    public void setLayoutResRule(String layoutResRule) {
        this.layoutResRule.setText(layoutResRule);
    }

    public boolean getSkipData() {
        return skipData.isSelected();
    }

    public void setSkipData(boolean skipData) {
        this.skipData.setSelected(skipData);
    }

    public String getExcludePath() {
        return excludePath.getText();
    }

    public void setExcludePath(String excludePath) {
        this.excludePath.setText(excludePath);
    }
}
