package com.dasong.commerce.util

import kotlin.random.Random

fun <T> MutableList<T>.shuffle() {
    val list = this
    for (i in list.size - 1 downTo 1) {
        val j = Random.nextInt(i + 1)
        val temp = list[i]
        list[i] = list[j]
        list[j] = temp
    }
}

fun <T> List<T>.shuffled(): List<T> {
    val mutable = this.toMutableList()
    mutable.shuffle()
    return mutable
}

fun <T> MutableList<T>.drawTop(): T = removeAt(lastIndex)
fun <T> MutableList<T>.drawBottom(): T = removeAt(0)
