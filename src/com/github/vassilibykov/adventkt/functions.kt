package com.github.vassilibykov.adventkt

import java.util.*

fun say(message: String) = println(message.trimMargin())

fun <T> random(vararg args: T): T {
    if (args.isEmpty()) {
        throw IllegalArgumentException()
    }
    return args[Random().nextInt(args.size)]
}

fun emptyVocabulary() = mutableMapOf<String, Action>()