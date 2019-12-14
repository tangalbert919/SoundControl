/*
 * Dynamic Surroundings: Sound Control
 * Copyright (C) 2019  OreCruncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package org.orecruncher.sndctrl.audio.acoustic;

import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.sndctrl.SoundControl;
import org.orecruncher.sndctrl.audio.SoundRegistry;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public enum AcousticLibrary {

    INSTANCE;

    private final Map<String, IAcoustic> compiled = new Object2ObjectAVLTreeMap<>(String.CASE_INSENSITIVE_ORDER);

    private AcousticLibrary() {

    }

    /**
     * Adds/replaces a known acoustic in the library.
     *
     * @param name     Name of the acoustic
     * @param acoustic The acoustic mapped to that name
     */
    public void addAcoustic(@Nonnull final String name, @Nonnull final IAcoustic acoustic) {
        this.compiled.put(name, acoustic);
    }

    @Nonnull
    public IAcoustic resolve(@Nonnull final String acousticName) {
        IAcoustic result = this.compiled.get(acousticName);
        if (result == null) {
            final IAcoustic[] parsed = Arrays.stream(acousticName.split(",")).map(fragment -> {
                // See if we have an acoustic for this fragment
                final IAcoustic a = generateAcoustic(fragment);
                if (a == null)
                    SoundControl.LOGGER.warn("Acoustic '%s' not found!", fragment);
                return a;
            }).filter(Objects::nonNull).toArray(IAcoustic[]::new);

            if (parsed.length == 0) {
                result = NullAcoustic.INSTANCE;
            } else if (parsed.length == 1) {
                result = parsed[0];
            } else {
                final SimultaneousAcoustic s = new SimultaneousAcoustic(acousticName);
                for (final IAcoustic t : parsed)
                    s.add(t);
                result = s;
            }
            this.compiled.put(acousticName, result);
        }

        return result;
    }

    private IAcoustic generateAcoustic(@Nonnull final String name) {
        IAcoustic a = this.compiled.get(name);
        if (a == null) {
            // Nope. Doesn't exist yet. It could be a sound name based on location.
            final ResourceLocation loc = new ResourceLocation(name);
            final Optional<SoundEvent> evt = SoundRegistry.getSound(loc);
            if (evt.isPresent())
                a = generateAcoustic(evt.get());
        }

        return a;
    }

    @Nonnull
    private IAcoustic generateAcoustic(@Nonnull final SoundEvent evt) {
        IAcoustic result = this.compiled.get(evt.getName().toString());
        if (result == null) {
            result = new SimpleAcoustic(evt);
            this.compiled.put(result.getName(), result);
        }
        return result;
    }

}