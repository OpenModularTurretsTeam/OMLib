package omtteam.omlib.core.machine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MachineModeTest {
    @Test
    void activeByRedstone() {
        assertTrue(MachineMode.ALWAYS_ON.isActive(false));
        assertTrue(MachineMode.ALWAYS_ON.isActive(true));
        assertFalse(MachineMode.ALWAYS_OFF.isActive(false));
        assertFalse(MachineMode.ALWAYS_OFF.isActive(true));
        assertTrue(MachineMode.INVERTED.isActive(false));
        assertFalse(MachineMode.INVERTED.isActive(true));
        assertFalse(MachineMode.NONINVERTED.isActive(false));
        assertTrue(MachineMode.NONINVERTED.isActive(true));
    }

    @Test
    void cyclingVisitsEveryModeOnce() {
        MachineMode mode = MachineMode.ALWAYS_ON;
        for (int i = 1; i < MachineMode.values().length; i++) {
            mode = mode.next();
            assertNotSame(MachineMode.ALWAYS_ON, mode, "back at the start after " + i + " steps");
        }
        assertEquals(MachineMode.ALWAYS_ON, mode.next());
    }

    @Test
    void serializedNamesRoundTrip() {
        for (MachineMode mode : MachineMode.values()) {
            assertEquals(mode, MachineMode.bySerializedName(mode.getSerializedName(), MachineMode.ALWAYS_OFF));
        }
    }

    @Test
    void unknownNamesFallBack() {
        assertEquals(MachineMode.INVERTED, MachineMode.bySerializedName("", MachineMode.INVERTED));
        assertEquals(MachineMode.INVERTED, MachineMode.bySerializedName("ALWAYS_ON", MachineMode.INVERTED),
                "names are case-sensitive");
    }

    @Test
    void ordinalsAreThe3xOrder() {
        assertEquals(MachineMode.ALWAYS_ON, MachineMode.byOrdinal(0, MachineMode.INVERTED));
        assertEquals(MachineMode.ALWAYS_OFF, MachineMode.byOrdinal(1, MachineMode.INVERTED));
        assertEquals(MachineMode.INVERTED, MachineMode.byOrdinal(2, MachineMode.ALWAYS_ON));
        assertEquals(MachineMode.NONINVERTED, MachineMode.byOrdinal(3, MachineMode.INVERTED));
        assertEquals(MachineMode.INVERTED, MachineMode.byOrdinal(4, MachineMode.INVERTED));
        assertEquals(MachineMode.INVERTED, MachineMode.byOrdinal(-1, MachineMode.INVERTED));
    }

    @Test
    void translationKeysAreThe3xKeys() {
        assertEquals("gui.omtteam.omlib:noninverted", MachineMode.NONINVERTED.getTranslationKey());
    }
}
