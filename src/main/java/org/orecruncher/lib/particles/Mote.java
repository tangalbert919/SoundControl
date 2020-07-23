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

package org.orecruncher.lib.particles;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.ILightReader;
import net.minecraft.world.LightType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.orecruncher.lib.GameUtils;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public abstract class Mote implements IParticleMote {

    protected final IBlockReader world;
    protected final ILightReader lighting;

    protected boolean isAlive = true;
    protected double posX;
    protected double posY;
    protected double posZ;
    protected final BlockPos.Mutable position = new BlockPos.Mutable();

    protected int slX16;
    protected int blX16;

    protected float red;
    protected float green;
    protected float blue;
    protected float alpha;

    public Mote(@Nonnull final IBlockReader world, final double x, final double y, final double z) {
        this.world = world;
        this.lighting = GameUtils.getWorld();
        setPosition(x, y, z);
        configureColor();
    }

    public void setPosition(final double posX, final double posY, final double posZ) {
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.position.set(posX, posY, posZ);
    }

    public void configureColor() {
        this.red = this.green = this.blue = this.alpha = 1F;
    }

    @Override
    public boolean isAlive() {
        return this.isAlive;
    }

    @Override
    public void kill() {
        this.isAlive = false;
    }

    @Override
    public boolean tick() {
        if (isAlive()) {

            update();

            // The update() may have killed the mote
            if (isAlive()) {
                setPosition(this.posX, this.posY, this.posZ);
                updateBrightness();
            }
        }
        return isAlive();
    }

    protected void update() {

    }

    public void updateBrightness() {
        final int combinedLight = getBrightnessForRender(0);
        this.slX16 = combinedLight >> 16 & 65535;
        this.blX16 = combinedLight & 65535;
    }

    protected float renderX(final float partialTicks) {
        return (float) (this.posX);
    }

    protected float renderY(final float partialTicks) {
        return (float) (this.posY);
    }

    protected float renderZ(final float partialTicks) {
        return (float) (this.posZ);
    }

    protected void drawVertex(final IVertexBuilder buffer, final double x, final double y, final double z,
                              final double u, final double v) {
        buffer
                .vertex(x, y, z)
                .uv((float) u, (float) v)
                .color(this.red, this.green, this.blue, this.alpha)
                .uv2(this.slX16, this.blX16)
                .endVertex();
    }

    @Override
    public abstract void render(final IVertexBuilder buffer, final ActiveRenderInfo info, float partialTicks);

    public int getBrightnessForRender(final float partialTicks) {
        int i = lighting.getBrightness(LightType.SKY, this.position);
        int j = lighting.getBrightness(LightType.BLOCK, this.position);
        return i << 20 | j << 4;
    }

}