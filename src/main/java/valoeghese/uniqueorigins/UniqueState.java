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

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.saveddata.SavedData;
import valoeghese.uniqueorigins.Uniqueorigins.UniquifierProperties;

import java.util.List;
import java.util.OptionalInt;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class UniqueState extends SavedData implements UniquifierProperties {
	public UniqueState(CompoundTag tag) {
		this.from(tag);
	}

	private CompoundTag impl = new CompoundTag();

	public void from(CompoundTag tag) {
		this.impl = tag;
	}

	@Override
	public CompoundTag save(CompoundTag tag) {
		return this.impl;
	}

	@Override
	public int getFilter(ResourceLocation layer, BiConsumer<Component, Boolean> feedback, Consumer<Component> error){
		AtomicBoolean layerTagPresent = new AtomicBoolean(true);
		boolean filtered = isFiltered(layer, () -> layerTagPresent.set(false)); // this sounds overkill
		if (!layerTagPresent.get())
			error.accept(new TranslatableComponent("The layer %s is not registered", layer));
		else
			feedback.accept(new TranslatableComponent(
			"Layer %s is" + (filtered ? " " : " not ") + "filtered",
			layer), false);
		return filtered ? 1 : 0;
	}

	@Override
	public int setFilter(ResourceLocation layer, boolean value, BiConsumer<Component, Boolean> feedback, Consumer<Component> error){
		CompoundTag layerTag = getLayerTag(layer);
		if (layerTag == null)
			error.accept(new TranslatableComponent("The layer %s is not registered", layer));
		else {
			feedback.accept(new TranslatableComponent(
				"The filtering of layer %s was changed from %s to %s",
				layer, !layerTag.contains("Filtered") || layerTag.getBoolean("Filtered"), value), false);
			layerTag.putBoolean("Filtered", value);
			this.setDirty();
		}
		return value ? 1 : 0;
	}

	@Override
	public int toggleFilter(ResourceLocation layer, BiConsumer<Component, Boolean> feedback, Consumer<Component> error){
		return setFilter(layer, !isFiltered(layer), feedback, error);
	}

	private boolean isFiltered(ResourceLocation layer){
		return isFiltered(layer, null);
	}

	private boolean isFiltered(ResourceLocation layer, Runnable onNoTag){
		CompoundTag layerTag = getLayerTag(layer);
		if (layerTag == null && onNoTag != null)
			onNoTag.run();
		return layerTag == null
			|| (!layerTag.contains("Filtered")
				|| layerTag.getBoolean("Filtered"));
	}

	public List<ResourceLocation> filter(ResourceLocation layer, List<ResourceLocation> conditionedOrigins, List<ResourceLocation> layerOrigins){
		if (!isFiltered(layer))
			return conditionedOrigins;
		OptionalInt min = layerOrigins.stream().mapToInt(origin -> getOriginCount(layer, origin)).min();
		return conditionedOrigins.stream().filter(origin -> getOriginCount(layer, origin) <= min.orElse(0)).collect(Collectors.toList());
	}

	private CompoundTag getLayerTag(ResourceLocation layer){
		return getLayerTag(layer.toString());
	}

	private CompoundTag getLayerTag(String layer){
		if (layer.equals(DEFAULT_LAYER))
			return impl;
		if (!impl.contains(getLayerKey(layer), COMPOUND))
			return null;
		return (CompoundTag)impl.get(getLayerKey(layer));
	}

	private String getLayerKey(String layer){ return "Layer:" + layer; } // IDs can't have capital letters so we're safe

	private int getOriginCount(ResourceLocation layer, ResourceLocation origin) {
		return getOriginCount(layer.toString(), origin.toString());
	}

	private int getOriginCount(String layer, String origin){
		return Math.max(getStoredOriginCount(layer, origin), 0); // prevents possibles issues with false numbers stored, I guess
	}

	private int getStoredOriginCount(String layer, String origin) {
		if (layer.equals(DEFAULT_LAYER))
			return impl.contains(origin, INT) ? impl.getInt(origin) : 0;
		CompoundTag layerTag = getLayerTag(layer);
		return layerTag != null && layerTag.contains(origin, INT) ? layerTag.getInt(origin) : 0;
	}

	@Override
	public void incrementOriginCount(ResourceLocation layer, ResourceLocation origin) {
		if (!origin.toString().equals("origins:empty"))
			Uniqueorigins.LOGGER.info("Adding 1 to the unique origin count of " + origin + " on layer " + layer);
		updateOriginCount(layer, origin, 1);
	}

	@Override
	public void decrementOriginCount(ResourceLocation layer, ResourceLocation origin) {
		if (!origin.toString().equals("origins:empty"))
			Uniqueorigins.LOGGER.info("Removing 1 from the unique origin count of " + origin + " on layer " + layer);
		updateOriginCount(layer, origin, -1);
	}

	private void updateOriginCount(ResourceLocation layer, ResourceLocation origin, int increment) {
		if (!origin.toString().equals("origins:empty")) {
			String o = origin.toString(); // get the string representation for the c.t. nbt
			String l = layer.toString(); // same thing for layer whatever you get the idea
			int count = getOriginCount(l, o); // get the old count
			// Set the new count
			if (l.equals(DEFAULT_LAYER))
				impl.putInt(o, count + increment); // backward compatibility, treating default layer specially
			else {
				String lk = getLayerKey(l);
				if (!impl.contains(lk) || !(impl.get(lk) instanceof CompoundTag))
					impl.put(lk, new CompoundTag());
				CompoundTag lt = (CompoundTag)impl.get(lk);
				if (lt != null) // IntelliJ doesn't seem to think it's impossible for it to be null so
					lt.putInt(o, count + increment);
			}
		}
		this.setDirty();
	}

	@Override
	public String toString() {
		return "UniqueState{" + this.impl.toString() + "}";
	}

	private static final byte INT = 3;
	private static final byte COMPOUND = 10;
	private static final String DEFAULT_LAYER = "origins:origin";
}
