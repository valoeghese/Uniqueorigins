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

import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Collectors;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;
import valoeghese.uniqueorigins.Uniqueorigins.UniquifierProperties;

public class UniqueState extends PersistentState implements UniquifierProperties {
	public UniqueState() {
		super("uniqueorigindata");
	}

	private CompoundTag impl = new CompoundTag();

	@Override
	public void fromTag(CompoundTag tag) {
		this.impl = tag;
	}

	@Override
	public CompoundTag toTag(CompoundTag tag) {
		return this.impl;
	}

	public List<Identifier> filter(Identifier layer, List<Identifier> origins){
		OptionalInt min = origins.stream().mapToInt(origin -> getOriginCount(layer, origin)).min();
		return origins.stream().filter(origin -> getOriginCount(layer, origin) <= min.orElse(0)).collect(Collectors.toList());
	}

	private String getLayerKey(String layer){ return "Layer:" + layer; } // IDs can't have capital letters so we're safe

	public int getOriginCount(Identifier layer, Identifier origin) {
		return getOriginCount(layer.toString(), origin.toString());
	}

	private int getOriginCount(String layer, String origin) {
		if (layer.equals(DEFAULT_LAYER))
			return impl.contains(origin, INT) ? impl.getInt(origin) : 0;
		String layerKey = getLayerKey(layer);
		if (!impl.contains(layerKey, COMPOUND))
			return 0;
		CompoundTag layerTag = ((CompoundTag)impl.get(layerKey));
		return layerTag != null && layerTag.contains(origin, INT) ? layerTag.getInt(origin) : 0;
	}

	@Override
	public void incrementOriginCount(Identifier layer, Identifier origin) {
		updateOriginCount(layer, origin, 1);
	}

	@Override
	public void decrementOriginCount(Identifier layer, Identifier origin) {
		updateOriginCount(layer, origin, -1);
	}

	private void updateOriginCount(Identifier layer, Identifier origin, int increment) {
		if (!origin.toString().equals("origins:empty")) {
			String o = origin.toString(); // get the string representation for the c.t. nbt
			String l = layer.toString(); // same thing for layer whatever you get the idea
			int count = getOriginCount(o, l); // get the old count
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
		this.markDirty();
	}

	@Override
	public String toString() {
		return "UniqueState{" + this.impl.toString() + "}";
	}

	private static final byte INT = 3;
	private static final byte COMPOUND = 10;
	private static final String DEFAULT_LAYER = "origins:origin";
}
