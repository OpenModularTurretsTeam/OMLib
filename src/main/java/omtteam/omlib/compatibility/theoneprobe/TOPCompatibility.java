package omtteam.omlib.compatibility.theoneprobe;

import mcjty.theoneprobe.api.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import omtteam.omlib.OMLib;

import javax.annotation.Nullable;

/**
 * Created by Keridos on 18/07/17.
 * This Class
 */
public class TOPCompatibility {
    private static boolean registered;

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        FMLInterModComms.sendFunctionMessage("theoneprobe", "getTheOneProbe", "omtteam.omlib.compatibility.theoneprobe.TOPCompatibility$GetTheOneProbe");
    }

    public static String getLocalizationString(String unloc) {
        return IProbeInfo.STARTLOC + unloc + IProbeInfo.ENDLOC;
    }

    public static class GetTheOneProbe implements com.google.common.base.Function<ITheOneProbe, Void> {
        static ITheOneProbe probe;

        @Nullable
        @Override
        public Void apply(ITheOneProbe theOneProbe) {
            probe = theOneProbe;
            OMLib.getLogger().info("Enabled support for The One Probe");
            probe.registerProvider(new IProbeInfoProvider() {
                @Override
                public String getID() {
                    return "omlib:default";
                }

                @Override
                public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
                    if (blockState.getBlock() instanceof TOPInfoProvider) {
                        TOPInfoProvider provider = (TOPInfoProvider) blockState.getBlock();
                        provider.addProbeInfo(mode, probeInfo, player, world, blockState, data);
                    }
                }
            });
            probe.registerBlockDisplayOverride((mode, probeInfo, player, world, blockState, data) -> {
                if (blockState.getBlock() instanceof TOPInfoModifier) {
                    TOPInfoModifier provider = (TOPInfoModifier) blockState.getBlock();
                    provider.addProbeInfo(mode, probeInfo, player, world, blockState, data);
                    return true;
                }
                return false;
            });
            return null;
        }
    }
}
