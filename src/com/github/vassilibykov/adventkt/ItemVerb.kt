package com.github.vassilibykov.adventkt

class ItemVerb(val item: Item, words: Collection<String>, action: LocalVerb.()->Unit)
    : LocalVerb(words, action)
{
    override fun act(subjects: List<String>) {
        if (!subjects.any { it in item.names }) {
            pass()
        } else {
            super.act(subjects)
        }
    }

}