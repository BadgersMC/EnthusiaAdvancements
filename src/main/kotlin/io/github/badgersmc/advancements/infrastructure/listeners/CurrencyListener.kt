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
class CurrencyListener(
    private val plugin: JavaPlugin,
    private val registry: AdvancementRegistry
) : Listener {

    @PostConstruct
    fun initialize() {
        try {
            Class.forName("com.enthusia.enthusiacurrency.event.BaltopTopEnterEvent")
            Bukkit.getPluginManager().registerEvents(BaltopHandler(), plugin)
            plugin.logger.info("EnthusiaCurrency listener enabled")
        } catch (e: ClassNotFoundException) {
            plugin.logger.info("EnthusiaCurrency not available, currency listener disabled")
        }
    }

    private inner class BaltopHandler : Listener {
        @EventHandler
        fun onBaltopEnter(event: com.enthusia.enthusiacurrency.event.BaltopTopEnterEvent) {
            val player = Bukkit.getPlayer(event.playerId) ?: return
            GrantProgress.execute(registry, RequirementType.BALTOP_ENTER, event.rank.toString(), player)
        }

        @EventHandler
        fun onDeposit(event: com.enthusia.enthusiacurrency.event.CurrencyDepositEvent) {
            val player = Bukkit.getPlayer(event.playerId) ?: return
            GrantProgress.execute(registry, RequirementType.CURRENCY_DEPOSIT, null, player)
        }

        @EventHandler
        fun onWithdraw(event: com.enthusia.enthusiacurrency.event.CurrencyWithdrawEvent) {
            val player = Bukkit.getPlayer(event.playerId) ?: return
            GrantProgress.execute(registry, RequirementType.CURRENCY_WITHDRAW, null, player)
        }

        @EventHandler
        fun onPay(event: com.enthusia.enthusiacurrency.event.CurrencyPayEvent) {
            val player = Bukkit.getPlayer(event.senderId) ?: return
            GrantProgress.execute(registry, RequirementType.CURRENCY_PAY, null, player)
        }

        @EventHandler
        fun onPaySelf(event: com.enthusia.enthusiacurrency.event.CurrencyPaySelfAttemptEvent) {
            val player = Bukkit.getPlayer(event.playerId) ?: return
            GrantProgress.execute(registry, RequirementType.CURRENCY_PAY_SELF, null, player)
        }

        @EventHandler
        fun onBalanceZero(event: com.enthusia.enthusiacurrency.event.CurrencyBalanceZeroEvent) {
            val player = Bukkit.getPlayer(event.playerId) ?: return
            GrantProgress.execute(registry, RequirementType.CURRENCY_BALANCE_ZERO, null, player)
        }
    }
}
