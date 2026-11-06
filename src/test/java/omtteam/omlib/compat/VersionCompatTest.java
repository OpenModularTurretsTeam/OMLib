package omtteam.omlib.compat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VersionCompatTest {
    @Test
    void idsAreBuiltFromNamespaceAndPath() {
        assertEquals("othermod:some/path", VersionCompat.id("othermod", "some/path").toString());
    }
}
