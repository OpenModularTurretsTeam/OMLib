package omtteam.omlib.api.permission;

import net.minecraftforge.common.UsernameCache;
import omtteam.omlib.handler.ConfigHandler;
import omtteam.omlib.tileentity.TileEntityOwnedBlock;
import omtteam.omlib.util.DebugHandler;
import omtteam.omlib.util.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static omtteam.omlib.util.player.PlayerUtil.getPlayerUUID;
import static omtteam.omlib.util.player.PlayerUtil.isPlayerOwner;

/**
 * Created by Keridos on 09/02/17.
 * This Class is the default TPM for use with Owned Tiles like Turret Bases from OMT.
 */
public class TrustedPlayersManagerTile implements ITrustedPlayersManager {
    protected final TileEntityOwnedBlock ownedBlock;
    protected List<TrustedPlayer> trustedPlayers = new ArrayList<>();
    protected boolean useGlobal = false;

    public TrustedPlayersManagerTile(TileEntityOwnedBlock ownedBlock) {
        this.ownedBlock = ownedBlock;
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean addTrustedPlayer(String name) {
        TrustedPlayer trustedPlayer = new TrustedPlayer(name);
        trustedPlayer.setUuid(getPlayerUUID(name));

        if (!ownedBlock.getWorld().isRemote) {
            Player player = null;
            boolean foundPlayer = false;
            for (Map.Entry<UUID, String> serverName : UsernameCache.getMap().entrySet()) {
                if (name.equals(serverName.getValue())) {
                    player = new Player(serverName.getKey(), serverName.getValue());
                    foundPlayer = true;
                    break;
                }
            }
            if (!foundPlayer) {
                player = new Player(null, name);
            }

            if (!foundPlayer && !ConfigHandler.offlineModeSupport) {
                DebugHandler.getInstance().sendMessageToDebugChat("Did not find player named " + name + "in the username cache.");
                return false;
            }

            if (ConfigHandler.offlineModeSupport) {
                if (isPlayerOwner(player, ownedBlock)) {
                    DebugHandler.getInstance().sendMessageToDebugChat("You cannot add an owner!");
                    return false;
                }
            } else {
                if (trustedPlayer.getUuid() == null || isPlayerOwner(player, ownedBlock)) {
                    DebugHandler.getInstance().sendMessageToDebugChat("You cannot add an owner!");
                    return false;
                }
            }

            if (trustedPlayer.getUuid() != null || ConfigHandler.offlineModeSupport) {
                for (TrustedPlayer existPlayer : trustedPlayers) {
                    if (ConfigHandler.offlineModeSupport) {
                        if (existPlayer.getName().toLowerCase().equals(name.toLowerCase())) {
                            DebugHandler.getInstance().sendMessageToDebugChat("Already on trust list!");
                            return false;
                        }
                    } else {
                        if (existPlayer.getName().toLowerCase().equals(name.toLowerCase()) || trustedPlayer.getUuid().equals(existPlayer.getUuid())) {
                            return false;
                        }
                    }
                }
                DebugHandler.getInstance().sendMessageToDebugChat("Sucessfully added " + name + ".");
                trustedPlayers.add(trustedPlayer);
                return true;
            }
        }
        return false;
    }

    @Override
    public TrustedPlayer getTrustedPlayer(String name) {
        if (useGlobal) {
            return GlobalTrustRegister.instance.getTrustedPlayer(ownedBlock.getOwnerAsPlayer(), name);
        }
        return ITrustedPlayersManager.super.getTrustedPlayer(name);
    }

    @Override
    public TrustedPlayer getTrustedPlayer(UUID uuid) {
        if (useGlobal) {
            return GlobalTrustRegister.instance.getTrustedPlayer(ownedBlock.getOwnerAsPlayer(), uuid);
        }
        return ITrustedPlayersManager.super.getTrustedPlayer(uuid);
    }

    @Override
    public TrustedPlayer getTrustedPlayer(Player player) {
        if (useGlobal) {
            return GlobalTrustRegister.instance.getTrustedPlayer(ownedBlock.getOwnerAsPlayer(), player);
        }
        return ITrustedPlayersManager.super.getTrustedPlayer(player);
    }

    public boolean useGlobal() {
        return useGlobal;
    }

    public void setUseGlobal(boolean useGlobal) {
        this.useGlobal = useGlobal;
    }

    @Override
    public List<TrustedPlayer> getTrustedPlayers() {
        return trustedPlayers;
    }

    @Override
    public void setTrustedPlayers(List<TrustedPlayer> trustedPlayers) {
        this.trustedPlayers = trustedPlayers;
    }
}
