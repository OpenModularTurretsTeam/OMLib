package omtteam.omlib.util;

import omtteam.omlib.core.machine.MachineMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnumMachineModeTest {
    @Test
    void theConstantsMatchTheCoreModes() {
        for (EnumMachineMode mode : EnumMachineMode.values()) {
            assertEquals(mode.getName(), mode.toCore().getSerializedName());
            assertEquals(mode, EnumMachineMode.fromCore(mode.toCore()));
        }
    }

    @Test
    void savedOrdinalsAreReadWithAFallback() {
        assertEquals(MachineMode.ALWAYS_ON, EnumMachineMode.byOrdinal(0, MachineMode.INVERTED));
        assertEquals(MachineMode.NONINVERTED, EnumMachineMode.byOrdinal(3, MachineMode.INVERTED));
        assertEquals(MachineMode.INVERTED, EnumMachineMode.byOrdinal(-1, MachineMode.INVERTED));
        assertEquals(MachineMode.ALWAYS_OFF, EnumMachineMode.byOrdinal(4, MachineMode.ALWAYS_OFF));
    }
}
