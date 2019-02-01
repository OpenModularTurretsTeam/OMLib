package omtteam.omlib.api.permission;

import omtteam.omlib.util.player.Player;

import java.util.UUID;

public interface IHasOwner {
    String getOwner();

    String getOwnerName();

    String getOwnerTeamName();

    default Player getOwnerAsPlayer() {
        return new Player(UUID.fromString(getOwner()), getOwnerName(), getOwnerTeamName());
    }
}
