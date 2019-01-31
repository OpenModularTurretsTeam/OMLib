package omtteam.omlib.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import omtteam.omlib.api.render.camo.ICamoSupport;
import omtteam.omlib.api.render.camo.RenderBlockStateContainer;
import omtteam.omlib.util.camo.CamoResult;
import omtteam.omlib.util.camo.EnumTool;
import omtteam.omlib.util.player.PlayerUtil;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import static omtteam.omlib.util.GeneralUtil.safeLocalize;
import static omtteam.omlib.util.player.PlayerUtil.addChatMessage;

/**
 * Created by Keridos on 31/01/17.
 * This Class
 */
public abstract class BlockAbstractCamoTileEntity extends BlockAbstractTileEntity {
    public static final IUnlistedProperty<RenderBlockStateContainer> RENDERBLOCKSTATE = new IUnlistedProperty<RenderBlockStateContainer>() {

        public String getName() {
            return "omlib:camo_property";
        }

        @Override
        public boolean isValid(RenderBlockStateContainer value) {
            return true;
        }

        @Override
        public Class<RenderBlockStateContainer> getType() {
            return RenderBlockStateContainer.class;
        }

        @Override
        public String valueToString(RenderBlockStateContainer value) {
            return value.toString();
        }
    };

    @SuppressWarnings("SameParameterValue")
    protected BlockAbstractCamoTileEntity(Material material) {
        super(material);
    }

    @Override
    @Nonnull
    @ParametersAreNonnullByDefault
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        ICamoSupport camoTE = (ICamoSupport) world.getTileEntity(pos);
        if (camoTE != null && state.getBlock() instanceof BlockAbstractCamoTileEntity && state instanceof IExtendedBlockState) {
            IBlockState camoState = camoTE.getCamoState();
            return ((IExtendedBlockState) state)
                    .withProperty(RENDERBLOCKSTATE, new RenderBlockStateContainer(camoState));
        } else if (camoTE != null && !(state instanceof IExtendedBlockState)) {
            return state.getBlock().getExtendedState(state, world, pos);
        }
        return (ForgeRegistries.BLOCKS.getValue(
                new ResourceLocation("minecraft:grass"))).getDefaultState();
    }

    protected String getDefaultTool() {
        return "";
    }

    protected CamoResult handleCamoActivation(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (hand == EnumHand.MAIN_HAND) {
            ItemStack heldItem = player.getHeldItemMainhand();
            ICamoSupport te = (ICamoSupport) world.getTileEntity(pos);
            // Reset Camo
            if (player.isSneaking() && heldItem == ItemStack.EMPTY) {
                if (te != null) {
                    if (PlayerUtil.isPlayerOwner(player, te.getOwnedBlock())) {
                        te.setCamoState(state);
                        return new CamoResult(true, EnumTool.byName(this.getDefaultTool()));
                    } else {
                        addChatMessage(player, new TextComponentString(safeLocalize("status.ownership")));
                    }
                }
            }
            // Camo preparation
            Block heldBlock = null;
            IBlockState camoState = null;
            if (heldItem != ItemStack.EMPTY) {
                heldBlock = Block.getBlockFromItem(heldItem.getItem());
                camoState = heldBlock.getStateForPlacement(world, pos, side.getOpposite(), hitX, hitY, hitZ, heldItem.getMetadata(), player, hand);
            }
            // Camo set
            if (!player.isSneaking() && heldItem != ItemStack.EMPTY && heldItem.getItem() instanceof ItemBlock
                    && heldBlock.isNormalCube(camoState, world, pos)
                    && !heldBlock.hasTileEntity(camoState)
                    && !(heldBlock instanceof BlockAbstractCamoTileEntity)) {
                if (te != null) {
                    if (PlayerUtil.isPlayerOwner(player, te.getOwnedBlock())) {
                        te.setCamoState(camoState);
                        return new CamoResult(true, EnumTool.byName(camoState.getBlock().getHarvestTool(camoState)));
                    } else {
                        addChatMessage(player, new TextComponentString(safeLocalize("status.ownership")));
                    }
                }
            }
        }
        return new CamoResult(false, null);
    }

    @Override
    @Nonnull
    @SideOnly(Side.CLIENT)
    @ParametersAreNonnullByDefault
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return true;
    }
}
