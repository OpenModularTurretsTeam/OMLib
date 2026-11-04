package omtteam.omlib.platform.fabric;

import com.neep.neepmeat.api.PowerUtil;
import com.neep.neepmeat.transport.api.pipe.BloodAcceptor;
import com.neep.neepmeat.transport.blood_network.BloodTransferChangeListener;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import omtteam.omlib.blockentity.TransferHost;
import omtteam.omlib.core.energy.EnergyAccess;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * NEEPMeat (Fabric, 1.20.1 only): the power of its vascular network charges the energy of the {@link TransferHost}s,
 * at 1 eJ = 1 E. Only loaded when NEEPMeat is installed (compile-only, never bundled).
 * <p>
 * The network carries power, not energy: a sink is told the power it gets (eJ per tick) whenever that changes. Each
 * side of a host that a vascular conduit touches is an active sink, served before NEEPMeat's passive sinks (motors):
 * it claims only what the host's energy still accepts per tick (its {@link EnergyAccess#insert} limit and free space)
 * and reports that as its demand, so a full buffer takes nothing. After each server tick the claimed power goes into
 * the host's energy; a sink whose free intake changed asks the network to share its power out again.
 */
final class FabricNeepMeatPower {
    static final String MOD_ID = "neepmeat";

    /** Energy (E) per eJ of NEEPMeat power. */
    private static final long ENERGY_PER_EJ = 1;

    /** Sinks that are part of a network (server thread only). */
    private static final Set<Sink> CONNECTED = new LinkedHashSet<>();

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
        BloodAcceptor.SIDED.registerForBlockEntity(
                (host, side) -> host.getEnergyAccess(side) == null ? null : new Sink(host, side), type);
    }

    private static void tick() {
        Iterator<Sink> it = CONNECTED.iterator();
        while (it.hasNext()) {
            Sink sink = it.next();
            if (sink.blockEntity.isRemoved()) {
                it.remove();
            } else {
                sink.tick();
            }
        }
    }

    /** A host side as an active sink of a vascular network; one per lookup, kept while a network holds it. */
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
            return Mode.ACTIVE_SINK;
        }

        @Override
        public void setChangeListener(@Nullable BloodTransferChangeListener network) {
            this.network = network;
            if (network != null) {
                demand = freeIntake();
                CONNECTED.add(this);
            } else {
                claimed = 0;
                CONNECTED.remove(this);
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

        /** Charges the host with the claimed power; asks for a new share when its free intake changed. */
        private void tick() {
            EnergyAccess energy = host.getEnergyAccess(side);
            if (energy != null && claimed > 0) {
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
