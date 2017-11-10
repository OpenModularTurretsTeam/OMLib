package omtteam.omlib.reference;

import static omtteam.omlib.compatibility.ModCompatibility.*;

public class Reference {
    public static final String MOD_ID = "omlib";
    public static final String NAME = "OMLib";
    public static final String VERSION = "@VERSION@";
    public static final String DEPENDENCIES = "after:" + TEModID + ";after:" + CCModID + ";after:" + MekModID +
            ";after:" + EIOModID + ";after:" + TCModID + ";after:" + OCModID;
}