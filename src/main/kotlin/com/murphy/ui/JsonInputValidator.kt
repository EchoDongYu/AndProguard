package com.murphy.ui

import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.intellij.openapi.ui.InputValidator


class JsonInputValidator : InputValidator {
    override fun checkInput(inputString: String) = try {
        val jsonElement = JsonParser.parseString(inputString)
        jsonElement.isJsonObject || jsonElement.isJsonArray
    } catch (e: JsonSyntaxException) {
        false
    }

    override fun canClose(inputString: String): Boolean = true
}