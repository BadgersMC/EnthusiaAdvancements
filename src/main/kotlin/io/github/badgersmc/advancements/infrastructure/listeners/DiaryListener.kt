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
class DiaryListener(
    private val plugin: JavaPlugin,
    private val registry: AdvancementRegistry
) : Listener {

    @PostConstruct
    fun initialize() {
        try {
            Class.forName("com.lincoln.diary.events.DiaryReceivedEvent")
            Bukkit.getPluginManager().registerEvents(DiaryEventHandler(), plugin)
            plugin.logger.info("DiaryKeeper listener enabled")
        } catch (e: ClassNotFoundException) {
            plugin.logger.info("DiaryKeeper not available, diary listener disabled")
        }
    }

    private inner class DiaryEventHandler : Listener {
        @EventHandler
        fun onReceived(event: com.lincoln.diary.events.DiaryReceivedEvent) {
            GrantProgress.execute(registry, RequirementType.DIARY_RECEIVED, null, event.player)
        }

        @EventHandler
        fun onFilled(event: com.lincoln.diary.events.DiaryFilledEvent) {
            GrantProgress.execute(registry, RequirementType.DIARY_FILLED, null, event.player)
        }

        @EventHandler
        fun onSigned(event: com.lincoln.diary.events.DiarySignedEvent) {
            GrantProgress.execute(registry, RequirementType.DIARY_SIGNED, null, event.player)
        }

        @EventHandler
        fun onObtained(event: com.lincoln.diary.events.DiaryObtainedEvent) {
            GrantProgress.execute(registry, RequirementType.DIARY_OBTAINED, null, event.player)
        }

        @EventHandler
        fun onVoidReturn(event: com.lincoln.diary.events.DiaryVoidReturnEvent) {
            GrantProgress.execute(registry, RequirementType.DIARY_VOID_RETURN, null, event.player)
        }

        @EventHandler
        fun onDestructionAttempt(event: com.lincoln.diary.events.DiaryDestructionAttemptEvent) {
            // No player on Item entity — find nearest player who owns this diary
            val item = event.item
            val world = item.world
            val nearby = world.getNearbyEntities(item.location, 32.0, 32.0, 32.0)
            for (entity in nearby) {
                if (entity is org.bukkit.entity.Player) {
                    GrantProgress.execute(registry, RequirementType.DIARY_DESTRUCTION_ATTEMPT, null, entity)
                    break
                }
            }
        }

        @EventHandler
        fun onContainerAttempt(event: com.lincoln.diary.events.DiaryContainerAttemptEvent) {
            GrantProgress.execute(registry, RequirementType.DIARY_CONTAINER_ATTEMPT, null, event.player)
        }

        // DiaryDuplicateWarningEvent handler deferred — event has no player field yet
    }
}
