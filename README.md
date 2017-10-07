# AdventKT

A Kotlin-based DSL for text adventures, with a partial replica of the classic
Colossal Cave as an example.

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

Run `gradle jar` in the project directory to build a jar under `build/libs/AdventKT.jar`.

Alternatively, open the project directory in IntelliJ (it is a project) and
build the `AdventKT:jar` artifact. This produces
`out/artifacts/AdventKT_jar/AdventKT.jar`.

Run the jar using the `advent` shell script in the project directory. It runs
the jar from the location where the IDE builds it. If using Gradle, edit the
path in the script accordingly.

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
