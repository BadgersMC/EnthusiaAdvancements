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
 * Listens to EnthusiaMarket events for shop/market advancements.
 */
@Component
class ShopEventListener(
    private val plugin: JavaPlugin,
    private val registry: AdvancementRegistry,
    private val guildHook: LumaGuildsHook
) : Listener {

    @PostConstruct
    fun initialize() {
        // EnthusiaMarket events
        try {
            Class.forName("net.badgersmc.em.events.ShopCreatedEvent")
            Bukkit.getPluginManager().registerEvents(EMShopCreatedHandler(), plugin)
            Bukkit.getPluginManager().registerEvents(EMShopDeletedHandler(), plugin)
            Bukkit.getPluginManager().registerEvents(EMShopStockHandler(), plugin)
            plugin.logger.info("EnthusiaMarket shop listeners enabled")
        } catch (e: ClassNotFoundException) {
            plugin.logger.info("EnthusiaMarket not available, EM shop listeners disabled")
        }
    }

    private inner class EMShopCreatedHandler : Listener {
        @EventHandler
        fun onShopCreated(event: net.badgersmc.em.events.ShopCreatedEvent) {
            val player = Bukkit.getPlayer(event.ownerId) ?: return
            GrantProgress.execute(registry, RequirementType.SHOP_CREATED, null, player)
        }
    }

    private inner class EMShopDeletedHandler : Listener {
        @EventHandler
        fun onShopDeleted(event: net.badgersmc.em.events.ShopDeletedEvent) {
            val player = Bukkit.getPlayer(event.ownerId) ?: return
            GrantProgress.execute(registry, RequirementType.SHOP_DELETED, null, player)
        }
    }

    private inner class EMShopStockHandler : Listener {
        @EventHandler
        fun onStockDepleted(event: net.badgersmc.em.events.ShopStockDepletedEvent) {
            val owner = Bukkit.getPlayer(event.ownerId) ?: return
            GrantProgress.execute(registry, RequirementType.SHOP_STOCK_DEPLETED, null, owner)
        }
    }
}
