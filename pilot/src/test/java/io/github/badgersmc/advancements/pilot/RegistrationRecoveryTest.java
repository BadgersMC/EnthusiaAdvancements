package io.github.badgersmc.advancements.pilot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fren_gor.ultimateAdvancementAPI.AdvancementTab;
import com.fren_gor.ultimateAdvancementAPI.advancement.RootAdvancement;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

class RegistrationRecoveryTest extends RegistrationTestSupport {
    @Test
    void creationFailureRebuildsPreviousTree() throws Exception {
        var f = fixture();
        var failure = new IllegalStateException("creation failed");
        var restored = creationFailure(f, failure);
        register(f, "old");
        assertSame(failure, replacementFailure(f));
        verify(f.api(), times(3)).createAdvancementTab("test");
        verifyRegistered(restored);
        verifyOldProgress(f);
    }

    @Test
    void registrationFailureDisposesNewTabAndRebuildsPreviousTree() throws Exception {
        var f = fixture();
        var failure = new IllegalStateException("registration failed");
        var restored = registrationFailure(f, failure);
        register(f, "old");
        assertSame(failure, replacementFailure(f));
        verify(f.api(), times(2)).unregisterAdvancementTab("test");
        verifyRegistered(restored);
        verifyOldProgress(f);
    }

    @Test
    void recoveryFailureIsSuppressedAndLeavesNoDisposedTreeRegistered() throws Exception {
        var f = fixture();
        var failure = new IllegalStateException("replacement failed");
        var recovery = new IllegalStateException("recovery failed");
        when(f.api().createAdvancementTab("test"))
            .thenReturn(mock(AdvancementTab.class)).thenThrow(failure).thenThrow(recovery);
        register(f, "old");
        assertSame(failure, replacementFailure(f));
        assertSame(recovery, failure.getSuppressed()[0]);
        verifyNoProjection(f, "old");
    }

    @Test
    void invalidDisplayIsRejectedBeforeRemovingExistingTree() throws Exception {
        var f = fixture();
        when(f.api().createAdvancementTab("test")).thenReturn(mock(AdvancementTab.class));
        register(f, "old");
        assertThrows(IllegalArgumentException.class, () -> registerInvalid(f));
        verify(f.api(), never()).unregisterAdvancementTab("test");
        verifyOldProgress(f);
    }

    @Test
    void recoveryUsesSnapshotsInsteadOfMutatedProviderInputs() throws Exception {
        var f = fixture();
        var failure = new IllegalStateException("replacement failed");
        creationFailure(f, failure);
        registerThenMutateInputs(f);
        assertSame(failure, replacementFailure(f));
        assertEquals(List.of(1, 1), restoredAmounts);
        verifyOldProgress(f);
    }

    @Test
    void firstRegistrationFailureDoesNotInventPreviousTree() throws Exception {
        var f = fixture();
        var failure = new IllegalStateException("registration failed");
        var tab = failingTab(failure);
        when(f.api().createAdvancementTab("test")).thenReturn(tab);
        assertSame(failure, replacementFailure(f));
        verify(f.api()).createAdvancementTab("test");
        verify(f.api()).unregisterAdvancementTab("test");
        verifyNoProjection(f, "new");
    }

    private static AdvancementTab creationFailure(Fixture f, RuntimeException failure) {
        var restored = mock(AdvancementTab.class);
        when(f.api().createAdvancementTab("test"))
            .thenReturn(mock(AdvancementTab.class)).thenThrow(failure).thenReturn(restored);
        return restored;
    }

    private static AdvancementTab registrationFailure(Fixture f, RuntimeException failure) {
        var restored = mock(AdvancementTab.class);
        var replacement = failingTab(failure);
        when(f.api().createAdvancementTab("test"))
            .thenReturn(mock(AdvancementTab.class), replacement, restored);
        return restored;
    }

    private static AdvancementTab failingTab(RuntimeException failure) {
        var tab = mock(AdvancementTab.class);
        doThrow(failure).when(tab).registerAdvancements(any(RootAdvancement.class), anySet());
        return tab;
    }

    private static RuntimeException replacementFailure(Fixture f) {
        return assertThrows(IllegalStateException.class, () -> register(f, "new"));
    }

    private static void verifyRegistered(AdvancementTab tab) {
        verify(tab).registerAdvancements(any(RootAdvancement.class), anySet());
    }

    private void verifyNoProjection(Fixture f, String key) {
        project(f, key);
        verifyNoInteractions(roots.constructed().getFirst(), children.constructed().getFirst());
    }

    private static void registerInvalid(Fixture f) {
        var invalid = new ProjectionService.Node("bad", null, "bad", List.of(),
                Material.CLOCK, "TASK", Float.NaN, 0);
        f.service().registerTree(f.owner(), "test", new ItemStack(Material.CLOCK), List.of(invalid));
    }

    private static void registerThenMutateInputs(Fixture f) {
        var sourceIcon = icon(1);
        var sourceDefinitions = new ArrayList<>(nodes("old"));
        f.service().registerTree(f.owner(), "test", sourceIcon, sourceDefinitions);
        sourceDefinitions.clear();
        when(sourceIcon.getAmount()).thenReturn(64);
    }
}
