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

package valoeghese.uniqueorigins.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.networking.ModPackets;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.registry.ModComponents;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import valoeghese.uniqueorigins.Uniqueorigins.HackedOriginLayer;

/**
 * Mixin to fabric api networking
 * @reason because I need to f*ck with the packets being sent with reference to the data of the player, and origins does all this in the mixin rather than in a separate logic class.
 */
@Mixin(value = ServerPlayNetworking.class, remap = false)
public class MixinServerPlayerNetworking {
	@Inject(at = @At("HEAD"), method = "send", cancellable = true)
	private static void overwriteDataOnFirstLogin(ServerPlayerEntity player, Identifier channelName, PacketByteBuf buf, CallbackInfo info) {
		if (channelName.equals(ModPackets.LAYER_LIST)) {
			OriginComponent component = ModComponents.ORIGIN.get(player);
			PacketByteBuf originLayerData = new PacketByteBuf(Unpooled.buffer());

			originLayerData.writeInt(OriginLayers.size());
			OriginLayers.getLayers().forEach((layer) -> {
				// If should overwrite. We do not need the first check as origins already has set it to empty by now
				if (/*!component.hasOrigin(layer) || */component.getOrigin(layer).equals(Origin.EMPTY)) {
					((HackedOriginLayer) layer).writeFirstLogin(player, originLayerData);
				} else { // otherwise don't f*ck up the client on reconnect
					layer.write(originLayerData);
				}

				// duplicated code does not need to be run as origins has already done this check
				/*if(layer.isEnabled()) {
					if(!component.hasOrigin(layer)) {
						component.setOrigin(layer, Origin.EMPTY);
					}
				}*/
			});

			player.networkHandler.sendPacket(ServerPlayNetworking.createS2CPacket(channelName, originLayerData));
			info.cancel();
		}
	}
}
