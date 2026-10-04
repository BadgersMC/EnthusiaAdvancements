package io.github.badgersmc.advancements.pilot;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.util.PlayerUtil;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/** Best-effort DiscordSRV delivery; never controls toasts, progress, or rewards. */
final class DiscordAdvancementAnnouncer {
    private final JavaPlugin owner;
    private long nextWarning;
    private final Semaphore iconJobs = new Semaphore(8);

    DiscordAdvancementAnnouncer(JavaPlugin owner) {
        this.owner = owner;
    }

    void announce(Player player, String namespace, ProjectionService.Node node) {
        if (!owner.getConfig().getBoolean("discord.enabled", false)) return;
        if (!Bukkit.getPluginManager().isPluginEnabled("DiscordSRV")) return;
        try {
            if (PlayerUtil.isVanished(player)) return;
            var discord = DiscordSRV.getPlugin();
            String configured = owner.getConfig().getString("discord.channel", "awards");
            String gameChannel = discord.getOptionalChannel(configured);
            var channel = discord.getDestinationTextChannelForGameChannelName(gameChannel);
            if (channel == null) {
                warn("DiscordSRV advancement channel is unavailable: " + configured, null);
                return;
            }
            var notice = DiscordAdvancementNotice.from(player.getName(), node);
            var embed = new EmbedBuilder().setColor(notice.color()).setTitle(notice.heading())
                .setDescription(notice.description());
            String avatarUrl = DiscordSRV.getAvatarUrl(player);
            if (avatarUrl != null && avatarUrl.startsWith("https://"))
                embed.setAuthor(player.getName(), null, avatarUrl);
            String iconUrl = iconUrl(namespace, node.key());
            if (iconUrl != null) embed.setThumbnail(iconUrl);
            if (iconUrl == null && renderIcon(player, node, embed, channel)) return;
            channel.sendMessageEmbeds(embed.build()).queue(
                sent -> { }, failure -> warn("Discord advancement delivery failed", failure));
        } catch (RuntimeException | LinkageError failure) {
            warn("Discord advancement bridge unavailable", failure);
        }
    }

    private boolean renderIcon(Player player, ProjectionService.Node node, EmbedBuilder embed,
        github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel channel) {
        if (!owner.getConfig().getBoolean("discord.auto-icons", true)) return false;
        var addon = Bukkit.getPluginManager().getPlugin("InteractiveChatDiscordSrvAddon");
        if (addon == null || !addon.isEnabled() || !iconJobs.tryAcquire()) return false;
        try {
            var job = DiscordAdvancementIcon.prepare(addon.getClass().getClassLoader(), player,
                PilotDisplays.createIcon(node), node.frame());
            Bukkit.getScheduler().runTaskAsynchronously(owner, () -> {
                byte[] rendered = null;
                try {
                    if (owner.isEnabled() && addon.isEnabled()) rendered = job.render();
                } catch (ReflectiveOperationException | java.io.IOException | RuntimeException | LinkageError failure) {
                    warn("Discord advancement icon unavailable; sending text only", failure);
                } finally {
                    iconJobs.release();
                }
                byte[] attachment = rendered;
                if (!owner.isEnabled()) return;
                try {
                    Bukkit.getScheduler().runTask(owner, () -> {
                        try {
                        if (!owner.isEnabled() || PlayerUtil.isVanished(player)) return;
                        if (attachment != null) embed.setThumbnail("attachment://advancement.png");
                        var message = channel.sendMessageEmbeds(embed.build());
                        if (attachment != null) message.addFile(attachment, "advancement.png");
                        message.queue(sent -> { }, failure -> warn("Discord advancement delivery failed", failure));
                        } catch (RuntimeException | LinkageError failure) {
                            warn("Discord advancement delivery unavailable", failure);
                        }
                    });
                } catch (RuntimeException failure) {
                    warn("Discord advancement callback unavailable", failure);
                }
            });
            return true;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError failure) {
            iconJobs.release();
            warn("Discord advancement renderer unavailable; sending text only", failure);
            return false;
        }
    }

    private String iconUrl(String namespace, String key) {
        var configured = owner.getConfig().getConfigurationSection("discord.icon-urls");
        Map<String, Object> urls = configured == null ? Map.of() : configured.getValues(false);
        Object value = urls.get(namespace + "/" + key);
        return value instanceof String url && url.startsWith("https://") ? url : null;
    }

    private synchronized void warn(String message, Throwable failure) {
        if (System.currentTimeMillis() < nextWarning) return;
        nextWarning = System.currentTimeMillis() + 60_000;
        owner.getLogger().log(Level.WARNING, message, failure);
    }
}
