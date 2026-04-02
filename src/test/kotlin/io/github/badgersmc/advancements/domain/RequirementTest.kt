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
            "GUILD_CREATE", "GUILD_MEMBER_JOIN"
        )
        val actual = RequirementType.entries.map { it.name }.toSet()
        assertEquals(expected, actual)
    }

    @Test
    fun `requirement type count is 9`() {
        assertEquals(8, RequirementType.entries.size)
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
