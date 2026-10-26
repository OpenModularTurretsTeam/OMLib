package omtteam.omlib.platform.fabric;

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import omtteam.omlib.blockentity.TransferHost;
import omtteam.omlib.core.energy.EnergyAccess;
import team.reborn.energy.api.EnergyStorage;

import java.util.List;

/**
 * Fabric API lookups of the {@link TransferHost}s: items and energy, on the sides each host exposes.
 */
public final class FabricBlockLookups {
    private FabricBlockLookups() {
    }

    /** Registers the lookups of the block entity types of a mod (its {@code ModBlockEntities.transferTypes()}), after they are registered. */
    public static void register(List<BlockEntityType<? extends TransferHost>> types) {
        for (BlockEntityType<? extends TransferHost> type : types) {
            registerType(type);
        }
    }

    private static <T extends BlockEntity & TransferHost> void registerType(BlockEntityType<T> type) {
        ItemStorage.SIDED.registerForBlockEntity((host, side) -> FabricItemStorages.of(host.getItemAccess(side)), type);
        EnergyStorage.SIDED.registerForBlockEntity((host, side) -> {
            EnergyAccess access = host.getEnergyAccess(side);
            return access == null ? null : FabricEnergyStorages.of(access);
        }, type);
    }
}
