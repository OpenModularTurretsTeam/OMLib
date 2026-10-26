package omtteam.omlib.platform.forge;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.RegisterEvent;
import omtteam.omlib.OMLib;
import omtteam.omlib.platform.Platform;
import omtteam.omlib.registry.RegistrationStep;
import omtteam.omlib.startup.OMLibStartup;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Forge entrypoint of the library: installs the platform services for all mods that use it (the mods declare that they
 * load after it), runs each registration step in the {@link RegisterEvent} of its registry and attaches the item and
 * energy capabilities to every transfer block entity. Forge 1.20.1 has no client-only {@code @Mod} classes, so it also
 * starts the client entrypoint ({@code OMLibForgeClient}) on a client.
 */
@Mod(OMLib.MOD_ID)
public final class OMLibForge {
    /** Started by name: main cannot see the client source set. */
    private static final String CLIENT_ENTRYPOINT = "omtteam.omlib.client.platform.forge.OMLibForgeClient";

    private final List<RegistrationStep> steps = OMLibStartup.registrationSteps();
    private final Set<RegistrationStep> done = new HashSet<>();

    public OMLibForge() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        Platform.ensureInstalled();
        OMLibStartup.init();
        modBus.addListener(this::onRegister);
        modBus.addListener(this::onCommonSetup);
        MinecraftForge.EVENT_BUS.addGenericListener(BlockEntity.class, ForgeBlockCapabilities::attach);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            startClient(modBus);
        }
    }

    private static void startClient(IEventBus modBus) {
        try {
            Class.forName(CLIENT_ENTRYPOINT).getConstructor(IEventBus.class).newInstance(modBus);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not start the client entrypoint " + CLIENT_ENTRYPOINT, e);
        }
    }

    private void onRegister(RegisterEvent event) {
        for (RegistrationStep step : steps) {
            if (step.registry().equals(event.getRegistryKey())) {
                step.register().run();
                done.add(step);
            }
        }
    }

    /** A step whose registry fired no event would leave its content unregistered: fail loudly instead. */
    private void onCommonSetup(FMLCommonSetupEvent event) {
        if (done.size() != steps.size()) {
            List<RegistrationStep> missing = steps.stream().filter(step -> !done.contains(step)).toList();
            throw new IllegalStateException("Registration steps never ran: " + missing);
        }
    }
}
