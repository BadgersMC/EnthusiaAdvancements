package io.github.badgersmc.advancements.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class TreeDefTest {

    private fun node(key: String, parentKey: String? = null) = AdvancementNodeDef(
        key = key,
        title = key,
        description = listOf("Test node"),
        icon = "STONE",
        frame = FrameType.TASK,
        x = 0f,
        y = 0f,
        maxProgression = 1,
        parentKey = parentKey
    )

    private fun tree(vararg nodes: AdvancementNodeDef) = TreeDef(
        namespace = "test",
        backgroundTexture = "minecraft:textures/block/stone.png",
        nodes = nodes.toList()
    )

    // --- validate() tests ---

    @Test
    fun `valid tree with one root and valid parents returns no errors`() {
        val t = tree(
            node("root"),
            node("child1", "root"),
            node("child2", "root")
        )
        assertEquals(emptyList(), t.validate())
    }

    @Test
    fun `zero roots produces error mentioning 0`() {
        val t = tree(
            node("a", "b"),
            node("b", "a")
        )
        val errors = t.validate()
        assertTrue(errors.isNotEmpty(), "Should have errors")
        assertTrue(errors.any { "0" in it }, "Should mention 0 roots: $errors")
    }

    @Test
    fun `two roots produces error mentioning 2`() {
        val t = tree(
            node("root1"),
            node("root2")
        )
        val errors = t.validate()
        assertTrue(errors.isNotEmpty(), "Should have errors")
        assertTrue(errors.any { "2" in it }, "Should mention 2 roots: $errors")
    }

    @Test
    fun `orphan node with missing parent produces error mentioning the orphan key`() {
        val t = tree(
            node("root"),
            node("child", "nonexistent")
        )
        val errors = t.validate()
        assertTrue(errors.isNotEmpty(), "Should have errors")
        assertTrue(errors.any { "nonexistent" in it }, "Should mention missing parent: $errors")
    }

    @Test
    fun `cycle among children produces error mentioning cycle`() {
        // root -> a, but b -> c -> b forms a cycle (b and c both have parents that exist but form a loop)
        val t = tree(
            node("root"),
            node("a", "root"),
            node("b", "c"),
            node("c", "b")
        )
        val errors = t.validate()
        assertTrue(errors.isNotEmpty(), "Should have errors")
        assertTrue(errors.any { "cycle" in it.lowercase() }, "Should mention cycle: $errors")
    }

    // --- root tests ---

    @Test
    fun `root returns the single node with null parentKey`() {
        val t = tree(
            node("the_root"),
            node("child", "the_root")
        )
        assertEquals("the_root", t.root.key)
    }

    @Test
    fun `root throws on tree with two roots`() {
        val t = tree(
            node("root1"),
            node("root2")
        )
        assertFailsWith<IllegalArgumentException> { t.root }
    }

    // --- sortedNodes() tests ---

    @Test
    fun `sorted nodes has root first`() {
        val t = tree(
            node("child", "root"),
            node("root")
        )
        assertEquals("root", t.sortedNodes().first().key)
    }

    @Test
    fun `every node appears after its parent`() {
        val t = tree(
            node("root"),
            node("a", "root"),
            node("b", "root"),
            node("c", "a"),
            node("d", "c")
        )
        val sorted = t.sortedNodes()
        val indexMap = sorted.mapIndexed { i, n -> n.key to i }.toMap()

        for (node in sorted) {
            if (node.parentKey != null) {
                assertTrue(
                    indexMap[node.parentKey]!! < indexMap[node.key]!!,
                    "Parent '${node.parentKey}' should appear before '${node.key}'"
                )
            }
        }
    }

    @Test
    fun `all nodes present in sorted output`() {
        val t = tree(
            node("root"),
            node("a", "root"),
            node("b", "root"),
            node("c", "a")
        )
        val sorted = t.sortedNodes()
        assertEquals(4, sorted.size)
        assertEquals(setOf("root", "a", "b", "c"), sorted.map { it.key }.toSet())
    }

    @Test
    fun `linear chain produces exact order`() {
        val t = tree(
            node("c", "b"),
            node("b", "a"),
            node("a", "root"),
            node("root")
        )
        val keys = t.sortedNodes().map { it.key }
        assertEquals(listOf("root", "a", "b", "c"), keys)
    }

    @Test
    fun `branching tree has root first and parent a before child c`() {
        val t = tree(
            node("root"),
            node("a", "root"),
            node("b", "root"),
            node("c", "a")
        )
        val sorted = t.sortedNodes()
        val keys = sorted.map { it.key }

        assertEquals("root", keys.first())
        assertTrue(keys.indexOf("a") < keys.indexOf("c"), "a should come before c")
        assertTrue(keys.indexOf("root") < keys.indexOf("b"), "root should come before b")
    }
}
