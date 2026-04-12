package io.github.badgersmc.advancements.infrastructure.listeners

import io.github.badgersmc.advancements.application.actions.GrantProgress
import io.github.badgersmc.advancements.application.ports.AdvancementRegistry
import io.github.badgersmc.advancements.domain.RequirementType
import io.github.badgersmc.advancements.infrastructure.plugins.LumaGuildsHook
import net.badgersmc.nexus.annotations.Component
import net.badgersmc.nexus.annotations.PostConstruct
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

/**
 * Listens to ItemShops and ARM-Guilds-Bridge events for shop/market advancements.
 */
@Component
class ShopEventListener(
    private val plugin: JavaPlugin,
    private val registry: AdvancementRegistry,
    private val guildHook: LumaGuildsHook
) : Listener {

    @PostConstruct
    fun initialize() {
        // ItemShops events
        try {
            Class.forName("dev.enthusia.itemshops.events.ShopCreatedEvent")
            Bukkit.getPluginManager().registerEvents(ShopCreatedHandler(), plugin)
            plugin.logger.info("ItemShops shop creation listener enabled")
        } catch (e: ClassNotFoundException) {
            plugin.logger.info("ItemShops not available, shop creation listener disabled")
        }

        // ARM-Guilds-Bridge events
        try {
            Class.forName("net.lumalyte.armbridge.events.GuildRegionPurchasedEvent")
            Bukkit.getPluginManager().registerEvents(RegionPurchasedHandler(), plugin)
            plugin.logger.info("ARM-Guilds-Bridge region purchase listener enabled")
        } catch (e: ClassNotFoundException) {
            plugin.logger.info("ARM-Guilds-Bridge not available, region purchase listener disabled")
        }
    }

    private inner class ShopCreatedHandler : Listener {
        @EventHandler
        fun onShopCreated(event: dev.enthusia.itemshops.events.ShopCreatedEvent) {
            val player = Bukkit.getPlayer(event.ownerId) ?: return
            GrantProgress.execute(registry, RequirementType.SHOP_CREATED, null, player)
        }
    }

    private inner class RegionPurchasedHandler : Listener {
        @EventHandler
        fun onRegionPurchased(event: net.lumalyte.armbridge.events.GuildRegionPurchasedEvent) {
            val members = guildHook.getOnlineGuildMembers(event.guildId)
            for (player in members) {
                GrantProgress.execute(registry, RequirementType.GUILD_REGION_PURCHASED, null, player)
            }
        }
    }
}
