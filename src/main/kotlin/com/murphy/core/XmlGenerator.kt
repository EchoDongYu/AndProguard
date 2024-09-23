package com.murphy.core

import com.android.SdkConstants.*
import com.android.resources.ResourceType
import com.android.tools.idea.res.psi.ResourceReferencePsiElement
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import java.util.*

object XmlGenerator : AbstractGenerator() {
    override val name: String get() = "Xml"

    override fun process(list: List<PsiNamedElement>, indicator: ProgressIndicator) {
        indicator.fraction = 0.0
        indicator.text = "Refactor $name..."
        if (config.resourceRule.isNotEmpty()) {
            val resIdList: MutableList<String> = LinkedList()
            list.filterIsInstance<XmlTag>().alsoReset().forEach {
                when (it.name) {
                    TAG_STRING, TAG_STRING_ARRAY, TAG_INTEGER, TAG_INTEGER_ARRAY, TAG_STYLE, TAG_COLOR, TAG_DIMEN -> {
                        runReadAction { it.getAttribute(ATTR_NAME) }
                            ?.valueElement
                            ?.renameX(config.randomResourceName, "Resource")
                    }

                    TAG_ITEM -> {
                        val name = it.parentTag?.name
                        if (name != TAG_STYLE) {
                            runReadAction { it.getAttribute(ATTR_NAME) }
                                ?.valueElement
                                ?.renameX(config.randomResourceName, "Resource")
                        }
                    }

                    else -> {
                        val attr = runReadAction { it.getAttribute(ANDROID_NS_NAME_PREFIX + ATTR_ID) } ?: return@forEach
                        if (resIdList.contains(runReadAction { attr.value })) return@forEach
                        val newName = config.randomResourceName
                        resIdList.add(NEW_ID_PREFIX + newName)
                        resIdList.add(ID_PREFIX + newName)
                        attr.valueElement?.renameX(newName, "ResourceId")
                    }
                }
                indicator.increase("Resource")
            }
            resIdList.clear()
        }
        if (config.fileResRule.isNotEmpty()) {
            list.filterIsInstance<XmlFile>().filter { it.checkRename() }.alsoReset().forEach {
                it.rename(config.randomFileResName, "File")
                indicator.increase("File")
            }
        }
    }

    private const val TAG_INTEGER = "integer"

    private fun XmlFile.checkRename(): Boolean {
        val resPsi = runReadAction { ResourceReferencePsiElement.create(this) } ?: return false
        val type = resPsi.resourceReference.resourceType
        return type != ResourceType.INTEGER && type != ResourceType.STYLE && type != ResourceType.COLOR
                && type != ResourceType.STRING && type != ResourceType.DIMEN && type != ResourceType.ATTR
    }
}
