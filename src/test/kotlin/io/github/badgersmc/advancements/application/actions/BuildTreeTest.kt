package io.github.badgersmc.advancements.application.actions

import io.github.badgersmc.advancements.domain.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * BuildTree.execute() constructs real UAAPI advancement objects which require
 * a running Bukkit server (Material.valueOf, NMS static initializers). These tests
 * verify the prerequisite logic that BuildTree depends on — topological ordering
 * and tree structure — which are the domain-layer guarantees BuildTree relies on.
 *
 * Full integration testing of BuildTree is done in P8-T2 on a live server.
 */
class BuildTreeTest {

    private fun node(key: String, parentKey: String? = null, maxProgression: Int = 1) = AdvancementNodeDef(
        key = key,
        title = key,
        description = listOf("Test"),
        icon = "STONE",
        frame = FrameType.TASK,
        x = 0f,
        y = 0f,
        maxProgression = maxProgression,
        parentKey = parentKey
    )

    private fun tree(vararg nodes: AdvancementNodeDef) = TreeDef(
        namespace = "test",
        backgroundTexture = "minecraft:textures/block/stone.png",
        nodes = nodes.toList()
    )

    @Test
    fun `topological sort ensures root comes first for BuildTree`() {
        val treeDef = tree(
            node("child", "root"),
            node("root")
        )
        val sorted = treeDef.sortedNodes()
        assertEquals("root", sorted.first().key)
    }

    @Test
    fun `topological sort ensures parents precede children for BuildTree`() {
        val treeDef = tree(
            node("root"),
            node("a", "root"),
            node("b", "a"),
            node("c", "b")
        )
        val sorted = treeDef.sortedNodes()
        val keys = sorted.map { it.key }
        assertEquals(listOf("root", "a", "b", "c"), keys)
    }

    @Test
    fun `tree with branching produces valid build order`() {
        val treeDef = tree(
            node("root"),
            node("a", "root"),
            node("b", "root"),
            node("c", "a")
        )
        val sorted = treeDef.sortedNodes()
        val indexMap = sorted.mapIndexed { i, n -> n.key to i }.toMap()

        assertEquals(0, indexMap["root"])
        assertTrue(indexMap["a"]!! < indexMap["c"]!!, "a must come before c")
        assertEquals(4, sorted.size)
    }

    @Test
    fun `tree validation passes for valid tree before BuildTree`() {
        val treeDef = tree(
            node("root"),
            node("child1", "root"),
            node("child2", "root")
        )
        assertEquals(emptyList(), treeDef.validate())
    }

    @Test
    fun `root node is correctly identified`() {
        val treeDef = tree(
            node("root"),
            node("child1", "root")
        )
        assertEquals("root", treeDef.root.key)
    }

    @Test
    fun `sorted nodes separate root from children`() {
        val treeDef = tree(
            node("root"),
            node("child1", "root"),
            node("child2", "root")
        )
        val sorted = treeDef.sortedNodes()
        val root = sorted.first()
        val children = sorted.drop(1)

        assertTrue(root.parentKey == null, "First node should be root")
        assertTrue(children.all { it.parentKey != null }, "Remaining nodes should be children")
    }

    @Test
    fun `BuildTreeResult data class holds tab and advancements`() {
        // Verify the result type compiles and works as expected
        val result = BuildTreeResult(
            tab = mockk(),
            advancements = mapOf("root" to mockk(), "child" to mockk())
        )
        assertEquals(2, result.advancements.size)
        assertTrue(result.advancements.containsKey("root"))
        assertTrue(result.advancements.containsKey("child"))
    }

    // Inline mockk function for test — avoids importing full MockK for simple tests
    private inline fun <reified T : Any> mockk(): T = io.mockk.mockk(relaxed = true)
}
