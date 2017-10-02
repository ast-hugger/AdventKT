package com.github.vassilibykov.adventkt

/**
 * A simple hidden [Fixture] with no special behavior other than providing a
 * description when it's looked at.
 */
class Detail(vararg names: String,
             override val description: String,
             private val verbs: Collection<String> = setOf("look", "examine", "l", "x"))
    : Fixture(*names)
{
    override val isHidden = true

    override fun configure() {
        super.configure()
        vicinityAction(*verbs.toTypedArray()) {
            say(description)
        }
    }
}