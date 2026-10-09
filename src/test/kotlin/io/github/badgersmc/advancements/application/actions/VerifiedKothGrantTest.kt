package io.github.badgersmc.advancements.application.actions

import io.github.badgersmc.advancements.commands.AdvancementCommand
import io.github.badgersmc.advancements.infrastructure.advancement.UltimateAdvancementAdapter
import io.mockk.*
import org.bukkit.command.CommandSender
import org.junit.jupiter.api.Test

class VerifiedKothGrantTest {
    @Test fun `admin grant cannot authorize a provider-owned advancement`() {
        val adapter=mockk<UltimateAdvancementAdapter>()
        val sender=mockk<CommandSender>(relaxed=true)
        every { adapter.isProviderOwned("ekoth","sovereign_blade") } returns true
        AdvancementCommand(adapter).grant(sender,"someone","ekoth","sovereign_blade")
        verify(exactly=0) { adapter.getAdvancement(any(),any()) }
    }
}
