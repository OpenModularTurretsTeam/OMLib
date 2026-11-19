package omtteam.omlib.util;

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
}
