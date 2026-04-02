package io.github.badgersmc.advancements.domain

enum class RequirementType {
    BLOCK_BREAK,
    BLOCK_PLACE,
    ENTITY_KILL,
    CRAFT_ITEM,
    PLAYER_KILL,
    PLAYER_JOIN,
    GUILD_CREATE,
    GUILD_MEMBER_JOIN
}

data class Requirement(
    val type: RequirementType,
    val target: String? = null,
    val amount: Int = 1
)
