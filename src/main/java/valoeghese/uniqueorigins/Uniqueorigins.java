/*
 * Uniqueorigins
 * Copyright (C) 2021 Valoeghese
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package valoeghese.uniqueorigins;

import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.text.Text;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Uniqueorigins implements ModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("Uniqueorigins");
	private static final String ID = "uniqueorigindata";

	@Override
	public void onInitialize() {
		LOGGER.info("Making sure your origins will be more... unique~");
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
			UniqueoriginsCommand.register(dispatcher);
		});
	}

	public static UniquifierProperties getOriginData(MinecraftServer server) {
		return server.getLevel(Level.OVERWORLD).getDataStorage().get(UniqueState::new, ID);
	}

	public interface HackedOriginLayer {
		void writeFirstLogin(Player entity, FriendlyByteBuf buffer);
	}

	public interface UniquifierProperties {
		/**
		 * Tests whether the filter is active for a layer
		 * @param layer the given layer
		 * @param feedback a reference to the chat feedback function
		 * @param error a reference to the chat error feedback function
		 * @return whether the filter is on (1) or off (0)
		 */
		int getFilter(ResourceLocation layer, BiConsumer<Component, Boolean> feedback, Consumer<Component> error);
		/**
		 * Activates/deactivates the filter for a layer
		 * @param layer the given layer
		 * @param value whether that layer should now be active
		 * @param feedback a reference to the chat feedback function
		 * @param error a reference to the chat error feedback function
		 * @return the input value
		 */
		int setFilter(ResourceLocation layer, boolean value, BiConsumer<Component, Boolean> feedback, Consumer<Component> error);
		/**
		 * Toggles the filter for a layer
		 * @param layer the given layer
		 * @param feedback a reference to the chat feedback function
		 * @param error a reference to the chat error feedback function
		 * @return the input value
		 */
		int toggleFilter(ResourceLocation layer, BiConsumer<Component, Boolean> feedback, Consumer<Component> error);
		/**
		 * Filters out saturated origins from a list
		 * @param layer the given layer
		 * @param conditionedOrigins the list of conditioned origins to filter
		 * @param layerOrigins the list of origins on the layer
		 * @return the filtered list of origins with saturated origins removed
		 */
		List<ResourceLocation> filter(ResourceLocation layer, List<ResourceLocation> conditionedOrigins, List<ResourceLocation> layerOrigins);
		/**
		 * Increments the count for the specified origin
		 * @param layer the layer to increment the count for
		 * @param origin the origin to increment the count for
		 */
		void incrementOriginCount(ResourceLocation layer, ResourceLocation origin);
		/**
		 * Decrement the count for the specified origin
		 * @param layer the layer to decrement the count for
		 * @param origin the origin to decrement the count for
		 */
		void decrementOriginCount(ResourceLocation layer, ResourceLocation origin);
	}
}
