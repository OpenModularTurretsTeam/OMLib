package omtteam.omlib.util.camo;

import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public enum EnumTool implements IStringSerializable {
    AXE("axe"),
    PICKAXE("pickaxe"),
    SHOVEL("shovel");

    final String name;

    EnumTool(String name) {
        this.name = name;
    }

    @Nullable
    public static EnumTool byName(@Nullable String name) {
        if (name == null) {
            return null;
        }
        switch (name) {
            case "axe":
                return EnumTool.AXE;
            case "pickaxe":
                return EnumTool.PICKAXE;
            case "shovel":
                return EnumTool.SHOVEL;
            default:
                return null;
        }
    }

    @Override
    @Nonnull
    public String getName() {
        return this.name;
    }
}
