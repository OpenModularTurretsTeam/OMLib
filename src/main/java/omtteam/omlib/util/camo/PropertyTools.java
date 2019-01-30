package omtteam.omlib.util.camo;

import net.minecraft.block.properties.PropertyEnum;

import javax.annotation.Nonnull;
import java.util.Collection;

public class PropertyTools extends PropertyEnum<EnumTool> {

    public PropertyTools(String name, Class<EnumTool> valueClass, Collection<EnumTool> allowedValues) {
        super(name, valueClass, allowedValues);
    }

    @Override
    @Nonnull
    public String getName() {
        return "tools";
    }
}
