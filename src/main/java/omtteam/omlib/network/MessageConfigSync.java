package omtteam.omlib.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.nio.charset.StandardCharsets;

/**
 * Server → client: the server's {@code @Config} values of one mod, as JSON ({@code config.ConfigSync}).
 */
public class MessageConfigSync implements IMessage {
    private String modId;
    private String json;

    public MessageConfigSync() {
    }

    public MessageConfigSync(String modId, String json) {
        this.modId = modId;
        this.json = json;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        modId = ByteBufUtils.readUTF8String(buf);
        byte[] bytes = new byte[buf.readInt()];
        buf.readBytes(bytes);
        json = new String(bytes, StandardCharsets.UTF_8);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, modId);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
    }

    public String getModId() {
        return modId;
    }

    public String getJson() {
        return json;
    }
}
