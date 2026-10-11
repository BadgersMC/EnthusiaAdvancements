package io.github.badgersmc.advancements.pilot;

import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import java.util.List;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ParsedAdvancementDisplayTest {
    @BeforeAll static void initialize() { UaaDisplayFixture.initialize(); }
    @Test void rgbTitleAndDescriptionAreParsedForChatAndHover() {
        ItemStack icon = mock(ItemStack.class);
        when(icon.clone()).thenReturn(icon);
        var upstream = new AdvancementDisplay(icon, "§x§f§5§f§5§f§5Seen the Watcher",
            AdvancementFrameType.CHALLENGE, false, false, 0, 0, List.of("It has seen you too."));
        assertTrue(BaseComponent.toPlainText(upstream.getChatDescription()).contains("§"),
            "Reproduce UAA's raw legacy-code hover text before applying the adapter");
        var display = new ParsedAdvancementDisplay(icon, "§x§f§5§f§5§f§5Seen the Watcher",
            AdvancementFrameType.CHALLENGE, 0, 0, List.of("§x§9§a§a§0§a§6It has seen you too."));
        var title = display.getChatTitle();
        var hover = display.getChatDescription();
        assertEquals(1, title.length);
        assertEquals(1, hover.length);
        assertEquals("Seen the Watcher", BaseComponent.toPlainText(title));
        assertEquals("Seen the Watcher\nIt has seen you too.", BaseComponent.toPlainText(hover));
        assertFalse(BaseComponent.toPlainText(hover).contains("§"));
        assertTrue(title[0].toLegacyText().contains("§x§f§5§f§5§f§5"));
        assertTrue(hover[0].toLegacyText().contains("§x§9§a§a§0§a§6"));
        ((TextComponent) title[0]).setText("changed");
        assertEquals("Seen the Watcher", BaseComponent.toPlainText(display.getChatTitle()));
    }
}
