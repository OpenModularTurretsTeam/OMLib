package omtteam.omlib.items;

import net.minecraft.item.ItemSword;
import omtteam.omlib.reference.OMLibNames;
import omtteam.omlib.reference.Reference;

/**
 * Created by Keridos on 04/02/18.
 * This Class is the weapon for the fake player upgrade.
 */
public class FakeSword extends ItemSword {

    public FakeSword(ToolMaterial material) {
        super(material);
        this.setRegistryName(Reference.MOD_ID, OMLibNames.Items.fakeSword);
        this.setUnlocalizedName(OMLibNames.Items.fakeSword);
    }

    @Override
    public float getDamageVsEntity() {
        return 10F;
    }
}
