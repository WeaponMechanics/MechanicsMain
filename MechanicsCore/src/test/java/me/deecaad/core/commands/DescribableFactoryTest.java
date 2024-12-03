package me.deecaad.core.commands;

import be.seeseemelk.mockbukkit.MockBukkit;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import me.deecaad.core.MechanicsCore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class DescribableFactoryTest {

    @BeforeEach
    public void setUp() {
        MockBukkit.mock();
        MockBukkit.load(MechanicsCore.class);
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void test_makeArgumentDescribable() {
        StringArgument original = new StringArgument("test");
        String description = "test description";
        StringArgument result = DescribableFactory.makeArgumentDescribable(original, description);

        // The result should be a Describable object
        assertInstanceOf(Describable.class, result);
        assertEquals(description, ((Describable) result).getDescription());

        // The new instance should have the same fields as the original instance
        assertEquals(original.getNodeName(), result.getNodeName());
    }

    @Test
    public void test_makeComplexArgumentDescribable() {
        EntitySelectorArgument.ManyPlayers original = new EntitySelectorArgument.ManyPlayers("test", true);
        String description = "test description";
        EntitySelectorArgument.ManyPlayers result = DescribableFactory.makeArgumentDescribable(original, description);

        // The result should be a Describable object
        assertInstanceOf(Describable.class, result);
        assertEquals(description, ((Describable) result).getDescription());

        // The new instance should have the same fields as the original instance
        assertEquals(original.getNodeName(), result.getNodeName());
    }

    @Test
    public void test_makeArgumentDescribableWithSuggestions() {
        IntegerArgument original = new IntegerArgument("test");
        original.includeSuggestions(ArgumentSuggestions.strings("1", "2", "3"));
        original.setOptional(true);
        String description = "test description";
        IntegerArgument result = DescribableFactory.makeArgumentDescribable(original, description);

        // The result should be a Describable object
        assertInstanceOf(Describable.class, result);
        assertEquals(description, ((Describable) result).getDescription());

        // The new instance should have the same fields as the original instance
        assertEquals(original.getNodeName(), result.getNodeName());
        assertEquals(original.getIncludedSuggestions(), result.getIncludedSuggestions());
        assertEquals(original.isOptional(), result.isOptional());
    }
}
