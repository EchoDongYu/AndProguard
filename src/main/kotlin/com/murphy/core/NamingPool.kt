package com.murphy.core

import com.murphy.config.AndConfigState
import java.util.*

class NamingPool(private val randomName: List<LinkedList<String>>) {
    fun getOrNull(index: Int) = randomName[index].removeFirstOrNull()

    companion object {
        fun createNamingPool(list: List<RenamableElement<*>>): NamingPool {
            val config = AndConfigState.getInstance()
            val randomName = list.map { it.namingIndex }
                .let { position -> List(6) { index -> position.count { it == index } } }
                .mapIndexed { index, count ->
                    generateSequence { config.namingNodes[index].randomNaming }
                        .distinct()
                        .take(count)
                        .toCollection(LinkedList())
                }
            return NamingPool(randomName)
        }
    }
}