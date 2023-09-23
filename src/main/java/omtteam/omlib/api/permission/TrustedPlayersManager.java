package omtteam.omlib.api.permission;

import net.minecraft.tileentity.TileEntity;
import omtteam.omlib.handler.OMConfig;
import omtteam.omlib.util.DebugHandler;
import omtteam.omlib.util.player.Player;
import omtteam.omlib.util.player.PlayerUtil;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Keridos on 09/02/17.
 * This Class is the non linked version of a TPM, when adding player, csome checks need to be made before calling addTP.
 */
public class TrustedPlayersManager implements ITrustedPlayersManager {
    protected List<TrustedPlayer> trustedPlayers = new ArrayList<>();
    Player owner;
    boolean useGlobal;
    TileEntity tile;

    public TrustedPlayersManager(Player owner) {
        this.owner = owner;
        this.useGlobal = false;
    }

    public TrustedPlayersManager(Player owner, TileEntity tile) {
        this.owner = owner;
        this.useGlobal = false;
        this.tile = tile;
    }

    @Override
    public Player getOwner() {
        return this.owner;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean addTrustedPlayer(String name) {
        Player player = PlayerUtil.getPlayerFromUsernameCache(name);
        if (player == null && !OMConfig.GENERAL.offlineModeSupport) {
            DebugHandler.getInstance().sendMessageToDebugChat("Did not find player named " + name + "in the username cache.");
            return false;
        } else if (player == null) {
            DebugHandler.getInstance().sendMessageToDebugChat("Invalid player!");
            return false;
        } else {
            TrustedPlayer trustedPlayer = new TrustedPlayer(player.getName());
            trustedPlayer.setUuid(player.getUuid());

            if (trustedPlayer.getUuid() != null || OMConfig.GENERAL.offlineModeSupport) {
                for (TrustedPlayer existPlayer : trustedPlayers) {
                    if (OMConfig.GENERAL.offlineModeSupport) {
                        if (existPlayer.getName().equalsIgnoreCase(name)) {
                            DebugHandler.getInstance().sendMessageToDebugChat("Already on trust list!");
                            return false;
                        }
                    } else {
                        if (existPlayer.getName().equalsIgnoreCase(name) || trustedPlayer.getUuid().equals(existPlayer.getUuid())) {
                            return false;
                        }
                    }
                }
                DebugHandler.getInstance().sendMessageToDebugChat("Sucessfully added " + name + ".");
                trustedPlayers.add(trustedPlayer);
                return true;
            }
            return false;
        }
    }

    @Override
    public TrustedPlayer getTrustedPlayer(String name) {
        if (useGlobal) {
            return GlobalTrustRegister.instance.getTrustedPlayer(owner, name);
        }
        return ITrustedPlayersManager.super.getTrustedPlayer(name);
    }

    @Override
    public TrustedPlayer getTrustedPlayer(UUID uuid) {
        if (useGlobal) {
            return GlobalTrustRegister.instance.getTrustedPlayer(owner, uuid);
        }
        return ITrustedPlayersManager.super.getTrustedPlayer(uuid);
    }

    @Override
    public TrustedPlayer getTrustedPlayer(Player player) {
        if (useGlobal) {
            return GlobalTrustRegister.instance.getTrustedPlayer(owner, player);
        }
        return ITrustedPlayersManager.super.getTrustedPlayer(player);
    }

    @Override
    public List<TrustedPlayer> getTrustedPlayers() {
        return trustedPlayers;
    }

    @Override
    public void setTrustedPlayers(List<TrustedPlayer> trustedPlayers) {
        this.trustedPlayers = trustedPlayers;
    }

    @Override
    public boolean useGlobal() {
        return useGlobal;
    }

    @Override
    public void setUseGlobal(boolean useGlobal) {
        this.useGlobal = useGlobal;
    }

    @Override
    public boolean hasTile() {
        return tile != null;
    }

    @Override
    public TileEntity getTile() {
        return tile;
    }
}
