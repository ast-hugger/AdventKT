/*
 * Copyright (c) 2017 Vassili Bykov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.vassilibykov.adventkt.cave

import org.jline.reader.EndOfFileException
import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder
import org.jline.terminal.TerminalBuilder


/**
 *
 * @author Vassili Bykov
 */

var cave = ColossalCave.create()
    private set
var player = cave.player
    private set
var lantern = cave.lantern
    private set

fun main(args: Array<String>) {
    if ("--edit" in args) {
        val terminal = TerminalBuilder.terminal()
        val lineReader = LineReaderBuilder.builder()
                .terminal(terminal)
                .build()
        cave.play({ readJLine(lineReader) })
    } else {
        cave.play()
    }
}

private fun readJLine(lineReader: LineReader): String {
    return try {
        lineReader.readLine("> ") ?: "quit"
    } catch (e: EndOfFileException) {
        "quit"
    }
}

// for tests only
fun reset() {
    cave = ColossalCave.create()
    player = cave.player
    lantern = cave.lantern
}
