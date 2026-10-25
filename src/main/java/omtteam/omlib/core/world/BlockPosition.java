package omtteam.omlib.core.world;

/**
 * A block position without Minecraft types (1.20.1+ OMT {@code core.world.BlockPosition}), for core rules that walk
 * blocks, such as {@link FloodFill}.
 */
public final class BlockPosition {
    private final int x;
    private final int y;
    private final int z;

    public BlockPosition(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int z() {
        return z;
    }

    /** The position moved by the given offsets. */
    public BlockPosition offset(int dx, int dy, int dz) {
        return new BlockPosition(x + dx, y + dy, z + dz);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlockPosition)) return false;
        BlockPosition other = (BlockPosition) o;
        return x == other.x && y == other.y && z == other.z;
    }

    @Override
    public int hashCode() {
        return 31 * (31 * x + y) + z;
    }

    @Override
    public String toString() {
        return "BlockPosition[" + x + ", " + y + ", " + z + "]";
    }
}
