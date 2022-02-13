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

import io.github.apace100.origins.component.PlayerOriginComponent;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import valoeghese.uniqueorigins.Uniqueorigins;
import valoeghese.uniqueorigins.Uniqueorigins.UniquifierProperties;

@Mixin(value = PlayerOriginComponent.class, remap = false)
public abstract class MixinPlayerOriginComponent {
	@Shadow
	public abstract Origin getOrigin(OriginLayer layer);

	@Shadow
	private Player player;

	@Inject(at = @At("HEAD"), method = "setOrigin")
	private void updateOriginState(OriginLayer layer, Origin origin, CallbackInfo info) {
		if (!this.player.level.isClientSide()) {
			Origin oldOrigin = getOrigin(layer); // duplicated code to check if origin actually changed

			if(oldOrigin != origin) {
				// now our actual code starts
				// we edit our persistent state information to update who has what
				@SuppressWarnings("ConstantConditions")
				UniquifierProperties properties = Uniqueorigins.getOriginData(this.player.getServer());

				if (oldOrigin != null)
					properties.decrementOriginCount(layer.getIdentifier(), oldOrigin.getIdentifier());

				properties.incrementOriginCount(layer.getIdentifier(), origin.getIdentifier());
			}
		}
	}
}
