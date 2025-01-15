package me.deecaad.core.commands;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class DescribableFactoryTest {

    @Test
    public void test_makeArgumentDescribable() {
        DummyArgument original = new DummyArgument("test");
        String description = "test description";
        DummyArgument result = DescribableFactory.makeArgumentDescribable(original, description);

        // The result should be a Describable object
        assertInstanceOf(Describable.class, result);
        assertEquals(description, ((Describable) result).getDescription());

        // The new instance should have the same fields as the original instance
        assertEquals(original.getNodeName(), result.getNodeName());
    }

    public static class DummyArgument {

        private final String nodeName;

        public DummyArgument(String nodeName) {
            this.nodeName = nodeName;
        }

        public String getNodeName() {
            return nodeName;
        }
    }
}
