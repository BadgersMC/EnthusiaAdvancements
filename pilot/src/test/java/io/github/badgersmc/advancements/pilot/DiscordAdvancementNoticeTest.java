package io.github.badgersmc.advancements.pilot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class DiscordAdvancementNoticeTest {
    @Test
    void challengeCardContainsRequirementsAndRewardsWithoutFormattingCodes() {
        var notice = DiscordAdvancementNotice.from("FainNeito", node(
            "CHALLENGE", "<gold>One Thousand Hours</gold>",
            List.of("&cReach 1,000 active hours", "Reward: <gold>3,000 raw gold</gold>")
        ));
        assertEquals("FainNeito has completed the challenge One Thousand Hours", notice.heading());
        assertEquals("Reach 1,000 active hours\nReward: 3,000 raw gold", notice.description());
        assertEquals(0xB04BEE, notice.color());
    }

    @Test
    void taskAndGoalHaveDistinctHeadingsAndNoDiscordMentions() {
        var task = DiscordAdvancementNotice.from("Player", node("TASK", "First Step", List.of("@everyone")));
        var goal = DiscordAdvancementNotice.from("Player", node("GOAL", "Milestone", List.of("Ready")));
        assertTrue(task.heading().contains("has made the advancement"));
        assertTrue(goal.heading().contains("has reached the goal"));
        assertFalse(task.description().contains("@"));
    }

    private static ProjectionService.Node node(String frame, String title, List<String> description) {
        return new ProjectionService.Node("sample", null, title, description, Material.CLOCK, frame, 1, 0);
    }
}
