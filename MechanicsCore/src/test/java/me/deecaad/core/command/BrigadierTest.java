package me.deecaad.core.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.junit.jupiter.api.Test;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BrigadierTest {

    @Test
    void test_brigadier() throws CommandSyntaxException {
        CommandDispatcher<TestCommandSender> dispatcher = new CommandDispatcher<>();
        LiteralArgumentBuilder<TestCommandSender> command = literal("test");
        command.executes(this::run);

        dispatcher.register(command);

        assertEquals(1, dispatcher.execute("test", new TestCommandSender()));
    }

    private int run(CommandContext<TestCommandSender> context) throws CommandSyntaxException {
        context.getSource().sendMessage("Hello World! " + context);
        return 1;
    }

    private static class TestCommandSender {

        public void sendMessage(String msg) {
            System.out.println(msg);
        }
    }
}
