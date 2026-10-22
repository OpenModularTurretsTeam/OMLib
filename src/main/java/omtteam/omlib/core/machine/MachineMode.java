package omtteam.omlib.core.machine;

/**
 * Redstone control mode of a machine. Ported from OMLib {@code EnumMachineMode} / {@code TileEntityMachine}; the
 * order is the 3.x one, which is saved as the ordinal.
 */
public enum MachineMode {
    ALWAYS_ON("always_on"),
    ALWAYS_OFF("always_off"),
    /** Active while <b>not</b> powered (default). */
    INVERTED("inverted"),
    /** Active while powered. */
    NONINVERTED("noninverted");

    private final String serializedName;

    MachineMode(String serializedName) {
        this.serializedName = serializedName;
    }

    public String getSerializedName() {
        return serializedName;
    }

    /** Language key of the mode's name in the GUI (the 3.x OMLib key). */
    public String getTranslationKey() {
        return "gui.omtteam.omlib:" + serializedName;
    }

    /**
     * @param redstone whether the machine receives a redstone signal
     * @return whether a machine in this mode runs, given the signal
     */
    public boolean isActive(boolean redstone) {
        switch (this) {
            case ALWAYS_ON:
                return true;
            case ALWAYS_OFF:
                return false;
            case INVERTED:
                return !redstone;
            case NONINVERTED:
                return redstone;
            default:
                throw new IllegalStateException("Unknown mode " + this);
        }
    }

    /** The next mode in GUI cycling order. */
    public MachineMode next() {
        MachineMode[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    /** The mode with this serialized name, or {@code fallback} if there is none (unknown or missing saved value). */
    public static MachineMode bySerializedName(String name, MachineMode fallback) {
        for (MachineMode mode : values()) {
            if (mode.serializedName.equals(name)) return mode;
        }
        return fallback;
    }

    /** The mode saved as 3.x ordinal, or {@code fallback} if the value is out of range. */
    public static MachineMode byOrdinal(int ordinal, MachineMode fallback) {
        MachineMode[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : fallback;
    }
}
