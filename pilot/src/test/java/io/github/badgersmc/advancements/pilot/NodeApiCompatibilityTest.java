package io.github.badgersmc.advancements.pilot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.ArrayList;
import java.lang.reflect.Constructor;
import java.util.List;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class NodeApiCompatibilityTest {
    @Test
    void retainsRecordComponentsAndConstructorDescriptors() throws Exception {
        Class<ProjectionService.Node> type = ProjectionService.Node.class;
        assertTrue(type.isRecord());
        assertEquals(List.of("key", "parentKey", "title", "description", "icon",
                "customModelData", "itemModel", "frame", "x", "y"),
                Arrays.stream(type.getRecordComponents()).map(c -> c.getName()).toList());
        assertEquals(4, type.getConstructors().length);
        assertEquals(8, constructor(String.class, float.class, float.class).getParameterCount());
        assertEquals(9, constructor(Integer.class, String.class, float.class, float.class).getParameterCount());
        assertEquals(9, constructor(String.class, String.class, float.class, float.class).getParameterCount());
        assertEquals(10, constructor(Integer.class, String.class, String.class,
                float.class, float.class).getParameterCount());
    }

    private static Constructor<ProjectionService.Node> constructor(Class<?>... tail) throws Exception {
        var parameters = new ArrayList<Class<?>>(List.of(String.class, String.class,
                String.class, List.class, Material.class));
        parameters.addAll(List.of(tail));
        return ProjectionService.Node.class.getConstructor(parameters.toArray(Class<?>[]::new));
    }

    @Test
    void legacyOverloadsRetainMetadataDefaults() {
        var plain = new ProjectionService.Node("plain", null, "Plain", List.of(), null, "TASK", 1, 2);
        var numeric = new ProjectionService.Node("numeric", null, "Numeric", List.of(),
                Material.CLOCK, Integer.valueOf(42), "GOAL", 1, 2);
        var model = new ProjectionService.Node("model", null, "Model", List.of(),
                Material.CLOCK, "enthusia:diary", "CHALLENGE", 1, 2);
        assertEquals(Material.CLOCK, plain.icon());
        assertNull(plain.customModelData());
        assertNull(plain.itemModel());
        assertEquals(42, numeric.customModelData());
        assertNull(numeric.itemModel());
        assertNull(model.customModelData());
        assertEquals("enthusia:diary", model.itemModel());
    }
}
