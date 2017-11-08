package omtteam.omlib.util;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

@SuppressWarnings("unused")
public class TrustedPlayer {
    public String name = "";
    public boolean canOpenGUI = false;
    public boolean canChangeTargeting = false;
    public boolean admin = false;
    public UUID uuid;

    @ParametersAreNonnullByDefault
    public TrustedPlayer(String name) {
        this.name = name;
    }

    @ParametersAreNonnullByDefault
    public TrustedPlayer(Player player) {
        this.name = player.getName();
        this.uuid = player.getUuid();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCanOpenGUI(boolean canOpenGUI) {
        this.canOpenGUI = canOpenGUI;
    }

    public void setCanChangeTargeting(boolean canChangeTargeting) {
        this.canChangeTargeting = canChangeTargeting;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }
}
