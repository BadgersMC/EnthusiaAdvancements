package io.github.badgersmc.advancements.infrastructure.listeners

import io.github.badgersmc.advancements.application.actions.GrantProgress
import io.github.badgersmc.advancements.application.ports.AdvancementRegistry
import io.github.badgersmc.advancements.domain.RequirementType
import io.github.badgersmc.advancements.infrastructure.plugins.LumaGuildsHook
import dev.enthusia.itemshops.events.PostShopTransactionEvent
import net.badgersmc.nexus.annotations.Component
import net.badgersmc.nexus.annotations.PostConstruct
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID

/**
 * Listens to ItemShops PostShopTransactionEvent and grants guild-scoped
 * shop advancement progress to all online guild members.
 *
 * Only active when both ItemShops and LumaGuilds are present.
 */
@Component
class ShopTransactionListener(
    private val plugin: JavaPlugin,
    private val registry: AdvancementRegistry,
    private val guildHook: LumaGuildsHook
) : Listener {

    @PostConstruct
    fun initialize() {
        if (!Bukkit.getPluginManager().isPluginEnabled("ItemShops")) {
            plugin.logger.info("ItemShops not found, shop advancement listener disabled")
            return
        }
        if (!guildHook.isAvailable()) {
            plugin.logger.info("LumaGuilds not available, shop advancement listener disabled")
            return
        }

        try {
            Bukkit.getPluginManager().registerEvents(this, plugin)
            plugin.logger.info("Shop transaction advancement listener enabled")
        } catch (e: NoClassDefFoundError) {
            plugin.logger.warning("ItemShops classes not found, shop listener disabled")
        }
    }

    @EventHandler
    fun onShopTransaction(event: PostShopTransactionEvent) {
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
