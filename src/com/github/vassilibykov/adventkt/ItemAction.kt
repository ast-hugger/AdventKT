package com.github.vassilibykov.adventkt

class ItemAction(val item: Item, words: Collection<String>, action: LocalAction.()->Unit)
    : LocalAction(words, action)
{
    override fun act(subjects: List<String>) {
        if (!subjects.any { it in item.names }) {
            pass()
        } else {
            super.act(subjects)
        }
    }

}