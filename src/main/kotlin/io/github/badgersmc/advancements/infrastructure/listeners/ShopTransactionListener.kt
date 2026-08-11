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
 * Listens to EnthusiaMarket transaction events and grants guild-scoped
 * shop advancement progress to all online guild members.
 *
 * Only active when both EnthusiaMarket and LumaGuilds are present.
 */
@Component
class ShopTransactionListener(
    private val plugin: JavaPlugin,
    private val registry: AdvancementRegistry,
    private val guildHook: LumaGuildsHook
) : Listener {

    @PostConstruct
    fun initialize() {
        if (!guildHook.isAvailable()) {
            plugin.logger.info("LumaGuilds not available, shop advancement listener disabled")
            return
        }

        // EnthusiaMarket transaction events
        try {
            Class.forName("net.badgersmc.em.events.PostShopTransactionEvent")
            Bukkit.getPluginManager().registerEvents(EMShopTransactionHandler(), plugin)
            plugin.logger.info("EnthusiaMarket transaction listener enabled")
        } catch (e: ClassNotFoundException) {
            plugin.logger.info("EnthusiaMarket not available, EM transaction listener disabled")
        }
    }

    private inner class EMShopTransactionHandler : Listener {
        @EventHandler
        fun onPostShopTransaction(event: net.badgersmc.em.events.PostShopTransactionEvent) {
            val buyerGuildId = guildHook.getPlayerGuildId(event.buyer.uniqueId)
            val sellerGuildId = guildHook.getPlayerGuildId(event.landlordId)

            // Grant GUILD_SHOP_SALE to all online members of seller's guild
            if (sellerGuildId != null) {
                val sellerMembers = guildHook.getOnlineGuildMembers(sellerGuildId)
                for (player in sellerMembers) {
                    GrantProgress.execute(registry, RequirementType.GUILD_SHOP_SALE, null, player)
                }
            }

            // Grant GUILD_SHOP_PURCHASE to buyer
            if (buyerGuildId != null) {
                GrantProgress.execute(registry, RequirementType.GUILD_SHOP_PURCHASE, null, event.buyer)
            }
        }
    }
}
