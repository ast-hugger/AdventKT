# AdventKT

A fully-functional text adventure engine/DSL, with a partial replica of the
classic Colossal Cave as an example. Pronounced "advent kit".

## Background

My main goal was playing with Kotlin to get a feel for what it was like. A text
adventure model/DSL was a fun little exercise to do. Colossal Cave is an
instantly recognizable classic, and replicating some of its locations, items,
and events was fun. The game itself was more of a use case and a test case than
a goal in itself. This said, it turned out to be a fairly complete
mini-adventure with its own goal and plot.

Locations and item descriptions are generally true to the original, as published
by Eric Raymond (adventure.yaml is included). Some interactions are changed or
augmented to try out various engine features, and to keep the world cohesive
without replicating it all.

## Building and Running

All building is currently via the IDE. This repository includes an IDEA project,
with an artifact that builds a `.jar` file. To run the game with command line
editing, build the artifact and in a terminal window run the `advent` script
found in the project root.

## Study Pointers for the Curious

The game is defined entirely in the `cave.ColossalCave` class, with global
actions in `cave.ColossalCaveActions.kt`. (Warning: the former is one huge
spoiler if you intend to actually play it). The class is Kotlin code, but
shaped into a DSL. The definition language is hopefully readable and
self-explanatory. The framework methods it uses are all documented--so if
anything is unclear, Ctrl-Q in IDEA might help.

The implementation revolves around four core classes under `framework`: `World`,
`Room`, `Item`, and `Action`. The DSL expressions in `cave.ColossalCave`
ultimately create a particular structure of those classes and their subclasses.

`World` contains `Rooms`, and `Rooms` contain `Items` and the `Player`. Some
items and the player can move between rooms. `Actions` are associated with rooms
and items and collaborate with the parser to interpret user input. Once actions
applicable to the input are selected, one or more of them run to manipulate the
game world. Manipulation commonly involves moving the player and the items, and
sometimes changing the values of program variables. When an item or the player
are moved between rooms, there is a protocol by which the item and the rooms
involved are notified about the move. Any of the notified parties can veto the
move, or perform additional game actions.

The world definition DSL as seen in the `ColossalCave` class is defined by a set
of methods in `World` and `Room`, and to a lesser degree, the `Item` class.

[http://www.digitalhumanities.org/dhq/vol/001/2/000009/000009.html]

## License

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
