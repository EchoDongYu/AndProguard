package com.murphy.core

import com.murphy.config.AndConfigState
import com.murphy.util.KOTLIN_SUFFIX

enum class RefactorType {
    PsiDirectory, PsiResourceFile,
    PsiClass, PsiMethod, PsiParameter, PsiField, PsiVariable, PsiEnumConstant,
    KtFile, KtObject, KtClass, KtFunction, KtVariable, KtParameter;

    fun randomName(config: AndConfigState) = when (this) {
        PsiClass, PsiEnumConstant, KtObject, KtClass -> config.randomClassName
        PsiMethod, KtFunction -> config.randomFunctionName
        PsiParameter, PsiVariable, PsiField,
        KtParameter, KtVariable -> config.randomPropertyName

        KtFile -> config.randomClassName + KOTLIN_SUFFIX
        PsiResourceFile -> config.randomResFileName
        PsiDirectory -> config.randomDirectoryName
    }
}