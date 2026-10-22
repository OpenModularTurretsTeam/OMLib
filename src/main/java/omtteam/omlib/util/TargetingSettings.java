package omtteam.omlib.util;

import net.minecraft.nbt.NBTTagCompound;

import java.util.Objects;

/**
 * Created by Keridos on 17/08/17.
 * The 3.x targeting settings of a turret base, now a facade over the core
 * {@link omtteam.omlib.core.machine.TargetingSettings}: it reads and writes the 3.x NBT format and keeps the 3.x
 * (chainable) setters. A facade made with {@link #of} is a live view of the core settings it wraps.
 */
public class TargetingSettings {
    private final omtteam.omlib.core.machine.TargetingSettings core;

    private TargetingSettings(omtteam.omlib.core.machine.TargetingSettings core) {
        this.core = core;
    }

    public TargetingSettings(boolean targetPlayers, boolean targetMobs, boolean targetPassive, int range,
                             int maxRange) {
        this(new omtteam.omlib.core.machine.TargetingSettings(targetPlayers, targetMobs, targetPassive, range,
                maxRange));
    }

    /** A live view of {@code core}: changes go to the core settings. */
    public static TargetingSettings of(omtteam.omlib.core.machine.TargetingSettings core) {
        return new TargetingSettings(core);
    }

    /** The core settings behind this facade. */
    public omtteam.omlib.core.machine.TargetingSettings core() {
        return core;
    }

    /** Reads the 4.0 / 3.2 compound {@code targetingSettings}, or the 3.1 keys of the base itself. */
    public static TargetingSettings readFromNBT(NBTTagCompound nbtTagCompound) {
        if (nbtTagCompound.hasKey("targetingSettings")) {
            NBTTagCompound nbt = nbtTagCompound.getCompoundTag("targetingSettings");
            return new TargetingSettings(nbt.getBoolean("targetPlayers"), nbt.getBoolean("targetMobs"),
                    nbt.getBoolean("targetPassive"), nbt.getInteger("range"), nbt.getInteger("maxRange"));
        }
        return new TargetingSettings(nbtTagCompound.getBoolean("attacksPlayers"),
                nbtTagCompound.getBoolean("attacksMobs"), nbtTagCompound.getBoolean("attacksNeutrals"),
                nbtTagCompound.getInteger("currentMaxRange"), nbtTagCompound.getInteger("upperBoundMaxRange"));
    }

    public NBTTagCompound writeToNBT(NBTTagCompound nbtTagCompound) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setBoolean("targetPlayers", this.isTargetPlayers());
        nbt.setBoolean("targetMobs", this.isTargetMobs());
        nbt.setBoolean("targetPassive", this.isTargetPassive());
        nbt.setInteger("range", this.getRange());
        nbt.setInteger("maxRange", this.getMaxRange());
        nbtTagCompound.setTag("targetingSettings", nbt);
        return nbtTagCompound;
    }

    public boolean isTargetPlayers() {
        return core.isTargetPlayers();
    }

    public TargetingSettings setTargetPlayers(boolean targetPlayers) {
        core.setTargetPlayers(targetPlayers);
        return this;
    }

    public boolean isTargetMobs() {
        return core.isTargetMobs();
    }

    public TargetingSettings setTargetMobs(boolean targetMobs) {
        core.setTargetMobs(targetMobs);
        return this;
    }

    public boolean isTargetPassive() {
        return core.isTargetPassive();
    }

    public TargetingSettings setTargetPassive(boolean targetPassive) {
        core.setTargetPassive(targetPassive);
        return this;
    }

    public int getRange() {
        return core.getRange();
    }

    /** Ignored if outside {@code [0, maxRange]}. */
    public TargetingSettings setRange(int range) {
        core.setRange(range);
        return this;
    }

    public int getMaxRange() {
        return core.getMaxRange();
    }

    /** Also lowers the range if it exceeds the new maximum. */
    public TargetingSettings setMaxRange(int maxRange) {
        core.setMaxRange(maxRange);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TargetingSettings that = (TargetingSettings) o;
        return isTargetPlayers() == that.isTargetPlayers() &&
                isTargetMobs() == that.isTargetMobs() &&
                isTargetPassive() == that.isTargetPassive() &&
                getRange() == that.getRange() &&
                getMaxRange() == that.getMaxRange();
    }

    @Override
    public int hashCode() {
        return Objects.hash(isTargetPlayers(), isTargetMobs(), isTargetPassive(), getRange(), getMaxRange());
    }
}
