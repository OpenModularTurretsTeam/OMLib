package omtteam.omlib.compat;

import net.minecraft.core.Direction;
import omtteam.omlib.core.world.BlockPosition;
import omtteam.omlib.core.world.Face;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * OpenModularCore's {@link Face} against Minecraft's {@link Direction}, on every node: the core cannot see Minecraft, so
 * this test lives in the game layer. The mods convert between the two by name (OMT) and by ordinal (OML).
 */
class FaceTest {
    @Test
    void everyStepIsOneBlockAlongOneAxis() {
        for (Face face : Face.values()) {
            int length = Math.abs(face.getStepX()) + Math.abs(face.getStepY()) + Math.abs(face.getStepZ());
            assertEquals(1, length, face.name());
        }
    }

    @Test
    void oppositeFacesCancelOut() {
        BlockPosition origin = new BlockPosition(3, -7, 12);
        assertEquals(origin, origin.relative(Face.DOWN).relative(Face.UP));
        assertEquals(origin, origin.relative(Face.NORTH).relative(Face.SOUTH));
        assertEquals(origin, origin.relative(Face.WEST).relative(Face.EAST));
    }

    @Test
    void relativeMovesByTheStep() {
        BlockPosition origin = new BlockPosition(3, -7, 12);
        assertEquals(new BlockPosition(3, -8, 12), origin.relative(Face.DOWN));
        assertEquals(new BlockPosition(3, -7, 11), origin.relative(Face.NORTH));
        assertEquals(new BlockPosition(4, -7, 12), origin.relative(Face.EAST));
    }

    @Test
    void namesOrderAndStepsMatchMinecraftsDirection() {
        assertEquals(Direction.values().length, Face.values().length);
        for (Face face : Face.values()) {
            Direction direction = Direction.values()[face.ordinal()];
            assertEquals(direction.name(), face.name());
            assertEquals(direction.getStepX(), face.getStepX(), face.name());
            assertEquals(direction.getStepY(), face.getStepY(), face.name());
            assertEquals(direction.getStepZ(), face.getStepZ(), face.name());
        }
    }
}
