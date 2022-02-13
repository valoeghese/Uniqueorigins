package valoeghese.uniqueorigins;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import io.github.apace100.origins.command.LayerArgument;
import io.github.apace100.origins.origin.OriginLayer;
import net.minecraft.commands.CommandSourceStack;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

// Inspired by Apace's command registries

public class UniqueoriginsCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(
            literal("uniqueorigins").requires(cs -> cs.hasPermission(2))
                .then(literal("layer")
                    .then(argument("layer", LayerArgument.layer())
                        .then(literal("filter")
                            .then(literal("get")
                                .executes(
                                    command -> Uniqueorigins.getOriginData(command.getSource().getServer()).getFilter(
                                        command.getArgument("layer", OriginLayer.class).getIdentifier(),
                                        command.getSource()::sendSuccess, command.getSource()::sendFailure
                                    )
                                )
                            )
                            .then(literal("set")
                                .then(argument("active", BoolArgumentType.bool())
                                    .executes(
                                        command -> Uniqueorigins.getOriginData(command.getSource().getServer()).setFilter(
                                            command.getArgument("layer", OriginLayer.class).getIdentifier(),
                                            command.getArgument("active", Boolean.class),
                                            command.getSource()::sendSuccess, command.getSource()::sendFailure
                                        )
                                    )
                                )
                            )
                            .then(literal("toggle")
                                .executes(
                                    command -> Uniqueorigins.getOriginData(command.getSource().getServer()).toggleFilter(
                                        command.getArgument("layer", OriginLayer.class).getIdentifier(),
                                        command.getSource()::sendSuccess, command.getSource()::sendFailure
                                    )
                                )
                            )
                        )
                    )
                )
        );
    }
}
