# Background

This is a fully-functional text adventure engine with an embedded DSL,
with a partial replica of the classic Colossal Cave as an example.

The replica is partial because my goal was playing with Kotlin to get a feel for
what it's like, and a text adventure model/DSL seemed like a fun little exercise
to do. Colossal Cave is an instantly recognizable classic, and replicating some
of its locations, items, and events was fun. Still, the actual game was a use
case for the engine and not a goal in itself. This said, it turned out to be a
fairly complete mini-adventure with its own goal and plot.

Location and item descriptions are generally true to the original, however some
interactions are changed or augmented to try out various engine features, and to
make the world cohesive without replicating it all.

# Building and Running

This repository includes an IDEA project, with an artifact building a `.jar`
file. The best way to run the game is by building the artifact and then
using the shell script `advent` in the project root to run it.

# Study Pointers

The game is defined entirely in the `cave.ColossalCave` class, with global
actions in `cave.ColossalCaveActions.kt`. Warning: the former is one huge
spoiler if you intend to actually play it. The class is Kotlin code, but
shaped into a DSL. The definition language is hopefully readable and
self-explanatory. The framework methods it uses are documented--so where it
isn't, Ctrl-Q in IDEA might clarify things.

The implementation revolves around four core classes under `framework`: `World`,
`Room`, `Item`, and `Action`. The DSL expressions in `cave.ColossalCave`
ultimately create a particular structure of those and their subclasses.

# License

    Copyright (c) 2017 Vassili Bykov

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
       http://www.apache.org/licenses/LICENSE-2.0
     
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
