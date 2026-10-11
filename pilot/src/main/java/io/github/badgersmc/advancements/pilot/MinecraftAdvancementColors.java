package io.github.badgersmc.advancements.pilot;

import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

/** Changes only frame-colored chat text on a copy, never shared display data. */
final class MinecraftAdvancementColors {
    private MinecraftAdvancementColors() {}

    static BaseComponent[] format(BaseComponent[] original, AdvancementFrameType frame) {
        return format(original, frame, null, null, null);
    }

    static BaseComponent[] format(BaseComponent[] original, AdvancementFrameType frame,
                                  String namespace, String key, String title) {
        if (original == null) return null;
        ChatColor replacement = frame == AdvancementFrameType.CHALLENGE
                ? ChatColor.DARK_RED : ChatColor.GOLD;
        if ("holidays".equals(namespace) && isWatcher(key) && title != null && !title.isBlank()) {
            for (BaseComponent part : TextComponent.fromLegacyText(title)) {
                if (!part.toPlainText().isEmpty() && part.getColorRaw() != null) {
                    replacement = part.getColorRaw();
                    break;
                }
            }
        }
        BaseComponent[] result = new BaseComponent[original.length];
        for (int i = 0; i < original.length; i++) {
            result[i] = original[i].duplicate();
            recolor(result[i], frame.getColor(), replacement);
        }
        return result;
    }

    private static boolean isWatcher(String key) {
        return "seen_the_watcher".equals(key) || "dont_blink".equals(key) || "forgotten_melody".equals(key);
    }

    private static void recolor(BaseComponent component, ChatColor previous, ChatColor replacement) {
        if (previous.equals(component.getColorRaw())) component.setColor(replacement);
        if (component.getExtra() != null) {
            for (BaseComponent child : component.getExtra()) recolor(child, previous, replacement);
        }
    }
}
