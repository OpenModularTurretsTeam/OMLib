package omtteam.omlib.api.permission;

import net.minecraft.command.ICommandSender;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import omtteam.omlib.OMLib;
import omtteam.omlib.handler.OMConfig;
import omtteam.omlib.network.OMLibNetworkingHandler;
import omtteam.omlib.network.messages.MessageSetGlobalTrustList;
import omtteam.omlib.reference.Reference;
import omtteam.omlib.util.player.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Keridos on 17/05/17.
 * This Class
 */
@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class GlobalTrustRegister implements ICapabilityProvider, IGlobalTrustRegister {
    public final static @Nonnull
    GlobalTrustRegister instance = new GlobalTrustRegister();
    private final static @Nonnull
    ResourceLocation CAP_KEY = new ResourceLocation(Reference.MOD_ID, "owner_global_trust");
    @SuppressWarnings("CanBeFinal")
    @CapabilityInject(IGlobalTrustRegister.class)
    public static Capability<IGlobalTrustRegister> GLOBAL_TRUST_REGISTER = null;
    private HashMap<Player, TrustedPlayersManager> globalTrustList;

    private GlobalTrustRegister() {
        globalTrustList = new HashMap<>();
    }

    @SubscribeEvent
    public static void onWorldCaps(AttachCapabilitiesEvent<World> event) {
        if (GLOBAL_TRUST_REGISTER != null && !event.getObject().isRemote && event.getObject().provider.getDimension() == 1) {
            event.addCapability(CAP_KEY, instance);
        }
    }

    public static void preInit() {
        CapabilityManager.INSTANCE.register(IGlobalTrustRegister.class, new Capability.IStorage<IGlobalTrustRegister>() {
            @Override
            public NBTBase writeNBT(Capability<IGlobalTrustRegister> capability, IGlobalTrustRegister instanceIn, EnumFacing side) {
                return instanceIn.serializeNBT();
            }

            @Override
            public void readNBT(Capability<IGlobalTrustRegister> capability, IGlobalTrustRegister instanceIn, EnumFacing side, NBTBase nbt) {
                instanceIn.deserializeNBT(nbt);
            }
        }, () -> GlobalTrustRegister.instance);
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == GLOBAL_TRUST_REGISTER;
    }

    @SuppressWarnings("unchecked")
    @Override
    @Nullable
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return (T) (capability == GLOBAL_TRUST_REGISTER ? this : null);
    }

    @Override
    public NBTBase serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        NBTTagList list = new NBTTagList();
        for (Map.Entry<Player, TrustedPlayersManager> entry : globalTrustList.entrySet()) {
            NBTTagCompound tag = new NBTTagCompound();
            entry.getKey().writeToNBT(tag);
            entry.getValue().writeToNBT(tag);
            list.appendTag(tag);
        }
        nbt.setTag("list", list);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTBase nbtIn) {
        if (nbtIn instanceof NBTTagCompound) {
            NBTTagCompound nbt = (NBTTagCompound) nbtIn;
            NBTTagList list = nbt.getTagList("list", nbt.getId());
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound tag = list.getCompoundTagAt(i);
                Player owner = Player.readFromNBT(tag);
                TrustedPlayersManager tpm = new TrustedPlayersManager(owner);
                tpm.readFromNBT(tag);
                globalTrustList.put(owner, tpm);
            }
        } else {
            OMLib.getLogger().debug("failed to deserialize NBT");
        }
    }

    public HashMap<Player, TrustedPlayersManager> getGlobalTrustList() {
        return globalTrustList;
    }

    public void setGlobalTrustList(@Nullable HashMap<Player, TrustedPlayersManager> ownerShareMap) {
        if (ownerShareMap != null) {
            this.globalTrustList = ownerShareMap;
        }
    }

    @Nullable
    public Map.Entry<Player, TrustedPlayersManager> getEntryFromName(String name) {
        for (Map.Entry<Player, TrustedPlayersManager> entry : globalTrustList.entrySet()) {
            if (entry.getKey().getName().equals(name)) {
                return entry;
            }
        }
        return null;
    }

    @Nullable
    private Map.Entry<Player, TrustedPlayersManager> getEntry(Player player) {
        for (Map.Entry<Player, TrustedPlayersManager> entry : globalTrustList.entrySet()) {
            if (entry.getKey().equals(player)) {
                return entry;
            }
        }
        return null;
    }

    public void addTrustedPlayer(Player owner, Player sharePlayer, EnumAccessLevel accessMode, @Nullable ICommandSender sender) {
        Map.Entry<Player, TrustedPlayersManager> entry = getEntry(owner);
        if (owner.equals(sharePlayer)) {
            return;
        }
        if (entry == null) {
            TrustedPlayersManager tpm = new TrustedPlayersManager(owner);
            TrustedPlayer trustedPlayer = new TrustedPlayer(sharePlayer);
            trustedPlayer.setAccessLevel(accessMode);
            if (tpm.addTrustedPlayer(trustedPlayer)) {
                if (sender != null) {
                    sender.sendMessage(new TextComponentString("Added player " + sharePlayer.getName() + " to your global trust List!"));
                }
            } else {
                if (sender != null) {
                    sender.sendMessage(new TextComponentString("Could not add " + sharePlayer.getName() + " to your global trust List!"));
                }
            }
            globalTrustList.put(owner, tpm);
        } else {
            if (entry.getValue().addTrustedPlayer(sharePlayer.getName())) {
                entry.getValue().getTrustedPlayer(sharePlayer.getName()).setAccessLevel(accessMode);
                if (sender != null) {
                    sender.sendMessage(new TextComponentString("Added player " + sharePlayer.getName() + " to your global trust List!"));
                }
            } else {
                if (sender != null) {
                    sender.sendMessage(new TextComponentString("Could not add " + sharePlayer.getName() + " to your global trust List!"));
                }
            }
        }
        OMLibNetworkingHandler.sendMessageToAllPlayers(new MessageSetGlobalTrustList(this));
    }

    public void removeTrustedPlayer(Player owner, Player trustedPlayer, @Nullable ICommandSender sender) {
        Map.Entry<Player, TrustedPlayersManager> entry = getEntry(owner);
        if (owner.equals(trustedPlayer)) {
            if (sender != null) {
                sender.sendMessage(new TextComponentString("You cannot add/remove yourself to/from your Share List!"));
            }
            return;
        }
        if (entry != null) {
            boolean result = entry.getValue().removeTrustedPlayer(trustedPlayer.getName());
            if (sender != null) {
                if (result) {
                    sender.sendMessage(new TextComponentString("Removed player " + trustedPlayer.getName() + " from your Share List!"));
                } else {
                    sender.sendMessage(new TextComponentString("Could not remove player " + trustedPlayer.getName() + " from your Share List!"));
                }
            }
        } else if (sender != null) {
            sender.sendMessage(new TextComponentString("Could not remove player " + trustedPlayer.getName() + " from your Share List!"));
        }
        OMLibNetworkingHandler.sendMessageToAllPlayers(new MessageSetGlobalTrustList(this));
    }

    public void printTrustedPlayers(Player owner, ICommandSender sender) {
        StringBuilder playerList = new StringBuilder();
        Map.Entry<Player, TrustedPlayersManager> entry = getEntry(owner);
        if (entry != null) {
            TrustedPlayersManager tpm = entry.getValue();
            for (TrustedPlayer trustedPlayer : tpm.getTrustedPlayers()) {
                playerList.append(trustedPlayer.getName()).append(" ");
            }
        }
        sender.sendMessage(new TextComponentString("Players on your share list: " + playerList.toString()));
    }

    @Nullable
    public TrustedPlayer getTrustedPlayer(Player owner, Player toCheck) {
        Map.Entry<Player, TrustedPlayersManager> entry = getEntry(owner);
        if (entry != null) {
            for (TrustedPlayer trustedPlayer : entry.getValue().getTrustedPlayers()) {
                if ((OMConfig.GENERAL.offlineModeSupport && trustedPlayer.getName().equals(toCheck.getName())) ||
                        (trustedPlayer.getUuid() != null && trustedPlayer.getUuid().equals(toCheck.getUuid()))) {
                    return trustedPlayer;
                }
            }
        }
        return null;
    }

    @Nullable
    public TrustedPlayer getTrustedPlayer(Player owner, String toCheck) {
        Map.Entry<Player, TrustedPlayersManager> entry = getEntry(owner);
        if (entry != null) {
            for (TrustedPlayer trustedPlayer : entry.getValue().getTrustedPlayers()) {
                if (trustedPlayer.getName().equals(toCheck)) {
                    return trustedPlayer;
                }
            }
        }
        return null;
    }

    @Nullable
    public TrustedPlayer getTrustedPlayer(Player owner, UUID uuid) {
        Map.Entry<Player, TrustedPlayersManager> entry = getEntry(owner);
        if (entry != null) {
            for (TrustedPlayer trustedPlayer : entry.getValue().getTrustedPlayers()) {
                if (trustedPlayer.getUuid().equals(uuid)) {
                    return trustedPlayer;
                }
            }
        }
        return null;
    }

    public boolean changePermission(Player owner, String player, EnumAccessLevel level) {
        Map.Entry<Player, TrustedPlayersManager> entry = getEntry(owner);
        if (entry != null) {
            for (TrustedPlayer trustedPlayer : entry.getValue().getTrustedPlayers()) {
                if (trustedPlayer.getName().equalsIgnoreCase(player)) {
                    trustedPlayer.setAccessLevel(level);
                    return true;
                }
            }
        }
        return false;
    }
}
