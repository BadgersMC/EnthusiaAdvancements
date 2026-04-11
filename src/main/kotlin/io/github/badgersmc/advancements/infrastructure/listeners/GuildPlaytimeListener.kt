package io.github.badgersmc.advancements.infrastructure.listeners

import io.github.badgersmc.advancements.application.actions.GrantProgress
import io.github.badgersmc.advancements.application.ports.AdvancementRegistry
import io.github.badgersmc.advancements.domain.RequirementType
import io.github.badgersmc.advancements.infrastructure.plugins.LumaGuildsHook
import net.badgersmc.nexus.annotations.Component
import net.badgersmc.nexus.annotations.PostConstruct
import org.bukkit.Bukkit
import org.bukkit.Statistic
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

/**
 * Periodically checks combined guild playtime using Bukkit's PLAY_ONE_MINUTE statistic.
 * Grants GUILD_COMBINED_PLAYTIME progress to online guild members when thresholds are met.
 *
 * Runs every 5 minutes to avoid excessive computation.
 */
@Component
class GuildPlaytimeListener(
    private val plugin: JavaPlugin,
    private val registry: AdvancementRegistry,
    private val guildHook: LumaGuildsHook
) : Listener {

    companion object {
        private const val CHECK_INTERVAL_TICKS = 6000L // 5 minutes
        private const val TICKS_PER_HOUR = 72000 // 20 ticks/sec * 3600 sec
    }

    @PostConstruct
    fun initialize() {
        if (!guildHook.isAvailable()) {
            plugin.logger.info("LumaGuilds not available, guild playtime tracker disabled")
            return
        }

        Bukkit.getScheduler().runTaskTimer(plugin, Runnable { checkGuildPlaytime() }, CHECK_INTERVAL_TICKS, CHECK_INTERVAL_TICKS)
        plugin.logger.info("Guild playtime tracker enabled (5min interval)")
    }

    private fun checkGuildPlaytime() {
        try {
            val memberRepo = org.koin.core.context.GlobalContext.get()
                .get<net.lumalyte.lg.application.persistence.MemberRepository>()

            // Group online players by guild
            val guildPlayers = mutableMapOf<java.util.UUID, MutableList<org.bukkit.entity.Player>>()
            for (player in Bukkit.getOnlinePlayers()) {
                val guildId = guildHook.getPlayerGuildId(player.uniqueId) ?: continue
                guildPlayers.getOrPut(guildId) { mutableListOf() }.add(player)
            }

            for ((guildId, onlinePlayers) in guildPlayers) {
                // Get ALL guild members (including offline) for total playtime calc
                val allMemberIds = memberRepo.getByGuild(guildId).map { it.playerId }.toSet()

                // Sum playtime across all members (online + offline via OfflinePlayer)
                var totalHours = 0L
                for (memberId in allMemberIds) {
                    val offlinePlayer = Bukkit.getOfflinePlayer(memberId)
                    val ticks = offlinePlayer.getStatistic(Statistic.PLAY_ONE_MINUTE)
                    totalHours += ticks / TICKS_PER_HOUR
                }

                // Grant progress = total combined hours to each online member
                // UAAPI tracks max progression, so re-granting same value = no-op
                for (player in onlinePlayers) {
                    repeat(totalHours.toInt().coerceAtMost(1000)) {
                        // incrementProgression adds 1 each call, capped at maxProgression
                        GrantProgress.execute(registry, RequirementType.GUILD_COMBINED_PLAYTIME, null, player)
                    }
                }
            }
        } catch (e: Exception) {
            plugin.logger.warning("Guild playtime check failed: ${e.message}")
        }
    }
}
