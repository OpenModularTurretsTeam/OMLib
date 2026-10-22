package omtteam.omlib.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import omtteam.omlib.api.permission.OwnerShareRegister;
import omtteam.omlib.util.player.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nico on 23/05/17.
 */
public class MessageSetSharePlayerList implements IMessage {
    private HashMap<Player, ArrayList<Player>> ownerShareMap;

    public MessageSetSharePlayerList() {
    }

    public MessageSetSharePlayerList(OwnerShareRegister shareHandler) {
        this.ownerShareMap = shareHandler.getOwnerShareMap();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        ownerShareMap = new HashMap<>();
        int lengthofMap = buf.readInt();
        for (int i = 0; i < lengthofMap; i++) {
            Player owner = Player.readFromByteBuf(buf);
            ArrayList<Player> sharePlayerList = new ArrayList<>();
            int lengthOfPlayerList = buf.readInt();
            if (lengthOfPlayerList > 0) {
                for (int j = 0; j < lengthOfPlayerList; j++) {
                    sharePlayerList.add(Player.readFromByteBuf(buf));
                }
            }
            ownerShareMap.put(owner, sharePlayerList);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(ownerShareMap.size());
        for (Map.Entry<Player, ArrayList<Player>> entry : ownerShareMap.entrySet()) {
            Player owner = entry.getKey();
            Player.writeToByteBuf(owner, buf);
            buf.writeInt(entry.getValue().size());
            if (entry.getValue().size() > 0) {
                for (Player player : entry.getValue()) {
                    Player.writeToByteBuf(player, buf);
                }
            }
        }
    }

    public HashMap<Player, ArrayList<Player>> getOwnerShareMap() {
        return ownerShareMap;
    }
}
