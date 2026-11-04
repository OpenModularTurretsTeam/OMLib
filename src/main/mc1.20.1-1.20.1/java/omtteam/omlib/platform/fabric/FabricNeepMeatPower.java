package omtteam.omlib.platform.fabric;

import com.neep.neepmeat.api.PowerUtil;
import com.neep.neepmeat.transport.api.pipe.BloodAcceptor;
import com.neep.neepmeat.transport.blood_network.BloodTransferChangeListener;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import omtteam.omlib.blockentity.TransferHost;
import omtteam.omlib.core.energy.EnergyAccess;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * NEEPMeat (Fabric, 1.20.1 only): the power of its vascular network charges the energy of the {@link TransferHost}s,
 * at 1 eJ = 1 E. Only loaded when NEEPMeat is installed (compile-only, never bundled).
 * <p>
 * The network carries power, not energy: a sink is told the power it gets (eJ per tick) whenever that changes. Each
 * side of a host that a vascular conduit touches is an active sink, served before NEEPMeat's passive sinks (motors):
 * it claims only what the host's energy still accepts per tick (its {@link EnergyAccess#insert} limit and free space)
 * and reports that as its demand, so a full buffer takes nothing. After each server tick the claimed power goes into
 * the host's energy; a sink whose free intake changed asks the network to share its power out again.
 * <p>
 * A network forgets its sinks next to a conduit whose chunk unloads without telling them, and finds them again when it
 * loads. So a side keeps one sink while it is part of a network (found again, it gets its new share instead of a second
 * sink charging too), and a sink charges nothing while the block it touches is not loaded.
 */
final class FabricNeepMeatPower {
    static final String MOD_ID = "neepmeat";

    /** Energy (E) per eJ of NEEPMeat power. */
    private static final long ENERGY_PER_EJ = 1;

    /** The sinks that are part of a network, by host side (changed on the server thread, looked up from any). */
    private static final Map<Side, Sink> CONNECTED = new ConcurrentHashMap<>();

    private static boolean eventsRegistered;

    private FabricNeepMeatPower() {
    }

    /** Makes the hosts of {@code type} sinks of vascular networks, on each side where they expose energy. */
    static synchronized <T extends BlockEntity & TransferHost> void register(BlockEntityType<T> type) {
        if (!eventsRegistered) {
            ServerTickEvents.END_SERVER_TICK.register(server -> tick());
            ServerLifecycleEvents.SERVER_STOPPED.register(server -> CONNECTED.clear());
            eventsRegistered = true;
        }
        BloodAcceptor.SIDED.registerForBlockEntity((host, side) -> {
            if (host.getEnergyAccess(side) == null) {
                return null;
            }
            Sink connected = CONNECTED.get(new Side(host, side));
            return connected != null ? connected : new Sink(host, side);
        }, type);
    }

    private static void tick() {
        Iterator<Sink> it = CONNECTED.values().iterator();
        while (it.hasNext()) {
            Sink sink = it.next();
            if (sink.blockEntity.isRemoved()) {
                it.remove();
            } else {
                sink.tick();
            }
        }
    }

    /** A side of a host block entity (compared by identity of the block entity). */
    private record Side(BlockEntity blockEntity, Direction side) {
    }

    /** A host side as an active sink of a vascular network; kept in {@link #CONNECTED} while a network holds it. */
    private static final class Sink implements BloodAcceptor {
        private final BlockEntity blockEntity;
        private final TransferHost host;
        private final Direction side;
        @Nullable
        private BloodTransferChangeListener network;
        /** Power that the network gives this sink, eJ per tick. */
        private long claimed;
        /** Power that this sink asks for, eJ per tick: what the host's energy accepts. */
        private long demand;

        private <T extends BlockEntity & TransferHost> Sink(T host, Direction side) {
            this.blockEntity = host;
            this.host = host;
            this.side = side;
        }

        @Override
        public Mode getMode() {
            return Mode.SINK;
        }

        @Override
        public void setChangeListener(@Nullable BloodTransferChangeListener network) {
            this.network = network;
            Side key = new Side(blockEntity, side);
            if (network != null) {
                demand = freeIntake();
                CONNECTED.put(key, this);
            } else {
                claimed = 0;
                CONNECTED.remove(key, this);
            }
        }

        @Override
        public long getDemandHint() {
            return demand;
        }

        /** @param influx the power left on the network, in NEEPMeat's per-unit power; returns the part taken */
        @Override
        public float updateInflux(float influx) {
            long offered = Math.max(0, (long) (influx * PowerUtil.referencePower()));
            demand = freeIntake();
            claimed = Math.min(offered, demand);
            return (float) claimed / PowerUtil.referencePower();
        }

        /**
         * Charges the host with the claimed power, unless the network may have dropped this sink (the block it touches
         * is not loaded); asks for a new share when its free intake changed.
         */
        private void tick() {
            EnergyAccess energy = host.getEnergyAccess(side);
            Level level = blockEntity.getLevel();
            if (energy != null && claimed > 0 && level != null && level.isLoaded(blockEntity.getBlockPos().relative(side))) {
                energy.insert(claimed * ENERGY_PER_EJ, false);
            }
            long intake = freeIntake();
            if (intake != demand) {
                demand = intake;
                if (network != null) {
                    network.updateTransfer(this);
                }
            }
        }

        /** eJ per tick that the host's energy accepts now. */
        private long freeIntake() {
            EnergyAccess energy = host.getEnergyAccess(side);
            return energy == null ? 0 : energy.insert(Long.MAX_VALUE, true) / ENERGY_PER_EJ;
        }
    }
}
