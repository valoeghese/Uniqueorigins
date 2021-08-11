package valoeghese.uniqueorigins;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import io.github.apace100.origins.command.LayerArgument;
import io.github.apace100.origins.origin.OriginLayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

// Inspired by Apace's command registries

public class UniqueoriginsCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher){
        dispatcher.register(
            literal("uniqueorigins").requires(cs -> cs.hasPermissionLevel(2))
                .then(literal("layer")
                    .then(argument("layer", LayerArgument.layer())
                        .then(literal("filter")
                            .then(literal("get")
                                .executes(
                                    command -> Uniqueorigins.getOriginData(command.getSource().getMinecraftServer()).getFilter(
                                        command.getArgument("layer", OriginLayer.class).getIdentifier(),
                                        command.getSource()::sendFeedback, command.getSource()::sendError
                                    )
                                )
                            )
                            .then(literal("set")
                                .then(argument("active", BoolArgumentType.bool())
                                    .executes(
                                        command -> Uniqueorigins.getOriginData(command.getSource().getMinecraftServer()).setFilter(
                                            command.getArgument("layer", OriginLayer.class).getIdentifier(),
                                            command.getArgument("active", Boolean.class),
                                            command.getSource()::sendFeedback, command.getSource()::sendError
                                        )
                                    )
                                )
                            )
                            .then(literal("toggle")
                                .executes(
                                    command -> Uniqueorigins.getOriginData(command.getSource().getMinecraftServer()).toggleFilter(
                                        command.getArgument("layer", OriginLayer.class).getIdentifier(),
                                        command.getSource()::sendFeedback, command.getSource()::sendError
                                    )
                                )
                            )
                        )
                    )
                )
        );
    }
}
