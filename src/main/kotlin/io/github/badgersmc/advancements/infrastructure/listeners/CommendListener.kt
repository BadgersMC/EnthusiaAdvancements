package io.github.badgersmc.advancements.infrastructure.listeners

import io.github.badgersmc.advancements.application.actions.GrantProgress
import io.github.badgersmc.advancements.application.ports.AdvancementRegistry
import io.github.badgersmc.advancements.domain.RequirementType
import net.badgersmc.nexus.annotations.Component
import net.badgersmc.nexus.annotations.PostConstruct
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

@Component
class CommendListener(
    private val plugin: JavaPlugin,
    private val registry: AdvancementRegistry
) : Listener {

    @PostConstruct
    fun initialize() {
        try {
            Class.forName("org.enthusia.rep.events.CommendationReceivedEvent")
            Bukkit.getPluginManager().registerEvents(CommendHandler(), plugin)
            plugin.logger.info("EnthusiaCommend listener enabled")
        } catch (e: ClassNotFoundException) {
            plugin.logger.info("EnthusiaCommend not available, commend listener disabled")
        }
    }

    private inner class CommendHandler : Listener {
        @EventHandler
        fun onReceived(event: org.enthusia.rep.events.CommendationReceivedEvent) {
            val player = Bukkit.getPlayer(event.targetId) ?: return
            val target = if (event.isPositive) "POSITIVE" else "NEGATIVE"
            GrantProgress.execute(registry, RequirementType.COMMEND_RECEIVED, target, player)
        }

        @EventHandler
        fun onGiven(event: org.enthusia.rep.events.CommendationGivenEvent) {
            val player = Bukkit.getPlayer(event.giverId) ?: return
            GrantProgress.execute(registry, RequirementType.COMMEND_GIVEN, null, player)
        }

        @EventHandler
        fun onMilestone(event: org.enthusia.rep.events.RepMilestoneReachedEvent) {
            val player = Bukkit.getPlayer(event.playerId) ?: return
            GrantProgress.execute(registry, RequirementType.REP_MILESTONE, event.newScore.toString(), player)
        }
    }
}
