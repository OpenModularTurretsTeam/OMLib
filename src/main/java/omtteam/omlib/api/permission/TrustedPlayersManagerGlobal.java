package omtteam.omlib.api.permission;

import net.minecraft.tileentity.TileEntity;
import omtteam.omlib.util.DebugHandler;
import omtteam.omlib.util.GeneralUtil;
import omtteam.omlib.util.player.Player;
import omtteam.omlib.util.player.PlayerUtil;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Keridos on 09/02/17.
 * This Class is the non linked version of a TPM, when adding player, csome checks need to be made before calling addTP.
 */
public class TrustedPlayersManagerGlobal implements ITrustedPlayersManager {
    protected List<TrustedPlayer> trustedPlayers = new ArrayList<>();
    Player owner;

    public TrustedPlayersManagerGlobal(Player owner) {
        this.owner = owner;
    }

    @Override
    public Player getOwner() {
        return this.owner;
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean addTrustedPlayer(String name) {
        Player player = PlayerUtil.getPlayerFromUsernameCache(name);
        if (player == null && GeneralUtil.isServerInOnlineMode()) {
            DebugHandler.getInstance().sendMessageToDebugChat("Did not find player named " + name + "in the username cache.");
            return false;
        } else if (player == null) {
            DebugHandler.getInstance().sendMessageToDebugChat("Invalid player!");
            return false;
        } else {
            TrustedPlayer trustedPlayer = new TrustedPlayer(player.getName());
            trustedPlayer.setUuid(player.getUuid());

            if (trustedPlayer.getUuid() != null || !GeneralUtil.isServerInOnlineMode()) {
                for (TrustedPlayer existPlayer : trustedPlayers) {
                    if (!GeneralUtil.isServerInOnlineMode()) {
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
    public List<TrustedPlayer> getTrustedPlayers() {
        return trustedPlayers;
    }

    @Override
    public void setTrustedPlayers(List<TrustedPlayer> trustedPlayers) {
        this.trustedPlayers = trustedPlayers;
    }

    @Override
    public boolean useGlobal() {
        return false;
    }

    @Override
    public void setUseGlobal(boolean useGlobal) {
        //Do nothing;
    }

    @Override
    public boolean hasTile() {
        return false;
    }

    @Override
    public TileEntity getTile() {
        return null;
    }
}
