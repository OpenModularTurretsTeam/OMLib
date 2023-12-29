//package com.ommods.omlib.util.permission;
//
//
//import net.minecraftforge.common.capabilities.Capability;
//
//import net.minecraftforge.common.capabilities.CapabilityManager;
//import net.minecraftforge.common.capabilities.ICapabilityProvider;
//
//
//import javax.annotation.Nonnull;
//import javax.annotation.Nullable;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Map;
//
//
///**
// * Created by Keridos on 17/05/17.
// * This Class
// */
//public class OwnerShareRegister implements ICapabilityProvider {
//    public final static @Nonnull
//    OwnerShareRegister instance = new OwnerShareRegister();
//    private final static @Nonnull
//    ResourceLocation CAP_KEY = new ResourceLocation(Reference.MOD_ID, "owner_share");
//    @SuppressWarnings("CanBeFinal")
//    @CapabilityInject(IOwnerShareRegister.class)
//    public static Capability<IOwnerShareRegister> OWNER_SHARE_REGISTER = null;
//    private HashMap<Player, ArrayList<Player>> ownerShareMap;
//
//    private OwnerShareRegister() {
//        ownerShareMap = new HashMap<>();
//    }
//
//    @SubscribeEvent
//    public static void onWorldCaps(AttachCapabilitiesEvent<World> event) {
//        if (OWNER_SHARE_REGISTER != null && !event.getObject().isRemote && event.getObject().provider.getDimension() == 0) {
//            event.addCapability(CAP_KEY, instance);
//        }
//    }
//
//    public static void preInit() {
//        CapabilityManager.INSTANCE.register(IOwnerShareRegister.class, new Capability.IStorage<IOwnerShareRegister>() {
//            @Override
//            public NBTBase writeNBT(Capability<IOwnerShareRegister> capability, IOwnerShareRegister instanceIn, EnumFacing side) {
//                return instanceIn.serializeNBT();
//            }
//
//            @Override
//            public void readNBT(Capability<IOwnerShareRegister> capability, IOwnerShareRegister instanceIn, EnumFacing side, NBTBase nbt) {
//                instanceIn.deserializeNBT(nbt);
//            }
//        }, () -> OwnerShareRegister.instance);
//    }
//
//    @Override
//    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
//        return capability == OWNER_SHARE_REGISTER;
//    }
//
//    @SuppressWarnings("unchecked")
//    @Override
//    @Nullable
//    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
//        return (T) (capability == OWNER_SHARE_REGISTER ? this : null);
//    }
//
//    @Override
//    public NBTBase serializeNBT() {
//        NBTTagCompound nbt = new NBTTagCompound();
//        NBTTagList list = new NBTTagList();
//
//        for (Map.Entry<Player, ArrayList<Player>> entry : ownerShareMap.entrySet()) {
//            NBTTagCompound tag = new NBTTagCompound();
//            NBTTagList listPlayers = new NBTTagList();
//            entry.getKey().writeToNBT(tag);
//            for (Player player : entry.getValue()) {
//                listPlayers.appendTag(player.writeToNBT(new NBTTagCompound()));
//            }
//            tag.setTag("share_players", listPlayers);
//            list.appendTag(tag);
//        }
//        nbt.setTag("list", list);
//
//        return nbt;
//    }
//
//    @Override
//    public void deserializeNBT(NBTBase nbtIn) {
//        if (nbtIn instanceof NBTTagCompound) {
//            NBTTagCompound nbt = (NBTTagCompound) nbtIn;
//            NBTTagList list = nbt.getTagList("list", nbt.getId());
//            for (int i = 0; i < list.tagCount(); i++) {
//                NBTTagCompound tag = list.getCompoundTagAt(i);
//                Player player = Player.readFromNBT(tag);
//                NBTTagList playerTagList = tag.getTagList("share_players", tag.getId());
//                ArrayList<Player> sharePlayers = new ArrayList<>();
//                for (int j = 0; j < playerTagList.tagCount(); j++) {
//                    Player tempPlayer = Player.readFromNBT(playerTagList.getCompoundTagAt(j));
//                    sharePlayers.add(tempPlayer);
//                }
//                ownerShareMap.put(player, sharePlayers);
//            }
//        } else {
//            OMLib.getLogger().debug("failed to deserialize NBT");
//        }
//    }
//
//    public HashMap<Player, ArrayList<Player>> getOwnerShareMap() {
//        return ownerShareMap;
//    }
//
//    public void setOwnerShareMap(@Nullable HashMap<Player, ArrayList<Player>> ownerShareMap) {
//        if (ownerShareMap != null) {
//            this.ownerShareMap = ownerShareMap;
//        }
//    }
//
//    @Nullable
//    public Map.Entry<Player, ArrayList<Player>> getEntryFromName(String name) {
//        for (Map.Entry<Player, ArrayList<Player>> entry : ownerShareMap.entrySet()) {
//            if (entry.getKey().getName().equals(name)) {
//                return entry;
//            }
//        }
//        return null;
//    }
//
//    @Nullable
//    private Map.Entry<Player, ArrayList<Player>> getEntry(Player player) {
//        for (Map.Entry<Player, ArrayList<Player>> entry : ownerShareMap.entrySet()) {
//            if (entry.getKey().equals(player)) {
//                return entry;
//            }
//        }
//        return null;
//    }
//
//    public void addSharePlayer(Player owner, Player sharePlayer, @Nullable ICommandSender sender) {
//        Map.Entry<Player, ArrayList<Player>> entryFound = null;
//        if (owner.equals(sharePlayer)) {
//            if (sender != null) {
//                sender.sendMessage(new TextComponentString("Cannot add yourself to the list!"));
//            }
//            return;
//        }
//        for (Map.Entry<Player, ArrayList<Player>> entry : ownerShareMap.entrySet()) {
//            if (entry.getKey().equals(owner)) {
//                entryFound = entry;
//            }
//        }
//        if (entryFound == null) {
//            ArrayList<Player> list = new ArrayList<>();
//            list.add(sharePlayer);
//            if (sender != null) {
//                sender.sendMessage(new TextComponentString("Added player " + sharePlayer.getName() + " to your Share List!"));
//            }
//            ownerShareMap.put(owner, list);
//        } else {
//            if (!entryFound.getValue().contains(sharePlayer)) {
//                entryFound.getValue().add(sharePlayer);
//                if (sender != null) {
//                    sender.sendMessage(new TextComponentString("Added player " + sharePlayer.getName() + " to your Share List!"));
//                }
//            } else {
//                if (sender != null) {
//                    sender.sendMessage(new TextComponentString("Error while adding " + sharePlayer.getName() + " to your Share List! Already on list!"));
//                }
//            }
//        }
//
//        OMLibNetworkingHandler.sendMessageToAllPlayers(new MessageSetSharePlayerList(this));
//    }
//
//    public void removeSharePlayer(Player owner, Player sharePlayer, @Nullable ICommandSender sender) {
//        Map.Entry<Player, ArrayList<Player>> entryFound = null;
//        if (owner.equals(sharePlayer)) {
//            if (sender != null) {
//                sender.sendMessage(new TextComponentString("You cannot add/remove yourself to/from your Share List!"));
//            }
//            return;
//        }
//        for (Map.Entry<Player, ArrayList<Player>> entry : ownerShareMap.entrySet()) {
//            if (entry.getKey().equals(owner)) {
//                entryFound = entry;
//            }
//        }
//        if (entryFound != null) {
//            int sizeBefore = entryFound.getValue().size();
//            Predicate<Player> predicate = p -> p.equals(sharePlayer);
//            entryFound.getValue().removeIf(predicate);
//            if (sender != null) {
//                if (entryFound.getValue().size() < sizeBefore) {
//                    sender.sendMessage(new TextComponentString("Removed player " + sharePlayer.getName() + " from your Share List!"));
//                } else {
//                    sender.sendMessage(new TextComponentString("Could not remove player " + sharePlayer.getName() + " from your Share List!"));
//                }
//            }
//        } else if (sender != null) {
//            sender.sendMessage(new TextComponentString("Could not remove player " + sharePlayer.getName() + " from your Share List!"));
//        }
//        OMLibNetworkingHandler.sendMessageToAllPlayers(new MessageSetSharePlayerList(this));
//    }
//
//    public void printSharePlayers(Player owner, ICommandSender sender) {
//        StringBuilder playerList = new StringBuilder();
//        Map.Entry<Player, ArrayList<Player>> entryFound = null;
//        for (Map.Entry<Player, ArrayList<Player>> entry : ownerShareMap.entrySet()) {
//            if (entry.getKey().equals(owner)) {
//                entryFound = entry;
//            }
//        }
//        if (entryFound != null) {
//            ArrayList<Player> list = entryFound.getValue();
//            for (int i = 0; i < list.size(); i++) {
//                Player player = list.get(i);
//                if (i < list.size() - 1) {
//                    playerList.append(player.getName());
//                    playerList.append(", ");
//                } else {
//                    playerList.append(player.getName());
//                }
//            }
//        }
//        sender.sendMessage(new TextComponentString("Players on your share list: " + playerList.toString()));
//    }
//
//    public boolean isPlayerSharedOwner(Player owner, Player shareCheck) {
//        Map.Entry<Player, ArrayList<Player>> entry = getEntry(owner);
//        if (entry != null) {
//            for (Player player : entry.getValue()) {
//                if (player.equals(shareCheck)) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }
//}