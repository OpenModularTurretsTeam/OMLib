package omtteam.omlib.api.render.camo;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import omtteam.omlib.network.MessageCamoSettings;
import omtteam.omlib.network.OMLibNetworkingHandler;
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

    /**
     * The tile entity holding this disguise, usually the implementing tile entity itself. Any tile entity can host a
     * camouflage; ownership is not needed (the permissions are {@link #canSetCamo} and {@link #canChangeLight}).
     */
    @Nonnull
    TileEntity getCamoTileEntity();

    /** Whether camouflage is enabled for this block at all (OMT: config {@code allowBaseCamo}). */
    boolean isCamoEnabled();

    /** Whether light value and opacity of this block can be adjusted (OMT: tier 4 and 5 bases). */
    boolean supportsLight();

    /**
     * Whether the camouflage menu also accepts full blocks that are not opaque (glass, ice, leaves) as a disguise.
     * Off by default: such a disguise shows the world behind the block (OMT's turret bases and OMPD's camouflage trap
     * accept them, as on 1.20.1+).
     */
    default boolean acceptsTransparentDisguises() {
        return false;
    }

    /** Server-side permission check for setting or resetting the disguise. */
    boolean canSetCamo(EntityPlayer player);

    /** Server-side permission check for changing light value and opacity. */
    boolean canChangeLight(EntityPlayer player);

    default void updateCamoSettingsToPlayers() {
        OMLibNetworkingHandler.INSTANCE.sendToAllTracking(new MessageCamoSettings(this),
                                                          new NetworkRegistry.TargetPoint(
                                                                  this.getCamoTileEntity().getWorld().provider.getDimension(),
                                                                  this.getCamoTileEntity().getPos().getX(),
                                                                  this.getCamoTileEntity().getPos().getY(),
                                                                  this.getCamoTileEntity().getPos().getZ(), 20));
    }
}
