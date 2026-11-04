package omtteam.omlib.item;

import net.minecraft.item.ItemSword;
import omtteam.omlib.reference.OMLibNames;
import omtteam.omlib.reference.Reference;

/**
 * Created by Keridos on 04/02/18.
 * This Class is the weapon for the fake player upgrade; in no creative tab (so not in JEI either).
 */
public class FakeSword extends ItemSword {

    public FakeSword(ToolMaterial material) {
        super(material);
        // only held by the fake player; ItemSword puts every sword into the Combat tab
        this.setCreativeTab(null);
        this.setRegistryName(Reference.MOD_ID, OMLibNames.Items.fakeSword);
        this.setTranslationKey(OMLibNames.Items.fakeSword);
    }
}
