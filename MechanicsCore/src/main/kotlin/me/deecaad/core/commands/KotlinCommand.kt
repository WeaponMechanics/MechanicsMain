package me.deecaad.core.commands

import me.deecaad.core.commands.arguments.CommandArgumentType
import org.bukkit.command.CommandSender
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.permissions.Permission
import java.util.function.BiConsumer
import java.util.function.Function
import java.util.function.Predicate

/**
 * This [scope marker](https://kotlinlang.org/docs/type-safe-builders.html#scope-control-dslmarker)
 * prevents people from using "improper nested types". For example, you cannot
 * put an argument in an argument, or a subcommand in an argument.
 */
@DslMarker
annotation class CommandDsl

/**
 * The KotlinCommand wraps a [CommandBuilder] and lets developers build a
 * command using Kotlin's lambda functions. Not a Kotlin developer? Just
 * use [CommandBuilder] instead.
 */
@CommandDsl
class KotlinCommand(val label: String) {

    var permission: Permission? = null
    var requirements: Predicate<CommandSender>? = null
    var aliases: List<String> = emptyList()
    var arguments: MutableList<Argument<Any>> = ArrayList()
    var subcommands: MutableList<CommandBuilder> = ArrayList()
    var executor: CommandExecutor<out CommandSender>? = null
    var description: String = "No description provided"

    fun aliases(vararg aliases: String) {
        this.aliases = aliases.toList()
    }

    fun permission(permission: String) {
        this.permission = Permission(permission)
    }

    fun description(description: String) {
        this.description = description
    }

    fun <T> argument(label: String, type: CommandArgumentType<T>, init: KotlinArgument<T>.() -> Unit) {
        val argument = KotlinArgument(label, type)
        argument.init()

        // After calling argument.init(), the code block inside the {} will
        // be executed. So now we can copy the values into the command argument.
        val temp = if (argument.isRequired) Argument(label, type) else Argument(label, type, argument.default)
        temp.withPermission(argument.permission)
        temp.description = argument.description
        temp.isReplaceSuggestions = argument.isReplaceSuggestions
        temp.suggestions = argument.suggestions
        temp.requirements = argument.requirements
        arguments.add(temp as Argument<Any>)
    }

    fun subcommand(label: String, init: KotlinCommand.() -> Unit) {
        subcommands.add(command(label, init))
    }

    fun executePlayer(init: BiConsumer<Player, Array<Any>>) {
        executor = CommandExecutor.player(init)
    }

    fun executeEntity(init: BiConsumer<Entity, Array<Any>>) {
        executor = CommandExecutor.entity(init)
    }

    fun executeAny(init: BiConsumer<CommandSender, Array<Any>>) {
        executor = CommandExecutor.any(init)
    }

    fun <T : CommandSender> execute(type: Class<T>, init: BiConsumer<T, Array<Any>>) {
        executor = object: CommandExecutor<T>(type) {
            override fun execute(sender: T, args: Array<Any>) {
                init.accept(sender, args)
            }
        }
    }
}

/**
 * The KotlinArgument wraps an [Argument] and lets developers build an
 * argument using kotlin's lambda functions.
 */
@CommandDsl
class KotlinArgument<T>(val label: String, type: CommandArgumentType<T>) {

    var isRequired: Boolean = false
    var default: T? = null
    var permission: Permission? = null
    var description: String = "No description provided"
    internal var isReplaceSuggestions: Boolean = false
    internal var suggestions: Function<CommandData, Array<Tooltip>>? = null
    internal var requirements: Predicate<CommandSender>? = null

    fun append(suggestions: Function<CommandData, Array<Tooltip>>) {
        this.suggestions = suggestions
    }

    fun replace(suggestions: Function<CommandData, Array<Tooltip>>) {
        this.suggestions = suggestions
        this.isReplaceSuggestions = true
    }

    fun requirements(requirements: Predicate<CommandSender>) {
        this.requirements = requirements
    }
}

fun command(label: String, init: KotlinCommand.() -> Unit): CommandBuilder {
    val builder = KotlinCommand(label)
    builder.init()

    // After calling argument.init(), the code block inside the {} will
    // be executed. So now we can copy the values into the command argument.
    val temp = CommandBuilder(label)
    temp.withPermission(builder.permission)
    temp.requirements = builder.requirements
    temp.aliases = builder.aliases
    temp.args = builder.arguments
    temp.subcommands = builder.subcommands
    temp.executor = builder.executor
    temp.description = builder.description
    return temp
}