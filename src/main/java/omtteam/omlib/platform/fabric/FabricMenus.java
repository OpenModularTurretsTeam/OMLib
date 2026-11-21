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
import net.minecraft.network.FriendlyByteBuf;

/**
 * Fabric extended screen handlers.
 */
public final class FabricMenus implements Menus {
    @Override
    public <T extends AbstractContainerMenu> MenuType<T> createBlockMenuType(MenuCompat.ClientFactory<T> factory) {
        return new ExtendedScreenHandlerType<>(
                (containerId, inventory, buf) -> factory.create(containerId, inventory, buf.readBlockPos()));
    }

    @Override
    public void openBlockMenu(ServerPlayer player, Component title, BlockPos pos, MenuCompat.ServerFactory factory) {
        player.openMenu(new ExtendedScreenHandlerFactory() {
            @Override
            public void writeScreenOpeningData(ServerPlayer opener, FriendlyByteBuf buf) {
                buf.writeBlockPos(pos);
            }

            @Override
            public Component getDisplayName() {
                return title;
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player menuPlayer) {
                return factory.create(containerId, inventory, menuPlayer);
            }
        });
    }
}
