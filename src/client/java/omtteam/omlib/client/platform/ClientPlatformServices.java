package omtteam.omlib.client.platform;

import java.util.Objects;

/**
 * Every loader-specific service that shared client code calls; the client counterpart of
 * {@link omtteam.omlib.platform.PlatformServices}.
 */
public record ClientPlatformServices(ClientNetwork network) {
    public ClientPlatformServices {
        Objects.requireNonNull(network, "network");
    }
}
