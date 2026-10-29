package omtteam.omlib.core.machine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TargetingSettingsTest {
    @Test
    void defaultsTargetOnlyMobs() {
        TargetingSettings settings = TargetingSettings.defaults();
        assertFalse(settings.isTargetPlayers());
        assertTrue(settings.isTargetMobs());
        assertFalse(settings.isTargetPassive());
        assertFalse(new TargetingSettings(false, false, false, 0, 0).isTargetMobs());
        assertEquals(0, settings.getRange());
        assertEquals(0, settings.getMaxRange());
    }

    @Test
    void constructorClampsTheRange() {
        assertEquals(10, new TargetingSettings(false, true, false, 15, 10).getRange());
        assertEquals(0, new TargetingSettings(false, true, false, -3, 10).getRange());
        assertEquals(0, new TargetingSettings(false, true, false, 5, -1).getMaxRange());
    }

    @Test
    void rangeOutsideTheMaximumIsIgnored() {
        TargetingSettings settings = new TargetingSettings(false, true, false, 5, 10);
        settings.setRange(11);
        assertEquals(5, settings.getRange());
        settings.setRange(-1);
        assertEquals(5, settings.getRange());
        settings.setRange(10);
        assertEquals(10, settings.getRange());
    }

    @Test
    void negativeMaxRangeCountsAsZero() {
        TargetingSettings settings = new TargetingSettings(false, true, false, 5, 10);
        settings.setMaxRange(-1);
        assertEquals(0, settings.getMaxRange());
        assertEquals(0, settings.getRange(), "the range is never negative");
    }

    @Test
    void loweringTheMaximumLowersTheRange() {
        TargetingSettings settings = new TargetingSettings(false, true, false, 10, 10);
        settings.setMaxRange(6);
        assertEquals(6, settings.getRange());
        settings.setMaxRange(20);
        assertEquals(6, settings.getRange());
    }

    @Test
    void rangeEqualToTheMaximumIsAccepted() {
        TargetingSettings settings = new TargetingSettings(false, true, false, 0, 10);
        settings.setRange(10);
        assertEquals(10, settings.getRange());
        settings.setRange(0);
        assertEquals(0, settings.getRange());
    }

    @Test
    void copiesAreIndependent() {
        TargetingSettings settings = new TargetingSettings(true, false, true, 4, 8);
        TargetingSettings copy = settings.copy();
        copy.setTargetPlayers(false);
        copy.setRange(2);
        assertTrue(settings.isTargetPlayers());
        assertEquals(4, settings.getRange());
        assertTrue(copy.isTargetPassive());
        assertEquals(8, copy.getMaxRange());
    }
}
