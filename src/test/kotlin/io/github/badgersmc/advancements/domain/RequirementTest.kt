package io.github.badgersmc.advancements.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RequirementTest {

    @Test
    fun `all requirement types exist`() {
        val expected = setOf(
            "BLOCK_BREAK", "BLOCK_PLACE", "ENTITY_KILL", "CRAFT_ITEM",
            "PLAYER_KILL", "PLAYER_JOIN",
            "GUILD_CREATE", "GUILD_MEMBER_JOIN",
            "GUILD_BANNER_SET", "GUILD_HOME_SET", "GUILD_VAULT_PLACED",
            "GUILD_LEVEL_UP", "GUILD_WAR_DECLARED", "GUILD_WAR_WON",
            "GUILD_MEMBER_REMOVED", "GUILD_OWNERSHIP_TRANSFER", "GUILD_FRIENDLY_FIRE",
            "GUILD_COLLECTIVE_SMELT", "GUILD_COLLECTIVE_BLOCK_BREAK",
            "GUILD_COLLECTIVE_CROP_HARVEST", "GUILD_COLLECTIVE_MOB_KILL",
            "GUILD_COLLECTIVE_BOSS_KILL"
        )
        val actual = RequirementType.entries.map { it.name }.toSet()
        assertEquals(expected, actual)
    }

    @Test
    fun `requirement type count is 22`() {
        assertEquals(22, RequirementType.entries.size)
    }

    @Test
    fun `requirement with null target is valid`() {
        val req = Requirement(RequirementType.PLAYER_JOIN)
        assertNull(req.target)
        assertEquals(1, req.amount)
    }

    @Test
    fun `requirement default amount is 1`() {
        val req = Requirement(RequirementType.ENTITY_KILL, "ZOMBIE")
        assertEquals(1, req.amount)
    }

    @Test
    fun `requirement stores all fields`() {
        val req = Requirement(RequirementType.BLOCK_BREAK, "STONE", 50)
        assertEquals(RequirementType.BLOCK_BREAK, req.type)
        assertEquals("STONE", req.target)
        assertEquals(50, req.amount)
    }

    @Test
    fun `valueOf resolves enum values`() {
        assertEquals(RequirementType.ENTITY_KILL, RequirementType.valueOf("ENTITY_KILL"))
        assertEquals(RequirementType.GUILD_CREATE, RequirementType.valueOf("GUILD_CREATE"))
    }
}
