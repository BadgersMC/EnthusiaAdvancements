package io.github.badgersmc.advancements.infrastructure.rewards

import io.github.badgersmc.advancements.application.ports.RewardExecutor
import io.github.badgersmc.advancements.domain.Reward
import net.badgersmc.nexus.annotations.Service
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level

@Service
class BukkitRewardExecutor(
    private val plugin: JavaPlugin
) : RewardExecutor {

    override fun execute(player: Player, rewards: List<Reward>) {
        for (reward in rewards) {
            try {
                when (reward) {
                    is Reward.Command -> {
                        val cmd = reward.command.replace("%player%", player.name)
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd)
                    }
                    is Reward.Experience -> {
                        player.giveExp(reward.amount)
                    }
                    is Reward.Item -> {
                        val item = ItemStack(Material.valueOf(reward.material), reward.amount)
                        player.inventory.addItem(item)
                    }
                    is Reward.Message -> {
                        val component = MiniMessage.miniMessage().deserialize(reward.text)
                        player.sendMessage(component)
                    }
                }
            } catch (e: Exception) {
                plugin.logger.log(Level.WARNING, "Failed to execute reward $reward for ${player.name}", e)
            }
        }
    }
}
