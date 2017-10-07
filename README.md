# AdventKT

A fully-functional text adventure engine/DSL, with a partial replica of the
classic Colossal Cave as an example.

## Background

My main goal was playing with Kotlin to get a feel for what it was like. A text
adventure model and DSL were a fun little exercise to do, as well as replicating
some of the classic locations, items, and events. However, the game was more of
a use case and a test case than a goal in itself. This said, it turned out to be
a fairly complete mini-adventure with its own goal and plot.

Locations and item descriptions are generally true to the original, as published
by Eric Raymond (see a link below). Some interactions are changed or
augmented to try out various engine features, and to keep the world cohesive
without replicating it all.

## Building and running

All building is currently via the IDE. This repository includes an IDEA project,
with an artifact that builds a `.jar` file. To run the game with command line
editing, build the artifact and in a terminal window run the `advent` script
found in the project root.

## Study pointers for the curious

The game is defined entirely in the `cave.ColossalCave` class, with global
actions in `cave.ColossalCaveActions.kt`. (Warning: the former is one huge
spoiler if you intend to actually play it). The class is Kotlin code, but
shaped into a DSL. The definition is extensively commented to explain the
use of DSL constructs.

The implementation revolves around four core classes under `framework`: `World`,
`Room`, `Item`, and `Action`. The DSL expressions in `cave.ColossalCave`
ultimately create a particular structure of those classes and their subclasses.

The world definition DSL as seen in the `ColossalCave` class is defined by a set
of methods in `World` and `Room`, and to a lesser degree, the `Item` class.

## Links of interest

[Open Adventure](https://gitlab.com/esr/open-adventure), the original Colossal Cave version 2.5  

[Photos of the real world cave](http://www.digitalhumanities.org/dhq/vol/001/2/000009/000009.html)

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

Repository icon by [Freepik](www.freepik.com) from www.flaticon.com
