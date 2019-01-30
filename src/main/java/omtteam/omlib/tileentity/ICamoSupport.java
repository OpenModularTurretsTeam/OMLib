package omtteam.omlib.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import omtteam.omlib.network.OMLibNetworkingHandler;
import omtteam.omlib.network.messages.MessageCamoSettings;
import omtteam.omlib.util.camo.CamoSettings;

import javax.annotation.Nonnull;

/**
 * Created by Keridos on 31/01/17.
 * This Class adds the interface functions for camo supporting tile entities.
 * The TE should have a private IBlockState variable that is accessed be these functions.
 */
public interface ICamoSupport {
    /**
     * Returns the blockstate used for rendering the camo for the corresponding TE.
     *
     * @return the camo blockstate used to show the camo
     */
    @Nonnull
    IBlockState getCamoState();

    /**
     * Sets the blockstate used for rendering the camo for the corresponding TE.
     *
     * @param state the camo blockstate used to show the camo
     */
    void setCamoState(IBlockState state);

    /**
     * Returns the default blockstate used for rendering the camo for the corresponding TE.
     *
     * @return the default blockstate to be rendered (without camo)
     */
    @Nonnull
    IBlockState getDefaultCamoState();

    @Nonnull
    CamoSettings getCamoSettings();

    @Nonnull
    TileEntityOwnedBlock getOwnedBlock();

    default void updateCamoSettingsToPlayers() {
        OMLibNetworkingHandler.INSTANCE.sendToAllAround(new MessageCamoSettings((ICamoSupport) this.getOwnedBlock()),
                                                        new NetworkRegistry.TargetPoint(
                                                                this.getOwnedBlock().getWorld().provider.getDimension(),
                                                                this.getOwnedBlock().getPos().getX(),
                                                                this.getOwnedBlock().getPos().getY(),
                                                                this.getOwnedBlock().getPos().getZ(), 160));
    }
}
