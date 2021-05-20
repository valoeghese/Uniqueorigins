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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class Uniqueorigins implements ModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("Uniqueorigins");
	private static final String ID = "uniqueorigindata";

	@Override
	public void onInitialize() {
		LOGGER.info("Making sure your origins will be more... unique~");
	}

	public static UniquifierProperties getOriginData(MinecraftServer server) {
		return server.getWorld(World.OVERWORLD).getPersistentStateManager().getOrCreate(UniqueState::new, ID);
	}

	public interface HackedOriginLayer {
		void writeFirstLogin(PlayerEntity entity, PacketByteBuf buffer);
	}

	public interface UniquifierProperties {
		/**
		 * @return the lowest count of any origin.
		 */
		int getMinOriginCount();
		/**
		 * @return the highest count of any origin.
		 */
		int getMaxOriginCount();
		/**
		 * Retrieves the count of a given origin.
		 * @param identifier the given origin
		 * @return the number of players with that origin
		 */
		int getOriginCount(Identifier identifier);
		/**
		 * Increments the count for the specified origin
		 * @param origin the origin to increment the count for
		 */
		void addOriginCount(Identifier origin);
		/**
		 * Decrement the count for the specified origin
		 * @param origin the origin to decrement the count for
		 */
		void removeOriginCount(Identifier origin);
	}
}
