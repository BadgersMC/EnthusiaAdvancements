package io.github.badgersmc.advancements.pilot;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fren_gor.ultimateAdvancementAPI.AdvancementTab;
import com.fren_gor.ultimateAdvancementAPI.UltimateAdvancementAPI;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.RootAdvancement;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.Test;

class ProjectionBoundaryTest {

    private static final class Fixture {

        final PilotPlugin service = mock(PilotPlugin.class, CALLS_REAL_METHODS);
        final Plugin owner = mock(Plugin.class);
        final Player player = mock(Player.class);
        final RootAdvancement root = mock(RootAdvancement.class);
        final BaseAdvancement node = mock(BaseAdvancement.class);
        final AdvancementTab tab = mock(AdvancementTab.class);
    }

    private Fixture fixture() throws Exception {
        Fixture f = new Fixture();
        UltimateAdvancementAPI api = mock(
            UltimateAdvancementAPI.class,
            RETURNS_DEEP_STUBS
        );
        when(f.player.isOnline()).thenReturn(true);
        when(api.isLoaded(f.player)).thenReturn(true);
        when(api.getTeamProgression(f.player).getSize()).thenReturn(1);
        installField(f.service, "api", api);
        installField(
            f.service,
            "trees",
            new LinkedHashMap<>(Map.of("test", tree(f)))
        );
        return f;
    }

    private Object tree(Fixture f) throws Exception {
        Class<?> type = Arrays.stream(PilotPlugin.class.getDeclaredClasses())
            .filter(candidate -> candidate.getSimpleName().equals("Tree"))
            .findFirst()
            .orElseThrow();
        var constructor = type.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        return constructor.newInstance(
            f.owner,
            f.tab,
            f.root,
            Map.of("node", f.node),
            null
        );
    }

    private void installField(PilotPlugin service, String name, Object value)
        throws Exception {
        var field = PilotPlugin.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(service, value);
    }

    private void project(Fixture f, Plugin owner, Map<String, Integer> progress)
        throws Exception {
        try {
            ProjectionService.class
                .getMethod(
                    "project",
                    Plugin.class,
                    String.class,
                    Player.class,
                    Map.class
                )
                .invoke(f.service, owner, "test", f.player, progress);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException failure) throw failure;
            throw e;
        }
    }

    @Test
    void nullEntryRejectsEntireMapBeforeAnyDisplayMutation() throws Exception {
        var f = fixture();
        var progress = new LinkedHashMap<String, Integer>();
        progress.put("node", 500);
        progress.put("bad", null);
        try (var bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(Bukkit::isPrimaryThread).thenReturn(true);
            assertThrows(IllegalArgumentException.class, () ->
                project(f, f.owner, progress)
            );
            verifyNoInteractions(f.root, f.node, f.tab);
        }
    }

    @Test
    void otherProviderCannotProjectOrCelebrate() throws Exception {
        var f = fixture();
        Plugin other = mock(Plugin.class);
        try (var bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(Bukkit::isPrimaryThread).thenReturn(true);
            assertThrows(IllegalArgumentException.class, () ->
                project(f, other, Map.of("node", 1000))
            );
            assertThrows(IllegalArgumentException.class, () ->
                f.service.celebrate(other, "test", f.player, "node")
            );
            verifyNoInteractions(f.root, f.node, f.tab);
        }
    }

    @Test
    void validOwnerRetainsUnavailableSentinelAndProjectsValidValues()
        throws Exception {
        var f = fixture();
        try (var bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(Bukkit::isPrimaryThread).thenReturn(true);
            project(f, f.owner, Map.of("node", -1));
            verify(f.node, never()).setProgression(
                any(Player.class),
                anyInt(),
                anyBoolean()
            );
            project(f, f.owner, Map.of("node", 999));
            verify(f.node).setProgression(f.player, 99, false);
        }
    }

    @Test
    void ownerlessCompatibilityMethodsFailClosed() throws Exception {
        var f = fixture();
        assertThrows(UnsupportedOperationException.class, () ->
            f.service.project("test", f.player, Map.of())
        );
        assertThrows(UnsupportedOperationException.class, () ->
            f.service.celebrate("test", f.player, "node")
        );
    }
}
