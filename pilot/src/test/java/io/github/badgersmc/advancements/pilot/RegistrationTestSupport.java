package io.github.badgersmc.advancements.pilot;

import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fren_gor.ultimateAdvancementAPI.UltimateAdvancementAPI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

/** Shared setup for registration failure-path tests; invokes the real service. */
abstract class RegistrationTestSupport {

    protected record Fixture(
        PilotPlugin service,
        Plugin owner,
        UltimateAdvancementAPI api
    ) {}

    protected static Fixture fixture() throws Exception {
        PilotPlugin service = mock(PilotPlugin.class, CALLS_REAL_METHODS);
        UltimateAdvancementAPI api = mock(
            UltimateAdvancementAPI.class,
            RETURNS_DEEP_STUBS
        );
        var apiField = PilotPlugin.class.getDeclaredField("api");
        apiField.setAccessible(true);
        apiField.set(service, api);
        var trees = PilotPlugin.class.getDeclaredField("trees");
        trees.setAccessible(true);
        trees.set(service, new LinkedHashMap<>());
        return new Fixture(service, mock(Plugin.class), api);
    }

    protected static List<ProjectionService.Node> nodes(String key) {
        return List.of(
            new ProjectionService.Node(
                key,
                null,
                key,
                List.of(),
                Material.CLOCK,
                "TASK",
                1,
                0
            )
        );
    }

    protected static void register(Fixture f, String key) {
        f.service().registerTree(
            f.owner(),
            "test",
            new ItemStack(Material.CLOCK),
            nodes(key)
        );
    }

    protected static void project(Fixture f, String key) {
        Player player = mock(Player.class);
        when(player.isOnline()).thenReturn(true);
        when(f.api().isLoaded(player)).thenReturn(true);
        when(f.api().getTeamProgression(player).getSize()).thenReturn(1);
        f.service().project(f.owner(), "test", player, Map.of(key, 500));
    }

    protected static ItemStack icon(int amount) {
        var item = mock(ItemStack.class);
        when(item.getAmount()).thenReturn(amount);
        when(item.clone()).thenAnswer(invocation -> icon(item.getAmount()));
        return item;
    }
}
