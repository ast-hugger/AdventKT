package com.github.vassilibykov.adventkt

/**
 * Most of the game objects are defined in ColossalCave, but the lantern
 * is special because it determines the player's ability to see in dark
 * rooms, so it's in fact part of the player.
 *
 * The lantern is a good example of using a Toggle to track its lit/unlit
 * state, producing appropriate messages as the state changes. It is also
 * an example of an item with dynamic descriptions dependent on the state.
 *
 * A lantern defines a local verb which turns it on or off it the player
 * is holding the lantern, in which case the verb is included in the verb
 * lookup sequence. The verb is selected for execution when the command
 * subjects (words following the first) include one of the item's names.
 * The verb also checks the subjects to see whether to turn the lamp on
 * or off.
 */
class Lantern : Item("lamp", "lantern") {
    var light = Toggle(false,
            turnOnMessage = "Your lamp is now turned on.",
            turnOffMessage = "Your lamp is now turned off.",
            alreadyOnMessage = "Your lamp is already turned on.",
            alreadyOffMessage = "Your lamp is already turned off.")

    override fun description() = if (light.isOn) "There is a brass lamp nearby." else "There is a brass lamp shining nearby."

    override fun inventoryDescription() = if (light.isOn) "A lit brass lantern" else "A brass lantern"

    init {
        verb("turn", "switch") {
            when {
                "on" in subjects -> {
                    val wasOn = light.isOn
                    light.turnOn()
                    if (!wasOn) {
                        player.room.printFullDescription()
                    }
                }
                "off" in subjects -> {
                    light.turnOff()
                    if (player.room is DarkRoom && !light.isOn) {
                        say("It is pitch black. You are likely to be eaten by... umm, nevermind, wrong game.")
                    }
                }
                else -> say("Do you want the lamp on or off?")
            }
        }
    }
}