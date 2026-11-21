package omtteam.omlib.platform.fabric;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import omtteam.omlib.compat.MenuCompat;
import omtteam.omlib.platform.Menus;
import org.jetbrains.annotations.NotNull;

/**
 * Fabric extended screen handlers.
 */
public final class FabricMenus implements Menus {
    @Override
    public <T extends AbstractContainerMenu> MenuType<T> createBlockMenuType(MenuCompat.ClientFactory<T> factory) {
        return new ExtendedScreenHandlerType<>(factory::create, BlockPos.STREAM_CODEC);
    }

    @Override
    public void openBlockMenu(ServerPlayer player, Component title, BlockPos pos, MenuCompat.ServerFactory factory) {
        player.openMenu(new ExtendedScreenHandlerFactory<BlockPos>() {
            @Override
            public BlockPos getScreenOpeningData(ServerPlayer opener) {
                return pos;
            }

            @Override
            public @NotNull Component getDisplayName() {
                return title;
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player menuPlayer) {
                return factory.create(containerId, inventory, menuPlayer);
            }
        });
    }
}
