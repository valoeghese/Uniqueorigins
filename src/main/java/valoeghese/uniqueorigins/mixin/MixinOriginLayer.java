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

import java.util.List;
import java.util.stream.Collectors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.apace100.origins.origin.OriginLayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import valoeghese.uniqueorigins.Uniqueorigins;
import valoeghese.uniqueorigins.Uniqueorigins.UniquifierProperties;

@Mixin(value = OriginLayer.class, remap = false)
public class MixinOriginLayer {
	@Inject(at= @At("RETURN"), method = "getRandomOrigin", cancellable = true)
	private void makeOriginsUniqueRandom(PlayerEntity entity, CallbackInfoReturnable<List<Identifier>> info) {
		UniquifierProperties properties = Uniqueorigins.getOriginData(entity);
		info.setReturnValue(info.getReturnValue().stream()
				.filter(id -> properties.getOriginCount(id) < properties.getMaxOriginCount())
				.collect(Collectors.toList()));
	}

	@Inject(at= @At("RETURN"), method = "getOrigins", cancellable = true)
	private void makeOriginsUniqueNormal(PlayerEntity entity, CallbackInfoReturnable<List<Identifier>> info) {
		UniquifierProperties properties = Uniqueorigins.getOriginData(entity);
		info.setReturnValue(info.getReturnValue().stream()
				.filter(id -> properties.getOriginCount(id) < properties.getMaxOriginCount())
				.collect(Collectors.toList()));
	}
}
