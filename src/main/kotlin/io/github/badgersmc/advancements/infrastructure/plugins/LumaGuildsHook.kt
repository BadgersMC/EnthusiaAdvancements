package io.github.badgersmc.advancements.infrastructure.plugins

import io.github.badgersmc.advancements.application.actions.GrantProgress
import io.github.badgersmc.advancements.application.ports.AdvancementRegistry
import io.github.badgersmc.advancements.application.ports.PluginHook
import io.github.badgersmc.advancements.domain.RequirementType
import net.badgersmc.nexus.annotations.Component
import net.badgersmc.nexus.annotations.PostConstruct
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

@Component
class LumaGuildsHook(
    private val plugin: JavaPlugin,
    private val registry: AdvancementRegistry
) : PluginHook, Listener {

    override val pluginName: String = "LumaGuilds"

    override fun isAvailable(): Boolean {
        return Bukkit.getPluginManager().isPluginEnabled("LumaGuilds")
    }

    @PostConstruct
    override fun onEnable() {
        if (!isAvailable()) {
            plugin.logger.info("LumaGuilds not found, skipping guild advancement hooks")
            return
        }

        try {
            Bukkit.getPluginManager().registerEvents(this, plugin)
            plugin.logger.info("LumaGuilds integration enabled")
        } catch (e: NoClassDefFoundError) {
            plugin.logger.warning("LumaGuilds classes not found on classpath, skipping guild hooks")
        }
    }

    @EventHandler
    fun onGuildCreated(event: org.bukkit.event.Event) {
        // Uses reflection-style approach: listen for any event and check class name
        // This avoids compile-time dependency on LumaGuilds event classes
        if (event.javaClass.simpleName != "GuildCreatedEvent") return

        try {
            val playerMethod = event.javaClass.getMethod("getPlayer")
            val player = playerMethod.invoke(event) as? org.bukkit.entity.Player ?: return
            GrantProgress.execute(registry, RequirementType.GUILD_CREATE, null, player)
        } catch (e: Exception) {
            plugin.logger.warning("Failed to handle GuildCreatedEvent: ${e.message}")
        }
    }

    @EventHandler
    fun onGuildMemberJoin(event: org.bukkit.event.Event) {
        if (event.javaClass.simpleName != "GuildMemberJoinEvent") return

        try {
            val playerMethod = event.javaClass.getMethod("getPlayer")
            val player = playerMethod.invoke(event) as? org.bukkit.entity.Player ?: return
            GrantProgress.execute(registry, RequirementType.GUILD_MEMBER_JOIN, null, player)
        } catch (e: Exception) {
            plugin.logger.warning("Failed to handle GuildMemberJoinEvent: ${e.message}")
        }
    }
}
