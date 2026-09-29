package io.github.badgersmc.advancements.pilot;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.bukkit.ChatColor;

/** Bounded, plain-text content for one explicit live advancement celebration. */
record DiscordAdvancementNotice(String heading, String description, int color) {
    private static final Pattern MINI_MESSAGE_TAG = Pattern.compile("<[/!#a-zA-Z][^>]*>");
    private static final Pattern HEX_COLOR = Pattern.compile("(?i)&#[0-9a-f]{6}");
    private static final Pattern LEGACY_COLOR = Pattern.compile("(?i)&[0-9a-fk-or]");

    static DiscordAdvancementNotice from(String playerName, ProjectionService.Node node) {
        String frame = node.frame().toUpperCase(Locale.ROOT);
        String kind = switch (frame) {
            case "GOAL" -> "has reached the goal";
            case "CHALLENGE" -> "has completed the challenge";
            default -> "has made the advancement";
        };
        int color = switch (frame) {
            case "GOAL" -> 0xFFC400;
            case "CHALLENGE" -> 0xB04BEE;
            default -> 0x55C977;
        };
        String heading = bounded(clean(playerName) + " " + kind + " " + clean(node.title()), 256);
        String description = bounded(joinDescription(node.description()), 3500);
        return new DiscordAdvancementNotice(heading, description, color);
    }

    private static String joinDescription(List<String> lines) {
        return lines.stream().map(DiscordAdvancementNotice::clean)
            .filter(line -> !line.isBlank()).collect(Collectors.joining("\n"));
    }

    private static String clean(String value) {
        if (value == null) return "";
        String plain = ChatColor.stripColor(MINI_MESSAGE_TAG.matcher(value).replaceAll(""));
        plain = HEX_COLOR.matcher(plain).replaceAll("");
        plain = LEGACY_COLOR.matcher(plain).replaceAll("");
        return plain.replace('@', '＠').replace('\r', ' ').trim();
    }

    private static String bounded(String value, int limit) {
        return value.length() <= limit ? value : value.substring(0, limit - 1) + "…";
    }
}
