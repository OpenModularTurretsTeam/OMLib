package omtteam.omlib.api.permission;

import omtteam.omlib.util.player.Player;
import omtteam.omlib.util.player.PlayerUtil;

public interface IHasOwner {
    String getOwner();

    String getOwnerName();

    String getOwnerTeamName();

    default Player getOwnerAsPlayer() {
        return new Player(PlayerUtil.getPlayerUIDUnstable(getOwner()), getOwnerName(), getOwnerTeamName());
    }
}
