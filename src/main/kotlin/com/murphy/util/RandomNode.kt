package com.murphy.util

import java.util.*
import kotlin.random.Random

/**
 * @property element String or Node or List
 */
class RandomNode(
    private val range: Pair<Int, Int>,
    private val element: Any
) {
    private val length: Int
        get() = if (range.first == range.second) range.first
        else if (range.first < range.second) Random.nextInt(range.first, range.second + 1)
        else throw IllegalArgumentException("Wrong length: (${range.first}, ${range.second})")

    private val String.randomChar: Char get() = this[Random.nextInt(0, length)]

    val randomString: String
        get() = when (element) {
            is String -> element
            is StringBuilder -> {
                val content = element.toString()
                String(CharArray(length) { content.randomChar })
            }

            is RandomNode -> {
                val builder = StringBuilder()
                repeat(length) { builder.append(element.randomString) }
                builder.toString()
            }

            is List<*> -> {
                val builder = StringBuilder()
                repeat(length) {
                    element.forEach {
                        builder.append((it as RandomNode).randomString)
                    }
                }
                builder.toString()
            }

            else -> throw IllegalArgumentException("Wrong type: ${element::class.simpleName}")
        }

    companion object {

        fun String.parseNode(): RandomNode {
            val element = parse(this)
            return if (element is RandomNode) element
            else RandomNode(Pair(1, 1), element)
        }

        private fun parse(content: String): Any {
            val stack = Stack<String>()
            var pos = content.length - 1
            while (pos > 0) {
                val start = when (content[pos]) {
                    ')' -> content.indexOfLast(pos, '(')
                    ']' -> content.indexOfLast(pos, '[')
                    '}' -> content.indexOf('{')
                    '>' -> {
                        stack.push("(1)")
                        content.indexOfLast(pos, '<')
                    }

                    else -> throw IllegalArgumentException("Wrong parse: ${content[pos]}")
                }
                assert(start > 0)
                stack.push(content.substring(start, pos + 1))
                pos = start - 1
            }
            return if (stack.size == 1) stack.pop().toElement()
            else if (stack.size == 2) {
                val first = stack.pop()
                val second = stack.pop()
                RandomNode(second.toRange(), first.toElement())
            } else if (stack.size % 2 == 0) {
                val list: MutableList<Pair<String, String>> = ArrayList()
                while (!stack.empty()) {
                    list.add(Pair(stack.pop(), stack.pop()))
                }
                list.map { RandomNode(it.second.toRange(), it.first.toElement()) }
            } else throw IllegalArgumentException("Wrong parse: $stack")
        }

        private fun String.toRange(): Pair<Int, Int> {
            if (!startsWith('(') || !endsWith(')'))
                throw IllegalArgumentException("Wrong convert range: $this")
            val split = substring(1, length - 1).split(',')
            val start = split[0].toInt()
            val end = if (split.size == 1) start else split[1].toInt()
            return Pair(start, end)
        }

        private fun String.toElement(): Any {
            val specCode = substring(1, length - 1)
            if (startsWith('<') && endsWith('>')) return specCode
            if (startsWith('{') && endsWith('}')) return parse(specCode)
            if (startsWith('[') && endsWith(']')) {
                val specPos = specCode.indexOf("1")
                if (specCode.indexOfFirst { it != '1' && it != '0' } > 0 || specCode.length < 4 || specPos < 0 || specPos > 3)
                    throw IllegalArgumentException("Wrong convert element[]: $this")
                val builder = StringBuilder()
                if (specCode[0] == '1') builder.append(CHAR_UPPER)
                if (specCode[1] == '1') builder.append(CHAR_LOWER)
                if (specCode[2] == '1') builder.append(CHAR_DIGIT)
                if (specCode[3] == '1') builder.append(CHAR_UNDERLINE)
                return builder
            }
            throw IllegalArgumentException("Wrong convert element: $this")
        }

        private fun CharSequence.indexOfLast(pos: Int, char: Char): Int {
            for (index in indices.reversed()) {
                if (this[index] == char && index < pos) {
                    return index
                }
            }
            return -1
        }
    }
}