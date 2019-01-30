package omtteam.omlib.util.camo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CamoResult {
    private boolean success;
    private EnumTool tool;

    public CamoResult(boolean success, EnumTool tool) {
        this.success = success;
        this.tool = tool;
    }

    @Nonnull
    public boolean isSuccess() {
        return success;
    }

    @Nullable
    public EnumTool getTool() {
        return tool;
    }
}
