package omtteam.omlib.platform;

import java.util.Objects;

/**
 * Every loader-specific service that shared code calls. {@link Platform} creates the running loader's one on first use
 * ({@link Platform#ensureInstalled()}); a new component makes each loader's branch there fail to compile until it
 * supplies it.
 */
public record PlatformServices(LoaderInfo loaderInfo, Fuel fuel, Network network, Menus menus,
                               Energy energy) {
    public PlatformServices {
        Objects.requireNonNull(loaderInfo, "loaderInfo");
        Objects.requireNonNull(fuel, "fuel");
        Objects.requireNonNull(network, "network");
        Objects.requireNonNull(menus, "menus");
        Objects.requireNonNull(energy, "energy");
    }
}
