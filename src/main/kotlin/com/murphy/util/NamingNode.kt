package com.murphy.util

import java.util.Stack
import kotlin.random.Random

/**
 * 命名树叶节点
 *
 * @property range 限定范围
 * @property element String or NamingRule or RandomNode or List
 */
class NamingNode(
    private val range: Pair<Int, Int>,
    private val element: Any
) {
    private val randomLength: Int
        get() = if (range.first == range.second) range.first
        else if (range.first < range.second) Random.nextInt(range.first, range.second + 1)
        else throw IllegalArgumentException("Wrong length: (${range.first}, ${range.second})")

    private fun buildRandomString(action: StringBuilder.() -> Unit): String {
        return buildString { repeat(randomLength) { action() } }
    }

    val randomNaming: String
        get() = when (element) {
            is String -> buildRandomString {
                append(element)
            }

            is NamingRule -> element.generateRandomName(randomLength)

            is NamingNode -> buildRandomString {
                append(element.randomNaming)
            }

            is List<*> -> buildRandomString {
                element.forEach {
                    append((it as NamingNode).randomNaming)
                }
            }

            else -> throw IllegalArgumentException("Wrong type: ${element::class.simpleName}")
        }
}

fun String.parseNode(): NamingNode {
    val element = parseNodeAny(this)
    return if (element is NamingNode) element
    else NamingNode(Pair(1, 1), element)
}

private fun parseNodeAny(content: String): Any {
    val stack = Stack<String>()
    var index = content.length - 1
    var fillRange = true
    while (index > 0) {
        val char = content[index]
        val (startIndex, isRange) = when (char) {
            '}' -> Pair(content.indexOfLast(index, '{'), true)
            ']' -> Pair(content.indexOfLast(index, '['), false)
            ')' -> Pair(content.indexOf('('), false)

            else -> throw IllegalArgumentException("Wrong parse: $content index=$index char=$char")
        }
        assert(startIndex >= 0) { "Wrong assert: $content startIndex=$startIndex" }
        if (fillRange && !isRange) stack.push("{1}")
        fillRange = !isRange
        stack.push(content.substring(startIndex, index + 1))
        index = startIndex - 1
    }
    return if (stack.size == 2) {
        val first = stack.pop()
        val second = stack.pop()
        NamingNode(second.toRange(), first.toElement())
    } else if (stack.size % 2 == 0) {
        val list: MutableList<Pair<String, String>> = ArrayList()
        while (!stack.empty()) list.add(Pair(stack.pop(), stack.pop()))
        list.map { NamingNode(it.second.toRange(), it.first.toElement()) }
    } else throw IllegalArgumentException("Wrong parse: $content stack=$stack")
}

private fun String.toRange(): Pair<Int, Int> {
    if (!startsWith('{') || !endsWith('}'))
        throw IllegalArgumentException("Wrong parse range: $this")
    val split = substring(1, length - 1).split(',')
    val start = split[0].toInt()
    val end = if (split.size == 1) start else split[1].toInt()
    return Pair(start, end)
}

private fun String.toElement(): Any {
    val content = substring(1, length - 1)
    if (startsWith('(') && endsWith(')')) return parseNodeAny(content)
    if (startsWith('[') && endsWith(']')) {
        return if (naming_flags.all { !content.startsWith(it) }) content
        else parseRuleOrThrow(content)
    }
    throw IllegalArgumentException("Wrong parse element: $this")
}

private fun CharSequence.indexOfLast(pos: Int, char: Char): Int {
    for (index in indices.reversed()) {
        if (this[index] == char && index < pos) {
            return index
        }
    }
    return -1
}