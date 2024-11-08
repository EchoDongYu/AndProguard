package com.murphy.core

import com.android.SdkConstants.*
import com.android.resources.ResourceType
import com.android.tools.idea.res.psi.ResourceReferencePsiElement
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import java.util.*

object XmlGenerator : AbstractGenerator() {
    override val name: String get() = "Xml"

    override fun process(project: Project, list: List<PsiNamedElement>, indicator: ProgressIndicator) {
        indicator.fraction = 0.001
        indicator.text = "Refactor $name..."
        val dService = DumbService.getInstance(project)
        if (config.resourceRule.isNotEmpty()) {
            val resIdList: MutableList<String> = LinkedList()
            list.filterIsInstance<XmlTag>().alsoReset().forEach {
                when (it.name) {
                    TAG_STRING, TAG_STRING_ARRAY, TAG_INTEGER, TAG_INTEGER_ARRAY, TAG_STYLE, TAG_COLOR, TAG_DIMEN -> {
                        dService.dumbReadAction { it.getAttribute(ATTR_NAME) }
                            ?.valueElement
                            ?.renameX(config.randomResourceName, "Resource", indicator.increase)
                    }

                    TAG_ITEM -> {
                        val name = it.parentTag?.name
                        if (name != TAG_STYLE) {
                            dService.dumbReadAction { it.getAttribute(ATTR_NAME) }
                                ?.valueElement
                                ?.renameX(config.randomResourceName, "Resource", indicator.increase)
                        }
                    }

                    else -> {
                        val attr = dService.dumbReadAction { it.getAttribute(ANDROID_NS_NAME_PREFIX + ATTR_ID) }
                            ?: return@forEach
                        if (resIdList.contains(dService.dumbReadAction { attr.value })) return@forEach
                        val newName = config.randomResourceName
                        resIdList.add(NEW_ID_PREFIX + newName)
                        resIdList.add(ID_PREFIX + newName)
                        attr.valueElement?.renameX(newName, "ResourceId", indicator.increase)
                    }
                }
            }
            resIdList.clear()
        }
        if (config.fileResRule.isNotEmpty()) {
            list.filterIsInstance<XmlFile>().filter { it.checkRename() }.alsoReset().forEach {
                it.rename(config.randomFileResName, "File", indicator.increase)
            }
        }
    }

    private const val TAG_INTEGER = "integer"

    private fun XmlFile.checkRename(): Boolean {
        val dService = DumbService.getInstance(project)
        val resPsi = dService.dumbReadAction { ResourceReferencePsiElement.create(this) } ?: return false
        val type = resPsi.resourceReference.resourceType
        return type != ResourceType.INTEGER && type != ResourceType.STYLE && type != ResourceType.COLOR
                && type != ResourceType.STRING && type != ResourceType.DIMEN && type != ResourceType.ATTR
    }
}
