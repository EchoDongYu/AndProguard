package com.murphy.core

import com.murphy.config.AndConfigState
import com.murphy.util.KOTLIN_SUFFIX

enum class RefactorType {
    PsiDirectory, PsiBinaryFile,
    PsiClass, PsiMethod, PsiParameter, PsiField, PsiVariable, PsiEnumConstant,
    KtFile, KtObject, KtClass, KtFunction, KtProperty, KtParameter;

    fun randomName(config: AndConfigState) = when (this) {
        PsiClass, PsiEnumConstant, KtObject, KtClass -> config.randomClassName
        PsiMethod, KtFunction -> config.randomFunctionName
        PsiParameter, PsiVariable, PsiField,
        KtParameter, KtProperty -> config.randomPropertyName

        KtFile -> config.randomClassName + KOTLIN_SUFFIX
        PsiBinaryFile -> config.randomResFileName
        PsiDirectory -> config.randomDirectoryName
    }
}