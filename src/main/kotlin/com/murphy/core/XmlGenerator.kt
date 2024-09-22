package com.murphy.core

import com.android.SdkConstants.*
import com.android.resources.ResourceType
import com.android.tools.idea.res.psi.ResourceReferencePsiElement
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.murphy.config.AndProguardConfigState
import java.util.*

private const val TAG_INTEGER = "integer"

fun Sequence<PsiNamedElement>.processXml(indicator: ProgressIndicator) {
    indicator.fraction = 0.0
    indicator.text = "Refactor xml..."
    var count = 0
    var total = 0
    fun ProgressIndicator.increase(label: String) {
        fraction = ++count / total.toDouble()
        text = "Xml $count of $total [$label]"
    }

    fun <T> Sequence<T>.alsoReset() = also {
        it.count().takeIf { it > 0 }?.let {
            count = 0
            total = it
        }
    }

    val config = AndProguardConfigState.getInstance()
    if (config.idResRule.isNotEmpty()) {
        val resIdList: MutableList<String> = LinkedList()
        filterIsInstance<XmlTag>().alsoReset().forEach {
            when (it.name) {
                TAG_STRING, TAG_STRING_ARRAY, TAG_INTEGER, TAG_INTEGER_ARRAY, TAG_STYLE, TAG_COLOR, TAG_DIMEN -> {
                    it.getAttribute(ATTR_NAME)?.valueElement?.rename(config.randomIdResName, "Resource")
                }

                TAG_ITEM -> {
                    val name = it.parentTag?.name
                    if (name != TAG_STYLE)
                        it.getAttribute(ATTR_NAME)?.valueElement?.rename(config.randomIdResName, "Resource")
                }

                else -> {
                    val attr = it.getAttribute(ANDROID_NS_NAME_PREFIX + ATTR_ID) ?: return@forEach
                    if (resIdList.contains(attr.value)) return@forEach
                    val newName = config.randomIdResName
                    resIdList.add(NEW_ID_PREFIX + newName)
                    attr.valueElement?.rename(newName, "ResId")
                }
            }
            indicator.increase("Resource")
        }
    }
    if (config.layoutResRule.isNotEmpty()) {
        filterIsInstance<XmlFile>().filter { it.checkRename() }.alsoReset().forEach {
            it.rename(config.randomLayoutResName, "ResFile")
            indicator.increase("File")
        }
    }
}

private fun XmlFile.checkRename(): Boolean {
    val resPsi = ResourceReferencePsiElement.create(this) ?: return false
    val type = resPsi.resourceReference.resourceType
    return type != ResourceType.INTEGER && type != ResourceType.STYLE && type != ResourceType.COLOR
            && type != ResourceType.STRING && type != ResourceType.DIMEN && type != ResourceType.ATTR
}