package omtteam.omlib.util.camo;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;

import static omtteam.omlib.util.BlockUtil.getBlockStateFromNBT;
import static omtteam.omlib.util.BlockUtil.writeBlockFromStateToNBT;

public class CamoSettings {
    protected IBlockState camoBlockState;
    protected int lightValue = 0;
    protected int lightOpacity = 15;

    public static CamoSettings getSettingsFromNBT(NBTTagCompound tag) {
        CamoSettings settings = new CamoSettings();
        settings.setCamoBlockState(getBlockStateFromNBT(tag));
        settings.setLightValue(tag.getInteger("light_value"));
        settings.setLightOpacity(tag.getInteger("light_opacity"));
        return settings;
    }

    public IBlockState getCamoBlockState() {
        return camoBlockState;
    }

    public void setCamoBlockState(IBlockState camoBlockState) {
        this.camoBlockState = camoBlockState;
    }

    public int getLightValue() {
        return lightValue;
    }

    public void setLightValue(int lightValue) {
        this.lightValue = lightValue;
    }

    public int getLightOpacity() {
        return lightOpacity;
    }

    public void setLightOpacity(int lightOpacity) {
        this.lightOpacity = lightOpacity;
    }

    public void writeNBT(NBTTagCompound tag) {
        writeBlockFromStateToNBT(tag, this.camoBlockState);
        tag.setInteger("light_value", this.lightValue);
        tag.setInteger("light_opacity", this.lightOpacity);
    }
}
