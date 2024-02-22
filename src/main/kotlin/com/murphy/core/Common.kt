package com.murphy.core

import com.intellij.psi.*
import com.intellij.psi.impl.getFieldOfGetter
import com.intellij.psi.impl.getFieldOfSetter
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.refactoring.RefactoringFactory
import com.murphy.config.AndGuardCoinfigState
import org.jetbrains.kotlin.asJava.namedUnwrappedElement
import org.jetbrains.kotlin.idea.search.isImportUsage
import org.jetbrains.kotlin.psi.KtCallableDeclaration
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import kotlin.time.Duration.Companion.milliseconds

fun PsiDirectory.packageName() {
    val srouce = virtualFile.path.replace('/', '.')
}

fun PsiDirectory.fileList(): MutableList<PsiFile> {
    val list: MutableList<PsiFile> = ArrayList()
    subdirectories.forEach { list.addAll(it.fileList()) }
    list.addAll(files)
    return list
}

fun PsiElement.childrenDfsSequence(): Sequence<PsiElement> =
    sequence {
        suspend fun SequenceScope<PsiElement>.visit(element: PsiElement) {
            element.children.forEach { visit(it) }
            yield(element)
        }
        visit(this@childrenDfsSequence)
    }

fun PsiNamedElement.rename(newName: String, desc: String) {
    println(String.format("[$desc] %s >>> %s", name, newName))
    val project = project
    RefactoringFactory.getInstance(project)
        .createRename(this, newName, GlobalSearchScope.projectScope(project), false, false)
        .run()
}

fun XmlAttributeValue.rename(newName: String, desc: String) {
    println(String.format("[$desc] %s >>> %s", value, newName))
    val project = project
    RefactoringFactory.getInstance(project)
        .createRename(this, newName, GlobalSearchScope.projectScope(project), false, false)
        .run()
}

fun PsiMethod.isSetter() = name.startsWith("set")
fun PsiMethod.isGetterOrSetter() = name.run { startsWith("set") || startsWith("get") || startsWith("is") }
fun PsiMethod.getFieldOfGetterOrSetter() = if (isSetter()) getFieldOfSetter(this) else getFieldOfGetter(this)

fun PsiNamedElement.renameReference() {
    ReferencesSearch.search(this, GlobalSearchScope.projectScope(project)).findAll()
        .mapNotNull { it.getNamedElement(name) }
        .distinct().partition { it is PsiField }
        .run {
            val config = AndGuardCoinfigState.getInstance()
            second.forEach { it.rename(config.randomFieldName, "Reference") }
            first.forEach { it.rename(config.randomFieldName, "Reference") }
        }
}

private fun PsiReference.getNamedElement(other: String?): PsiNamedElement? {
    if (isImportUsage() || other == null) return null
    return element.let {
        if (it is PsiJavaCodeReferenceElement || it is KtNameReferenceExpression) {
            it.namedUnwrappedElement?.run {
                if (this is PsiVariable || this is KtCallableDeclaration) {
                    if (name?.contains(other, true) == true) this
                    else null
                } else null
            }
        } else null
    }
}

fun computeTime(startTime: Long): String {
    val time = System.currentTimeMillis() - startTime
    val strBuilder = StringBuilder()
    time.milliseconds.toComponents { days, hours, minutes, seconds, _ ->
        if (days > 0) strBuilder.append(days).append(" d ")
        if (hours > 0) strBuilder.append(hours).append(" h ")
        if (minutes > 0) strBuilder.append(minutes).append(" m ")
        if (seconds >= 0) strBuilder.append(seconds).append(" s ")
    }
    return strBuilder.toString()
}