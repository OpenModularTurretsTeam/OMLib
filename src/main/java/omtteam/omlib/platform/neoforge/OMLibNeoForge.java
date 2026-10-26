package omtteam.omlib.platform.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import omtteam.omlib.OMLib;
import omtteam.omlib.block.InteractiveBlock;
import omtteam.omlib.network.ConfigSync;
import omtteam.omlib.platform.Platform;
import omtteam.omlib.registry.RegistrationStep;
import omtteam.omlib.startup.OMLibStartup;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * NeoForge entrypoint of the library: installs the platform services for all mods that use it (the mods declare that
 * they load after it), runs each registration step in the {@link RegisterEvent} of its registry and applies the
 * library's packets in its own payload event. The mods apply their own packets in theirs
 * ({@link NeoForgeNetwork#applyRegistrations}).
 */
@Mod(OMLib.MOD_ID)
public final class OMLibNeoForge {
    private final List<RegistrationStep> steps = OMLibStartup.registrationSteps();
    private final Set<RegistrationStep> done = new HashSet<>();

    public OMLibNeoForge(IEventBus modBus) {
        Platform.ensureInstalled();
        OMLibStartup.init();
        modBus.addListener(this::onRegister);
        modBus.addListener(this::onCommonSetup);
        modBus.addListener((RegisterPayloadHandlersEvent event) ->
                ((NeoForgeNetwork) Platform.network()).applyRegistrations(event, OMLib.MOD_ID));
        NeoForge.EVENT_BUS.addListener(OMLibNeoForge::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(OMLibNeoForge::onPlayerLoggedIn);
    }

    private static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ConfigSync.onPlayerJoin(player);
        }
    }

    private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        InteractionResult result = InteractiveBlock.handleUse(event.getEntity(), event.getLevel(), event.getHand(),
                event.getHitVec());
        if (result != InteractionResult.PASS) {
            event.setCanceled(true);
            event.setCancellationResult(result);
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
