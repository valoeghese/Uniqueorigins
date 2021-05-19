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

import java.util.function.IntPredicate;

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

	@Override
	public int getOriginCount(Identifier identifier) {
		return getOriginCount(identifier.toString());
	}

	private int getOriginCount(String str) {
		if (this.impl.contains(str, INT)) {
			return this.impl.getInt(str);
		} else {
			return 0;
		}
	}

	@Override
	public void addOriginCount(Identifier origin) {
		updateOriginCount(origin, "minCount", getMinOriginCount(), "maxCount", i -> i > getMaxOriginCount(), 1);
	}

	@Override
	public void removeOriginCount(Identifier origin) {
		updateOriginCount(origin, "maxCount", getMaxOriginCount(), "minCount", i -> i < getMinOriginCount(), -1);
	}

	private void updateOriginCount(Identifier origin, String storageComputed, int borderlineComputed, String storageSimple, IntPredicate borderlineSimple, int increment) {
		String str = origin.toString(); // get the string representation for the c.t. nbt
		int count = getOriginCount(origin); // get the old count
		boolean recompute = count == borderlineComputed; // if it was the border, we need to recompute the computed one
		this.impl.putInt(str, count + increment); // set the new count

		if (recompute) {
			boolean updateBorder = true;

			for (String k : this.impl.getKeys()) {
				if (getOriginCount(k) == count) { // if the old count (guaranteeed to be old min) still exists
					updateBorder = false;
					break;
				}
			}

			if (updateBorder) { // if the old count doesn't exist, count + increment is guaranteed to exist because we only use values +1 and -1 and in very sane places. Put it there.
				this.impl.putInt(storageComputed, count + increment);
			}
		}

		if (borderlineSimple.test(count)) {
			this.impl.putInt(storageSimple, count);
		}
	}
	@Override
	public int getMaxOriginCount() {
		if (this.impl.contains("maxCount", INT)) {
			return this.impl.getInt("maxCount");
		} else {
			this.impl.putInt("maxCount", 0);
			return 0;
		}
	}

	@Override
	public int getMinOriginCount() {
		if (this.impl.contains("minCount", INT)) {
			return this.impl.getInt("minCount");
		} else {
			this.impl.putInt("minCount", 0);
			return 0;
		}
	}

	@Override
	public String toString() {
		return "UniqueState{" + this.impl.toString() + "}";
	}

	private static final byte INT = 3;
}
