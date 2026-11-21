package omtteam.omlib.platform.neoforge;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import omtteam.omlib.blockentity.TransferHost;
import omtteam.omlib.core.energy.EnergyAccess;

import java.util.List;

/**
 * Item and energy capabilities of the {@link TransferHost}s, on the sides each host exposes ({@code IItemHandler} and
 * {@code IEnergyStorage}, NeoForge before its transfer API; the same as the Fabric lookups).
 */
public final class NeoForgeCapabilities {
    private NeoForgeCapabilities() {
    }

    /** Registers the capabilities of the block entity types of a mod (its {@code ModBlockEntities.transferTypes()}). */
    public static void register(RegisterCapabilitiesEvent event, List<BlockEntityType<? extends TransferHost>> types) {
        for (BlockEntityType<? extends TransferHost> type : types) {
            registerType(event, type);
        }
    }

    private static <T extends BlockEntity & TransferHost> void registerType(RegisterCapabilitiesEvent event,
                                                                            BlockEntityType<T> type) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type,
                (host, side) -> new NeoForgeItemHandler(host.getItemAccess(side)));
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, type, (host, side) -> {
            EnergyAccess access = host.getEnergyAccess(side);
            return access == null ? null : new NeoForgeEnergyHandler(access);
        });
    }
}
