package omtteam.omlib.api.block;

/**
 * Marker for blocks with a tile entity that may still be used as a disguise, because the tile entity only holds data
 * (such as the owner of a hardened block) and does not change how the block looks (1.20.1+ {@code PlainLookingBlock}).
 */
public interface IPlainLookingBlock {
}
