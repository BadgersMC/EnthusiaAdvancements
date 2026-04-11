package io.github.badgersmc.advancements.infrastructure.listeners

import io.github.badgersmc.advancements.application.actions.GrantProgress
import io.github.badgersmc.advancements.application.ports.AdvancementRegistry
import io.github.badgersmc.advancements.domain.RequirementType
import io.github.badgersmc.advancements.infrastructure.plugins.LumaGuildsHook
import net.badgersmc.nexus.annotations.Component
import net.badgersmc.nexus.annotations.PostConstruct
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Monster
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.inventory.FurnaceExtractEvent
import org.bukkit.plugin.java.JavaPlugin

/**
 * Listens to vanilla Bukkit events and grants guild-collective advancement progress
 * to all online members of the acting player's guild.
 *
 * Only active when LumaGuilds is present on the server.
 */
@Component
class GuildCollectiveListener(
    private val plugin: JavaPlugin,
    private val registry: AdvancementRegistry,
    private val guildHook: LumaGuildsHook
) : Listener {

    companion object {
        private val CROP_MATERIALS = setOf(
            Material.WHEAT, Material.CARROTS, Material.POTATOES,
            Material.BEETROOTS, Material.NETHER_WART,
            Material.COCOA, Material.SWEET_BERRY_BUSH,
            Material.MELON, Material.PUMPKIN
        )

        private const val DRAGON_MEMBER_THRESHOLD = 3
    }

    @PostConstruct
    fun initialize() {
        if (!guildHook.isAvailable()) {
            plugin.logger.info("LumaGuilds not available, guild collective listener disabled")
            return
        }

        try {
            Bukkit.getPluginManager().registerEvents(this, plugin)
            plugin.logger.info("Guild collective advancement listener enabled")
        } catch (e: NoClassDefFoundError) {
            plugin.logger.warning("LumaGuilds classes not found, guild collective listener disabled")
        }
    }

    @EventHandler
    fun onFurnaceExtract(event: FurnaceExtractEvent) {
        val guildId = guildHook.getPlayerGuildId(event.player.uniqueId) ?: return
        val members = guildHook.getOnlineGuildMembers(guildId)
        for (player in members) {
            repeat(event.itemAmount) {
                GrantProgress.execute(registry, RequirementType.GUILD_COLLECTIVE_SMELT, null, player)
            }
        }
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        val guildId = guildHook.getPlayerGuildId(event.player.uniqueId) ?: return
        val members = guildHook.getOnlineGuildMembers(guildId)

        // General block break
        for (player in members) {
            GrantProgress.execute(registry, RequirementType.GUILD_COLLECTIVE_BLOCK_BREAK, null, player)
        }

        // Crop harvest
        if (event.block.type in CROP_MATERIALS) {
            for (player in members) {
                GrantProgress.execute(registry, RequirementType.GUILD_COLLECTIVE_CROP_HARVEST, null, player)
            }
        }
    }

    @EventHandler
    fun onEntityDeath(event: EntityDeathEvent) {
        val killer = event.entity.killer ?: return
        val guildId = guildHook.getPlayerGuildId(killer.uniqueId) ?: return

        // Boss kills (Ender Dragon, Wither)
        when (event.entity.type) {
            EntityType.ENDER_DRAGON -> {
                // Require minimum guild members present in The End
                val membersInEnd = guildHook.getOnlineGuildMembers(guildId)
                    .filter { it.world == event.entity.world }
                if (membersInEnd.size >= DRAGON_MEMBER_THRESHOLD) {
                    for (player in membersInEnd) {
                        GrantProgress.execute(registry, RequirementType.GUILD_COLLECTIVE_BOSS_KILL, "ENDER_DRAGON", player)
                    }
                }
                return
            }
            EntityType.WITHER -> {
                val membersInWorld = guildHook.getOnlineGuildMembers(guildId)
                    .filter { it.world == event.entity.world }
                for (player in membersInWorld) {
                    GrantProgress.execute(registry, RequirementType.GUILD_COLLECTIVE_BOSS_KILL, "WITHER", player)
                }
                return
            }
            else -> {}
        }

        // Hostile mob kills
        if (event.entity is Monster) {
            val members = guildHook.getOnlineGuildMembers(guildId)
            for (player in members) {
                GrantProgress.execute(registry, RequirementType.GUILD_COLLECTIVE_MOB_KILL, null, player)
            }
        }
    }

    /**
     * Friendly fire detection — triggers when a player damages a guildmate.
     */
    @EventHandler
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        val attacker = event.damager as? Player ?: return
        val victim = event.entity as? Player ?: return

        val attackerGuild = guildHook.getPlayerGuildId(attacker.uniqueId) ?: return
        val victimGuild = guildHook.getPlayerGuildId(victim.uniqueId) ?: return

        if (attackerGuild == victimGuild) {
            GrantProgress.execute(registry, RequirementType.GUILD_FRIENDLY_FIRE, null, attacker)
        }
    }
}
