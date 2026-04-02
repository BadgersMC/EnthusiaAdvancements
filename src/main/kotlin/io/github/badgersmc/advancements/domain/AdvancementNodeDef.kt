package io.github.badgersmc.advancements.domain

enum class FrameType {
    TASK,
    GOAL,
    CHALLENGE
}

data class AdvancementNodeDef(
    val key: String,
    val title: String,
    val description: List<String>,
    val icon: String,
    val frame: FrameType,
    val x: Float,
    val y: Float,
    val maxProgression: Int,
    val parentKey: String? = null,
    val requirement: Requirement? = null,
    val rewards: List<Reward> = emptyList(),
    val showToast: Boolean = true,
    val announceChat: Boolean = true
)
