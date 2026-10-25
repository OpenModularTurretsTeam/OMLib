package omtteam.omlib.core;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import omtteam.omlib.core.camo.CamoLight;
import omtteam.omlib.core.security.AccessProfile;
import omtteam.omlib.core.security.PlayerIdentity;
import omtteam.omlib.core.security.PlayerInfo;
import omtteam.omlib.core.world.BlockPosition;
import org.junit.jupiter.api.Test;

/**
 * equals and hashCode of the core value classes (the Java 8 stand-ins for records). {@code NULL_FIELDS}: the
 * constructors reject or copy their arguments, so those fields are never null.
 */
class EqualsContractTest {
    @Test
    void blockPosition() {
        EqualsVerifier.forClass(BlockPosition.class).verify();
    }

    @Test
    void camoLight() {
        EqualsVerifier.forClass(CamoLight.class).verify();
    }

    @Test
    void accessProfile() {
        EqualsVerifier.forClass(AccessProfile.class).suppress(Warning.NULL_FIELDS).verify();
    }

    @Test
    void playerIdentity() {
        EqualsVerifier.forClass(PlayerIdentity.class).suppress(Warning.NULL_FIELDS).verify();
    }

    @Test
    void playerInfo() {
        EqualsVerifier.forClass(PlayerInfo.class).suppress(Warning.NULL_FIELDS).verify();
    }
}
