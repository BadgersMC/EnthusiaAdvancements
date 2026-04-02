package io.github.badgersmc.advancements.domain

sealed class Reward {
    data class Command(val command: String) : Reward()
    data class Experience(val amount: Int) : Reward()
    data class Item(val material: String, val amount: Int = 1) : Reward()
    data class Message(val text: String) : Reward()
}
