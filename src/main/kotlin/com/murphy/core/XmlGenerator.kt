package com.murphy.core

import com.android.SdkConstants.*
import com.android.resources.ResourceType
import com.android.tools.idea.res.psi.ResourceReferencePsiElement
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.murphy.config.AndProguardCoinfigState
import java.util.*

const val TAG_INTEGER = "integer"

fun processXml(psi: XmlFile, resIdList: MutableList<String> = LinkedList()) {
    println("============================== ${psi.name} ==============================")
    val config = AndProguardCoinfigState.getInstance()
    val tagSeq = psi.childrenDfsSequence().filterIsInstance<XmlTag>()
    tagSeq.forEach { tag ->
        when (tag.name) {
            TAG_STRING, TAG_STRING_ARRAY, TAG_INTEGER, TAG_INTEGER_ARRAY, TAG_STYLE, TAG_COLOR, TAG_DIMEN -> {
                tag.getAttribute(ATTR_NAME)?.valueElement?.rename(config.randomIdResName, "Resource")
            }

            TAG_ITEM -> {
                val name = tag.parentTag?.name
                if (name != TAG_STYLE)
                    tag.getAttribute(ATTR_NAME)?.valueElement?.rename(config.randomIdResName, "Resource")
            }

            else -> {
                val attr = tag.getAttribute(ANDROID_NS_NAME_PREFIX + ATTR_ID) ?: return@forEach
                if (resIdList.contains(attr.value)) return@forEach
                val newName = config.randomIdResName
                resIdList.add(NEW_ID_PREFIX + newName)
                attr.valueElement?.rename(newName, "ResId")
            }
        }
    }
    if (psi.checkRename()) psi.rename(config.randomLayoutResName, "ResFile")
}

private fun XmlFile.checkRename(): Boolean {
    val resPsi = ResourceReferencePsiElement.create(this) ?: return false
    val type = resPsi.resourceReference.resourceType
    return type != ResourceType.INTEGER && type != ResourceType.STYLE && type != ResourceType.COLOR
            && type != ResourceType.STRING && type != ResourceType.DIMEN && type != ResourceType.ATTR
}