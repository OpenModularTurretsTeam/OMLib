package omtteam.omlib.config;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigValidatorTest {
    @Test
    void valuesInsideTheRangeStay() {
        ConfigValidator v = new ConfigValidator();
        assertEquals(5, v.range("int", 5, 0, 10));
        assertEquals(0, v.range("int", 0, 0, 10));
        assertEquals(10, v.range("int", 10, 0, 10));
        assertEquals(0.5, v.range("double", 0.5, 0, 1));
        assertFalse(v.changed());
    }

    @Test
    void intsOutsideAreClamped() {
        ConfigValidator v = new ConfigValidator();
        assertEquals(0, v.range("int", -3, 0, 10));
        assertEquals(10, v.range("int", 11, 0, 10));
        assertTrue(v.changed());
    }

    @Test
    void doublesOutsideAreClampedAndNotANumberBecomesTheMinimum() {
        ConfigValidator v = new ConfigValidator();
        assertEquals(1.0, v.range("double", 1.5, 0, 1));
        assertEquals(0.25, v.range("double", Double.NaN, 0.25, 1));
        assertTrue(v.changed());
    }

    @Test
    void missingBoundsAreUnlimited() {
        ConfigValidator v = new ConfigValidator();
        assertEquals(1e300, v.range("double", 1e300, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));
        assertFalse(v.changed());
    }

    @Test
    void idsKeepValidResourceLocationsInOrder() {
        ConfigValidator v = new ConfigValidator();
        assertEquals(List.of("minecraft:stone", "dirt", "mod:path/sub"), v.ids("ids", List.of("minecraft:stone", "dirt", "mod:path/sub")));
        assertFalse(v.changed());
    }

    @Test
    void idsDropNullAndInvalidEntries() {
        ConfigValidator v = new ConfigValidator();
        List<String> ids = Arrays.asList("minecraft:stone", null, "Not Valid!", "a:b:c", "dirt");
        assertEquals(List.of("minecraft:stone", "dirt"), v.ids("ids", ids));
        assertTrue(v.changed());
    }

    @Test
    void aMissingListBecomesEmpty() {
        ConfigValidator v = new ConfigValidator();
        assertEquals(List.of(), v.filter("list", null, entry -> true));
        assertTrue(v.changed());
    }

    @Test
    void filterKeepsWhatPasses() {
        ConfigValidator v = new ConfigValidator();
        assertEquals(List.of("aa", "cc"), v.filter("list", List.of("aa", "b", "cc"), entry -> entry.length() == 2));
        assertTrue(v.changed());
    }
}
