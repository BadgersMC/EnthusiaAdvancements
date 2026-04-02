package io.github.badgersmc.advancements.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class RewardTest {

    @Test
    fun `Command reward stores command string`() {
        val reward = Reward.Command("give %player% diamond 1")
        assertEquals("give %player% diamond 1", reward.command)
    }

    @Test
    fun `Experience reward stores amount`() {
        val reward = Reward.Experience(100)
        assertEquals(100, reward.amount)
    }

    @Test
    fun `Item reward stores material and amount`() {
        val reward = Reward.Item("DIAMOND", 5)
        assertEquals("DIAMOND", reward.material)
        assertEquals(5, reward.amount)
    }

    @Test
    fun `Item reward defaults amount to 1`() {
        val reward = Reward.Item("DIAMOND")
        assertEquals(1, reward.amount)
    }

    @Test
    fun `Message reward stores text`() {
        val reward = Reward.Message("<green>Well done!")
        assertEquals("<green>Well done!", reward.text)
    }

    @Test
    fun `when expression exhaustively matches all variants`() {
        val rewards = listOf(
            Reward.Command("say hi"),
            Reward.Experience(50),
            Reward.Item("STONE", 64),
            Reward.Message("hello")
        )

        val labels = rewards.map { reward ->
            when (reward) {
                is Reward.Command -> "command"
                is Reward.Experience -> "experience"
                is Reward.Item -> "item"
                is Reward.Message -> "message"
            }
        }

        assertEquals(listOf("command", "experience", "item", "message"), labels)
    }

    @Test
    fun `data class equality works`() {
        assertEquals(Reward.Command("say hi"), Reward.Command("say hi"))
        assertEquals(Reward.Item("DIAMOND", 1), Reward.Item("DIAMOND"))
    }
}
