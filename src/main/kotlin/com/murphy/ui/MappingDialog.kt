package com.murphy.ui

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import com.intellij.json.JsonFileType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import java.awt.Dimension
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import javax.swing.JComponent
import javax.swing.JMenuItem
import javax.swing.JPopupMenu

/**
 * Json input Dialog
 */
private val jsonInputValidator: JsonInputValidator = JsonInputValidator()

class MappingDialog(project: Project?) : Messages.InputDialog(
    project,
    "Please input the JSON String to mapping Kotlin data or java bean",
    "JSON Mapping",
    null,
    "",
    jsonInputValidator
) {
    private lateinit var jsonContentEditor: Editor
    private val prettyGson: Gson = GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create()

    init {
        setOKButtonText("Mapping")
    }

    override fun createNorthPanel(): JComponent {
        return jHorizontalLinearLayout {
            fixedSpace(5)
            jVerticalLinearLayout {
                alignLeftComponent {
                    myMessage?.let { jLabel(it, 12f) }
                }
                jHorizontalLinearLayout {
                    jLabel("JSON Text: ", 14f)
                    jLabel("Tips: you can use JSON string or local file just right click on text area", 12f)
                    fillSpace()
                    jButton("Format", { handleFormatJSONString() })
                }
            }
        }
    }

    override fun createCenterPanel(): JComponent {
        jsonContentEditor = createJsonContentEditor()
        myField = createTextFieldComponent()
        return jBorderLayout { putCenterFill(jsonContentEditor.component) }
    }

    override fun getInputString(): String = jsonContentEditor.document.text.trim()

    private val checkInput get() = jsonInputValidator.checkInput(inputString)

    override fun getPreferredFocusedComponent(): JComponent = jsonContentEditor.contentComponent

    override fun doOKAction() {
        if (checkInput) {
            val map = prettyGson.fromJson(inputString, object : TypeToken<Map<String, String>>() {})
            listener?.onClick(map)
            close(0)
        }
    }

    private fun handleFormatJSONString() {
        val currentText = jsonContentEditor.document.text
        if (currentText.isNotEmpty()) {
            try {
                val jsonElement = prettyGson.fromJson(currentText, JsonElement::class.java)
                val formatJSON = prettyGson.toJson(jsonElement)
                runWriteAction { jsonContentEditor.document.setText(formatJSON) }
            } catch (_: Exception) {
            }
        }
    }

    private fun createJsonContentEditor(): Editor {
        val editorFactory = EditorFactory.getInstance()
        val document = editorFactory.createDocument("").apply {
            setReadOnly(false)
            addDocumentListener(object : com.intellij.openapi.editor.event.DocumentListener {
                override fun beforeDocumentChange(event: DocumentEvent) {
                    okAction.isEnabled = checkInput
                }

                override fun documentChanged(event: DocumentEvent) = Unit
            })
        }

        val editor = editorFactory.createEditor(document, null, JsonFileType.INSTANCE, false)

        editor.component.apply {
            isEnabled = true
            preferredSize = Dimension(640, 480)
            autoscrolls = true
        }

        val contentComponent = editor.contentComponent
        contentComponent.isFocusable = true
        contentComponent.componentPopupMenu = JPopupMenu().apply {
            add(createPasteFromClipboardMenuItem())
            add(createLoadFromLocalFileMenu())
        }
        return editor
    }

    private fun createPasteFromClipboardMenuItem() = JMenuItem("Paste from clipboard").apply {
        addActionListener {
            val transferable = Toolkit.getDefaultToolkit().systemClipboard.getContents(null)
            if (transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                runWriteAction {
                    jsonContentEditor.document.setText(transferable.getTransferData(DataFlavor.stringFlavor).toString())
                }
            }
        }
    }

    private fun createLoadFromLocalFileMenu() = JMenuItem("Load from local file").apply {
        addActionListener {
            FileChooser.chooseFile(FileChooserDescriptor(true, false, false, false, false, false), null, null) { file ->
                val content = String(file.contentsToByteArray())
                ApplicationManager.getApplication().runWriteAction {
                    jsonContentEditor.document.setText(content.replace("\r\n", "\n"))
                }
            }
        }
    }

    private var listener: OkListener? = null

    fun setOkClickListener(call: (Map<String, String>) -> Unit) {
        listener = object : OkListener {
            override fun onClick(map: Map<String, String>) {
                call.invoke(map)
            }
        }
    }

    private interface OkListener {
        fun onClick(map: Map<String, String>)
    }
}