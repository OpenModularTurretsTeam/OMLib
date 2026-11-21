"""Unit tests of resolve_versions.resolve_text.

Run: cd tools && python3 -m unittest test_resolve_versions -v

The fixtures are snippets of the real OMLib, OMT and OML sources, copied verbatim. The sources on disk are in the
1.21.1-fabric state; the 1.20.1 and 1.21.11 inputs are the Stonecutter states of the 1.20.1-fabric and 1.21.11-fabric
nodes (an inactive branch is wrapped as `/*` ... `*/`, nested block comments are escaped as `/^* ... ^/`).
Each input is only valid for the branch it is named after, which is what the error tests rely on.
"""
import re
import unittest

from resolve_versions import ResolveError, resolve_text, tokenize

B1201 = ((1, 20, 1), ("fabric", "forge"))
B1211 = ((1, 21, 1), ("fabric", "neoforge"))
B12111 = ((1, 21, 11), ("fabric", "neoforge"))
BRANCHES = {"1.20.1": B1201, "1.21.1": B1211, "1.21.11": B12111}
VERSION_PREDICATE = re.compile(r"//\?.*(>=|<)\d")


def t(block):
    """A triple-quoted block starts and ends with one newline that is not part of the text."""
    return block[1:-1]


def j(*blocks):
    return "\n".join(t(block) for block in blocks)


def resolve(text, branch, name="<test>"):
    mc, loaders = branch
    return resolve_text(text, mc, loaders, name)[0]


# --- fixtures: OMLib VersionCompat.java 42-57 -----------------------------------------------------------------------

VC_FABRIC_BUILDER_COMMENTED = """
//? if fabric && >=1.21.11 {
/*import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
*///?}
"""
VC_FABRIC_BUILDER_ACTIVE = """
//? if fabric && >=1.21.11 {
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
//?}
"""
VC_FABRIC_BUILDER_LOADER = """
//? if fabric {
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
//?}
"""
VC_NEOFORGE = """
//? if neoforge {
/*import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
*///?}
"""
VC_FORGE = """
//? if forge {
/*import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
*///?}
"""
VC_IMPORTS_ACTIVE = """
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
"""
VC_IMPORTS_GE_121_COMMENTED = """
//? if >=1.21 {
/*import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
*///?}
"""
VC_IMPORTS_GE_121_ACTIVE = """
//? if >=1.21 {
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
//?}
"""

VC_IMPORTS = {
    "1.20.1": (j(VC_FABRIC_BUILDER_COMMENTED, VC_NEOFORGE, VC_FORGE, VC_IMPORTS_GE_121_COMMENTED),
               j(VC_FORGE)),
    "1.21.1": (j(VC_FABRIC_BUILDER_COMMENTED, VC_NEOFORGE, VC_FORGE, VC_IMPORTS_GE_121_ACTIVE),
               j(VC_NEOFORGE, VC_IMPORTS_ACTIVE)),
    "1.21.11": (j(VC_FABRIC_BUILDER_ACTIVE, VC_NEOFORGE, VC_FORGE, VC_IMPORTS_GE_121_ACTIVE),
                j(VC_FABRIC_BUILDER_LOADER, VC_NEOFORGE, VC_IMPORTS_ACTIVE)),
}

# --- OMLib VersionCompat.java 100-115 (two if/else chains, code in between) -----------------------------------------

VC_ID_BETWEEN = """
    }

    // ---- Registration (1.21.11 needs the registry id in the properties before a block or item is created) ----

    /** Properties of the block that will be registered as {@code id}. */
    public static BlockBehaviour.Properties blockProperties(BlockBehaviour.Properties properties, ResourceLocation id) {
"""

VC_ID = {
    "1.20.1": (
        j("""
        //? if >=1.21 {
        /*return ResourceLocation.fromNamespaceAndPath(namespace, path);
        *///?} else {
        return new ResourceLocation(namespace, path);
        //?}
""", VC_ID_BETWEEN, """
        //? if >=1.21.11 {
        /*return properties.setId(ResourceKey.create(Registries.BLOCK, id));
        *///?} else {
        return properties;
        //?}
"""),
        j("""
        return new ResourceLocation(namespace, path);
""", VC_ID_BETWEEN, """
        return properties;
""")),
    "1.21.1": (
        j("""
        //? if >=1.21 {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
        //?} else {
        /*return new ResourceLocation(namespace, path);
        *///?}
""", VC_ID_BETWEEN, """
        //? if >=1.21.11 {
        /*return properties.setId(ResourceKey.create(Registries.BLOCK, id));
        *///?} else {
        return properties;
        //?}
"""),
        j("""
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
""", VC_ID_BETWEEN, """
        return properties;
""")),
    "1.21.11": (
        j("""
        //? if >=1.21 {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
        //?} else {
        /*return new ResourceLocation(namespace, path);
        *///?}
""", VC_ID_BETWEEN, """
        //? if >=1.21.11 {
        return properties.setId(ResourceKey.create(Registries.BLOCK, id));
        //?} else {
        /*return properties;
        *///?}
"""),
        j("""
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
""", VC_ID_BETWEEN, """
        return properties.setId(ResourceKey.create(Registries.BLOCK, id));
""")),
}

# --- OMLib VersionCompat.java 145-151: `fabric && >=1.21.11` / else if `>=1.21.11` / else ---------------------------

VC_BUILDER_ELSE_ACTIVE = """
        return BlockEntityType.Builder.of(factory::apply, blocks).build(null);
"""

VC_BUILDER = {
    "1.20.1": (
        j("""
        //? if fabric && >=1.21.11 {
        /*return FabricBlockEntityTypeBuilder.create(factory::apply, blocks).build();
        *///?} else if >=1.21.11 {
        /*return new BlockEntityType<>(factory::apply, blocks);
        *///?} else {
        return BlockEntityType.Builder.of(factory::apply, blocks).build(null);
        //?}
"""),
        j(VC_BUILDER_ELSE_ACTIVE)),
    "1.21.1": (
        j("""
        //? if fabric && >=1.21.11 {
        /*return FabricBlockEntityTypeBuilder.create(factory::apply, blocks).build();
        *///?} else if >=1.21.11 {
        /*return new BlockEntityType<>(factory::apply, blocks);
        *///?} else {
        return BlockEntityType.Builder.of(factory::apply, blocks).build(null);
        //?}
"""),
        j(VC_BUILDER_ELSE_ACTIVE)),
    "1.21.11": (
        j("""
        //? if fabric && >=1.21.11 {
        return FabricBlockEntityTypeBuilder.create(factory::apply, blocks).build();
        //?} else if >=1.21.11 {
        /*return new BlockEntityType<>(factory::apply, blocks);
        *///?} else {
        /*return BlockEntityType.Builder.of(factory::apply, blocks).build(null);
        *///?}
"""),
        j("""
        //? if fabric {
        return FabricBlockEntityTypeBuilder.create(factory::apply, blocks).build();
        //?} else {
        /*return new BlockEntityType<>(factory::apply, blocks);
        *///?}
""")),
}

# --- OMLib CompatBlockEntity.java 14-33 ---------------------------------------------------------------------------

CBE_HEAD = """
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
"""
CBE_MODEL_DATA_CHAIN = """
//? if neoforge && >=1.21.11 {
/*import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.model.data.ModelProperty;
*///?} else if neoforge {
/*import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
*///?}
"""
CBE_FORGE = """
//? if forge {
/*import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
*///?}
"""
CBE_REGISTRY_ACCESS_COMMENTED = """
//? if <1.21 || >=1.21.11 {
/*import net.minecraft.core.RegistryAccess;
*///?}
"""
CBE_REGISTRY_ACCESS_ACTIVE = """
//? if <1.21 || >=1.21.11 {
import net.minecraft.core.RegistryAccess;
//?}
"""
CBE_VALUE_IO_COMMENTED = """
//? if >=1.21.11 {
/*import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
*///?}
"""
CBE_VALUE_IO_ACTIVE = """
//? if >=1.21.11 {
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
//?}
"""
CBE_NEOFORGE_OLD_LOADER = """
//? if neoforge {
/*import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
*///?}
"""
CBE_NEOFORGE_NEW_LOADER = """
//? if neoforge {
/*import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.model.data.ModelProperty;
*///?}
"""

CBE_IMPORTS = {
    "1.20.1": (
        j(CBE_HEAD, CBE_MODEL_DATA_CHAIN, CBE_FORGE, CBE_REGISTRY_ACCESS_ACTIVE, CBE_VALUE_IO_COMMENTED),
        j(CBE_HEAD, CBE_FORGE, """
import net.minecraft.core.RegistryAccess;
""")),
    "1.21.1": (
        j(CBE_HEAD, CBE_MODEL_DATA_CHAIN, CBE_FORGE, CBE_REGISTRY_ACCESS_COMMENTED, CBE_VALUE_IO_COMMENTED),
        j(CBE_HEAD, CBE_NEOFORGE_OLD_LOADER)),
    "1.21.11": (
        j(CBE_HEAD, CBE_MODEL_DATA_CHAIN, CBE_FORGE, CBE_REGISTRY_ACCESS_ACTIVE, CBE_VALUE_IO_ACTIVE),
        j(CBE_HEAD, CBE_NEOFORGE_NEW_LOADER, """
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
""")),
}

# --- OMLib CompatBlockEntity.java 43-46: `neoforge || forge` around an escaped comment -------------------------------

CBE_RENDER_DATA_BODY = """
    /*/^* The {@link #renderData()} in the block entity's model data on NeoForge and Forge. ^/
    public static final ModelProperty<Object> RENDER_DATA = new ModelProperty<>();
    *///?}
"""
CBE_RENDER_DATA_IN = j("""
public abstract class CompatBlockEntity extends BlockEntity {
    //? if neoforge || forge {
""", CBE_RENDER_DATA_BODY)

CBE_RENDER_DATA = {
    "1.20.1": (CBE_RENDER_DATA_IN, j("""
public abstract class CompatBlockEntity extends BlockEntity {
    //? if forge {
""", CBE_RENDER_DATA_BODY)),
    "1.21.1": (CBE_RENDER_DATA_IN, j("""
public abstract class CompatBlockEntity extends BlockEntity {
    //? if neoforge {
""", CBE_RENDER_DATA_BODY)),
    "1.21.11": (CBE_RENDER_DATA_IN, j("""
public abstract class CompatBlockEntity extends BlockEntity {
    //? if neoforge {
""", CBE_RENDER_DATA_BODY)),
}

# --- OMLib Platform.java 69-76 and ClientPlatform.java 44-50: `fabric` / `neoforge` / else --------------------------

PLATFORM_IN = j("""
    private static PlatformServices loaderServices() {
        //? if fabric {
        return new PlatformServices(new FabricLoaderInfo(), new FabricFuel(), new FabricNetwork(), new FabricMenus(),
                new FabricEnergy());
        //?} else if neoforge {
        /*return new PlatformServices(new NeoForgeLoaderInfo(), new NeoForgeFuel(), new NeoForgeNetwork(),
                new NeoForgeMenus(), new NeoForgeEnergy());
        *///?} else {
        /*return new PlatformServices(new ForgeLoaderInfo(), new ForgeFuel(), new ForgeNetwork(), new ForgeMenus(),
                new ForgeEnergy());
        *///?}
    }
""")
PLATFORM_FABRIC_HEAD = """
    private static PlatformServices loaderServices() {
        //? if fabric {
        return new PlatformServices(new FabricLoaderInfo(), new FabricFuel(), new FabricNetwork(), new FabricMenus(),
                new FabricEnergy());
        //?} else {
"""
PLATFORM = {
    "1.20.1": (PLATFORM_IN, j(PLATFORM_FABRIC_HEAD, """
        /*return new PlatformServices(new ForgeLoaderInfo(), new ForgeFuel(), new ForgeNetwork(), new ForgeMenus(),
                new ForgeEnergy());
        *///?}
    }
""")),
    "1.21.1": (PLATFORM_IN, j(PLATFORM_FABRIC_HEAD, """
        /*return new PlatformServices(new NeoForgeLoaderInfo(), new NeoForgeFuel(), new NeoForgeNetwork(),
                new NeoForgeMenus(), new NeoForgeEnergy());
        *///?}
    }
""")),
}
PLATFORM["1.21.11"] = PLATFORM["1.21.1"]

CLIENT_PLATFORM_IN = j("""
    private static ClientPlatformServices loaderServices() {
        //? if fabric {
        return new ClientPlatformServices(new FabricClientNetwork());
        //?} else if neoforge {
        /*return new ClientPlatformServices(new NeoForgeClientNetwork((NeoForgeNetwork) Platform.network()));
        *///?} else {
        /*return new ClientPlatformServices(new ForgeClientNetwork((ForgeNetwork) Platform.network()));
        *///?}
    }
""")
CLIENT_PLATFORM_FABRIC_HEAD = """
    private static ClientPlatformServices loaderServices() {
        //? if fabric {
        return new ClientPlatformServices(new FabricClientNetwork());
        //?} else {
"""
CLIENT_PLATFORM = {
    "1.20.1": (CLIENT_PLATFORM_IN, j(CLIENT_PLATFORM_FABRIC_HEAD, """
        /*return new ClientPlatformServices(new ForgeClientNetwork((ForgeNetwork) Platform.network()));
        *///?}
    }
""")),
    "1.21.1": (CLIENT_PLATFORM_IN, j(CLIENT_PLATFORM_FABRIC_HEAD, """
        /*return new ClientPlatformServices(new NeoForgeClientNetwork((NeoForgeNetwork) Platform.network()));
        *///?}
    }
""")),
}
CLIENT_PLATFORM["1.21.11"] = CLIENT_PLATFORM["1.21.1"]

# --- OMLib CompatScreen.java 29-63 (nested chain in the if branch) --------------------------------------------------

CS_MID = """
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderForeground(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return onScroll(mouseX, mouseY, scrollY) || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
"""
CS_ELSE_BODY = """
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        renderPanel(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderForeground(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return onScroll(mouseX, mouseY, delta) || super.mouseScrolled(mouseX, mouseY, delta);
    }
"""
CS_ELSE_COMMENTED = """
    //?} else {
    /*@Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        renderPanel(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderForeground(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return onScroll(mouseX, mouseY, delta) || super.mouseScrolled(mouseX, mouseY, delta);
    }
    *///?}
"""
CS_RENDER_BACKGROUND_OPEN = """
    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
"""

CS_1201_IN = j("""
    //? if >=1.21 {
    /*@Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        //? if >=1.21.11 {
        /^super.renderBackground(graphics, mouseX, mouseY, partialTick);
        ^///?} else {
        renderTransparentBackground(graphics);
        //?}
        renderPanel(graphics, mouseX, mouseY, partialTick);
""", CS_MID, """
    *///?} else {
""", CS_ELSE_BODY, """
    //?}
""")
CS_1211_IN = j("""
    //? if >=1.21 {
""", CS_RENDER_BACKGROUND_OPEN, """
        //? if >=1.21.11 {
        /*super.renderBackground(graphics, mouseX, mouseY, partialTick);
        *///?} else {
        renderTransparentBackground(graphics);
        //?}
        renderPanel(graphics, mouseX, mouseY, partialTick);
""", CS_MID, CS_ELSE_COMMENTED)
CS_12111_IN = j("""
    //? if >=1.21 {
""", CS_RENDER_BACKGROUND_OPEN, """
        //? if >=1.21.11 {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        //?} else {
        /*renderTransparentBackground(graphics);
        *///?}
        renderPanel(graphics, mouseX, mouseY, partialTick);
""", CS_MID, CS_ELSE_COMMENTED)
CS_1201_OUT = j(CS_ELSE_BODY)
CS_1211_OUT = j(CS_RENDER_BACKGROUND_OPEN, """
        renderTransparentBackground(graphics);
        renderPanel(graphics, mouseX, mouseY, partialTick);
""", CS_MID)
CS_12111_OUT = j(CS_RENDER_BACKGROUND_OPEN, """
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        renderPanel(graphics, mouseX, mouseY, partialTick);
""", CS_MID)

# --- OMLib CommonTags.java 28-45 (escaped comments in the commented branch) -----------------------------------------

CT_IF_ACTIVE = """
    //? if >=1.21 {
    /** Ore dictionary {@code gemDiamond}. */
    public static final TagKey<Item> DIAMOND_GEMS = tag("gems/diamond");
"""
CT_IF_OUT = """
    /** Ore dictionary {@code gemDiamond}. */
    public static final TagKey<Item> DIAMOND_GEMS = tag("gems/diamond");
"""
CT_IF_COMMENTED = """
    //? if >=1.21 {
    /*/^* Ore dictionary {@code gemDiamond}. ^/
    public static final TagKey<Item> DIAMOND_GEMS = tag("gems/diamond");
"""
CT_ELSE_ESCAPED = """
    //?} else {
    /*/^* Wrenches of other mods: {@code c:wrenches} on 1.20.1, {@code c:tools/wrench} on 1.21. ^/
    public static final TagKey<Item> WRENCHES = tag("wrenches");
    /^* Ore dictionary {@code ingotIron}. ^/
    public static final TagKey<Item> IRON_INGOTS = tag("iron_ingots");
    /^* Ore dictionary {@code dustRedstone}. ^/
    public static final TagKey<Item> REDSTONE_DUSTS = tag("redstone_dusts");
    /^* Ore dictionary {@code ingotGold}. ^/
    public static final TagKey<Item> GOLD_INGOTS = tag("gold_ingots");
    /^* Ore dictionary {@code gemQuartz}. ^/
    public static final TagKey<Item> QUARTZ_GEMS = tag("quartz");
    /^* Ore dictionary {@code gemDiamond}. ^/
    public static final TagKey<Item> DIAMOND_GEMS = tag("diamonds");
    *///?}
"""
CT_IF_COMMENTED_END = """
    *///?} else {
"""
CT_ELSE_ACTIVE_BODY = """
    /** Wrenches of other mods: {@code c:wrenches} on 1.20.1, {@code c:tools/wrench} on 1.21. */
    public static final TagKey<Item> WRENCHES = tag("wrenches");
    /** Ore dictionary {@code ingotIron}. */
    public static final TagKey<Item> IRON_INGOTS = tag("iron_ingots");
    /** Ore dictionary {@code dustRedstone}. */
    public static final TagKey<Item> REDSTONE_DUSTS = tag("redstone_dusts");
    /** Ore dictionary {@code ingotGold}. */
    public static final TagKey<Item> GOLD_INGOTS = tag("gold_ingots");
    /** Ore dictionary {@code gemQuartz}. */
    public static final TagKey<Item> QUARTZ_GEMS = tag("quartz");
    /** Ore dictionary {@code gemDiamond}. */
    public static final TagKey<Item> DIAMOND_GEMS = tag("diamonds");
"""
CT_TAIL = """
    /** Ore dictionary {@code paneGlass}. */
    public static final TagKey<Item> GLASS_PANES = tag("glass_panes");
"""
COMMON_TAGS = {
    "1.20.1": (j(CT_IF_COMMENTED, CT_IF_COMMENTED_END, CT_ELSE_ACTIVE_BODY, """
    //?}
""", CT_TAIL), j(CT_ELSE_ACTIVE_BODY, CT_TAIL)),
    "1.21.1": (j(CT_IF_ACTIVE, CT_ELSE_ESCAPED, CT_TAIL), j(CT_IF_OUT, CT_TAIL)),
    "1.21.11": (j(CT_IF_ACTIVE, CT_ELSE_ESCAPED, CT_TAIL), j(CT_IF_OUT, CT_TAIL)),
}

# --- OMT gametest sources ---------------------------------------------------------------------------------------

OMT_EMPTY_STRUCTURE_IN = j("""
    //? if fabric || >=1.21.11 {
    String EMPTY_STRUCTURE = "openmodularturrets:empty";
    //?} else {
    /*String EMPTY_STRUCTURE = "empty";
    *///?}
""")
OMT_EMPTY_STRUCTURE_FABRIC_ELSE = j("""
    //? if fabric {
    String EMPTY_STRUCTURE = "openmodularturrets:empty";
    //?} else {
    /*String EMPTY_STRUCTURE = "empty";
    *///?}
""")
OMT_EMPTY_STRUCTURE = {
    "1.20.1": (OMT_EMPTY_STRUCTURE_IN, OMT_EMPTY_STRUCTURE_FABRIC_ELSE),
    "1.21.1": (OMT_EMPTY_STRUCTURE_IN, OMT_EMPTY_STRUCTURE_FABRIC_ELSE),
    "1.21.11": (OMT_EMPTY_STRUCTURE_IN, j("""
    String EMPTY_STRUCTURE = "openmodularturrets:empty";
""")),
}

AS_NEOFORGE_HOLDER = """
//? if neoforge && <1.21.11 {
/*import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
*///?}
"""
AS_FORGE_HOLDER = """
//? if forge {
/*import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
*///?}
"""
AS_OMT_IMPORT = """
//? if (neoforge && <1.21.11) || forge {
/*import omtteam.openmodularturrets.OMT;
*///?}
"""
AS_MIDDLE_IMPORTS = """
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
"""
AS_GAMETEST_ACTIVE = """
//? if <1.21.11 {
import net.minecraft.gametest.framework.GameTest;
//?}
"""
AS_GAMETEST_COMMENTED = """
//? if <1.21.11 {
/*import net.minecraft.gametest.framework.GameTest;
*///?}
"""
AS_TAIL_IMPORTS = """
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
"""
AS_IMPORTS_IN_121 = j(AS_NEOFORGE_HOLDER, AS_FORGE_HOLDER, AS_OMT_IMPORT, AS_MIDDLE_IMPORTS, AS_GAMETEST_ACTIVE,
                      AS_TAIL_IMPORTS)
AS_IMPORTS = {
    "1.20.1": (AS_IMPORTS_IN_121, j(AS_FORGE_HOLDER, """
//? if forge {
/*import omtteam.openmodularturrets.OMT;
*///?}
""", AS_MIDDLE_IMPORTS, """
import net.minecraft.gametest.framework.GameTest;
""", AS_TAIL_IMPORTS)),
    "1.21.1": (AS_IMPORTS_IN_121, j("""
//? if neoforge {
/*import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
*///?}
//? if neoforge {
/*import omtteam.openmodularturrets.OMT;
*///?}
""", AS_MIDDLE_IMPORTS, """
import net.minecraft.gametest.framework.GameTest;
""", AS_TAIL_IMPORTS)),
    "1.21.11": (j(AS_NEOFORGE_HOLDER, AS_FORGE_HOLDER, AS_OMT_IMPORT, AS_MIDDLE_IMPORTS, AS_GAMETEST_COMMENTED,
                  AS_TAIL_IMPORTS), j(AS_MIDDLE_IMPORTS, AS_TAIL_IMPORTS)),
}

AS_HOLDER_IN = j("""
 */
//? if (neoforge && <1.21.11) || forge {
/*@GameTestHolder(OMT.MOD_ID)
@PrefixGameTestTemplate(false)
*///?}
@SuppressWarnings("unused")
public class AmmoSortGameTests implements OMTGameTests {
""")
AS_HOLDER = {
    "1.20.1": (AS_HOLDER_IN, j("""
 */
//? if forge {
/*@GameTestHolder(OMT.MOD_ID)
@PrefixGameTestTemplate(false)
*///?}
@SuppressWarnings("unused")
public class AmmoSortGameTests implements OMTGameTests {
""")),
    "1.21.1": (AS_HOLDER_IN, j("""
 */
//? if neoforge {
/*@GameTestHolder(OMT.MOD_ID)
@PrefixGameTestTemplate(false)
*///?}
@SuppressWarnings("unused")
public class AmmoSortGameTests implements OMTGameTests {
""")),
    "1.21.11": (AS_HOLDER_IN, j("""
 */
@SuppressWarnings("unused")
public class AmmoSortGameTests implements OMTGameTests {
""")),
}

SOLAR_BODY = """
    /** Not on Forge: its game test server places the tests underground. */
    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = TIMEOUT_TICKS)
    public void solarPanelChargesUnderTheSky(GameTestHelper helper) {
        TurretBaseBlockEntity base = solarBase(helper);
        helper.succeedWhen(() -> {
            helper.assertTrue(helper.getLevel().canSeeSky(helper.absolutePos(BASE.above(2))),
                    "The light engine has not lit the base yet");
            helper.assertTrue(energy(base) > 0, "The solar panel did not charge the base");
        });
    }
"""
SOLAR_IN = j("""
    private static final long NOON = 6000L;

    //? if fabric || neoforge {
""", SOLAR_BODY, """
    //?}

""")
SOLAR_UNCONDITIONAL = j("""
    private static final long NOON = 6000L;

""", SOLAR_BODY, """

""")
SOLAR = {
    "1.20.1": (SOLAR_IN, j("""
    private static final long NOON = 6000L;

    //? if fabric {
""", SOLAR_BODY, """
    //?}

""")),
    "1.21.1": (SOLAR_IN, SOLAR_UNCONDITIONAL),
    "1.21.11": (SOLAR_IN, SOLAR_UNCONDITIONAL),
}

# --- OML FabricBlockEnabledCondition.java 22-26 and 44-64 -----------------------------------------------------------

OML_LOOKUP_IN = {
    "1.20.1": j("""
//? if >=1.21.11 {
/*import net.minecraft.resources.RegistryOps.RegistryInfoLookup;
*///?} else if >=1.21 {
/*import net.minecraft.core.HolderLookup;
*///?}
"""),
    "1.21.1": j("""
//? if >=1.21.11 {
/*import net.minecraft.resources.RegistryOps.RegistryInfoLookup;
*///?} else if >=1.21 {
import net.minecraft.core.HolderLookup;
//?}
"""),
    "1.21.11": j("""
//? if >=1.21.11 {
import net.minecraft.resources.RegistryOps.RegistryInfoLookup;
//?} else if >=1.21 {
/*import net.minecraft.core.HolderLookup;
*///?}
"""),
}
OML_LOOKUP = {
    "1.20.1": (OML_LOOKUP_IN["1.20.1"], ""),
    "1.21.1": (OML_LOOKUP_IN["1.21.1"], "import net.minecraft.core.HolderLookup;"),
    "1.21.11": (OML_LOOKUP_IN["1.21.11"], "import net.minecraft.resources.RegistryOps.RegistryInfoLookup;"),
}

OML_RECORD_HEAD = """
    private record Condition(List<String> blocks) implements ResourceCondition {
        static final MapCodec<Condition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.listOf().fieldOf(BLOCKS).forGetter(Condition::blocks)).apply(instance, Condition::new));
        static final ResourceConditionType<Condition> TYPE = ResourceConditionType.create(ID, CODEC);

        @Override
        public ResourceConditionType<?> getType() {
            return TYPE;
        }

"""
OML_RECORD_TAIL = """
            return BlockEnabledCondition.anyEnabled(blocks);
        }
    }
"""
OML_TEST_OLD = """
        @Override
        public boolean test(@Nullable HolderLookup.Provider registries) {
"""
OML_TEST_NEW = """
        @Override
        public boolean test(@Nullable RegistryInfoLookup registries) {
"""
# The else branch is shortened to its first lines (the original continues with the Gson based register method).
OML_ELSE_ACTIVE_BODY = """
    /** Registers the condition type; called by the Fabric entrypoint. */
    public static void register() {
"""
OML_NESTED_IN = {
    "1.20.1": j("""
    //? if >=1.21 {
    /*private record Condition(List<String> blocks) implements ResourceCondition {
        static final MapCodec<Condition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.listOf().fieldOf(BLOCKS).forGetter(Condition::blocks)).apply(instance, Condition::new));
        static final ResourceConditionType<Condition> TYPE = ResourceConditionType.create(ID, CODEC);

        @Override
        public ResourceConditionType<?> getType() {
            return TYPE;
        }

        //? if >=1.21.11 {
        /^@Override
        public boolean test(@Nullable RegistryInfoLookup registries) {
        ^///?} else {
        @Override
        public boolean test(@Nullable HolderLookup.Provider registries) {
        //?}
""", OML_RECORD_TAIL, """
    *///?} else {
""", OML_ELSE_ACTIVE_BODY, """
    //?}
"""),
    "1.21.1": j("""
    //? if >=1.21 {
""", OML_RECORD_HEAD, """
        //? if >=1.21.11 {
        /*@Override
        public boolean test(@Nullable RegistryInfoLookup registries) {
        *///?} else {
        @Override
        public boolean test(@Nullable HolderLookup.Provider registries) {
        //?}
""", OML_RECORD_TAIL, """
    //?} else {
    /*/^* Registers the condition type; called by the Fabric entrypoint. ^/
    public static void register() {
    *///?}
"""),
    "1.21.11": j("""
    //? if >=1.21 {
""", OML_RECORD_HEAD, """
        //? if >=1.21.11 {
        @Override
        public boolean test(@Nullable RegistryInfoLookup registries) {
        //?} else {
        /*@Override
        public boolean test(@Nullable HolderLookup.Provider registries) {
        *///?}
""", OML_RECORD_TAIL, """
    //?} else {
    /*/^* Registers the condition type; called by the Fabric entrypoint. ^/
    public static void register() {
    *///?}
"""),
}
OML_NESTED_OUT = {
    "1.20.1": j(OML_ELSE_ACTIVE_BODY),
    "1.21.1": j(OML_RECORD_HEAD, OML_TEST_OLD, OML_RECORD_TAIL),
    "1.21.11": j(OML_RECORD_HEAD, OML_TEST_NEW, OML_RECORD_TAIL),
}

# Cases that must resolve: name -> {branch: (input, expected)}. The nested cases are tested one branch at a time below.
CASES = {
    "VersionCompat imports": VC_IMPORTS,
    "VersionCompat resource id and properties": VC_ID,
    "VersionCompat block entity builder": VC_BUILDER,
    "CompatBlockEntity model data imports": CBE_IMPORTS,
    "CompatBlockEntity render data": CBE_RENDER_DATA,
    "Platform": PLATFORM,
    "ClientPlatform": CLIENT_PLATFORM,
    "CommonTags": COMMON_TAGS,
    "OMTGameTests": OMT_EMPTY_STRUCTURE,
    "AmmoSortGameTests imports": AS_IMPORTS,
    "AmmoSortGameTests holder": AS_HOLDER,
    "SolarPanelGameTests": SOLAR,
    "OML lookup import": OML_LOOKUP,
    "CompatScreen 1.21.x": {
        "1.21.1": (CS_1211_IN, CS_1211_OUT),
        "1.21.11": (CS_12111_IN, CS_12111_OUT),
    },
    "OML nested condition 1.21.x": {
        "1.21.1": (OML_NESTED_IN["1.21.1"], OML_NESTED_OUT["1.21.1"]),
        "1.21.11": (OML_NESTED_IN["1.21.11"], OML_NESTED_OUT["1.21.11"]),
    },
}


class FixtureTest(unittest.TestCase):
    def check(self, case):
        for branch, (source, expected) in case.items():
            with self.subTest(branch=branch):
                self.assertEqual(expected, resolve(source, BRANCHES[branch]))

    def test_version_compat_imports(self):
        self.check(VC_IMPORTS)

    def test_version_compat_resource_id_and_properties(self):
        self.check(VC_ID)

    def test_version_compat_builder_chain(self):
        self.check(VC_BUILDER)

    def test_builder_chain_1201_and_1211_keep_only_the_else_body(self):
        for branch in ("1.20.1", "1.21.1"):
            result = resolve(VC_BUILDER[branch][0], BRANCHES[branch])
            self.assertNotIn("//?", result)
            self.assertNotIn("*/", result)
            self.assertEqual("        return BlockEntityType.Builder.of(factory::apply, blocks).build(null);", result)

    def test_builder_chain_12111_becomes_fabric_and_else(self):
        result = resolve(VC_BUILDER["1.21.11"][0], B12111).split("\n")
        self.assertEqual(["        //? if fabric {", "        //?} else {", "        *///?}"],
                         [line for line in result if "//?" in line])
        self.assertEqual("        *///?}", result[-1])

    def test_compat_block_entity_model_data_imports(self):
        self.check(CBE_IMPORTS)

    def test_model_data_chain_without_else(self):
        chain = j(CBE_MODEL_DATA_CHAIN)
        self.assertEqual("", resolve(chain, B1201))
        self.assertEqual(j(CBE_NEOFORGE_OLD_LOADER), resolve(chain, B1211))
        self.assertEqual(j(CBE_NEOFORGE_NEW_LOADER), resolve(chain, B12111))

    def test_compat_block_entity_render_data(self):
        self.check(CBE_RENDER_DATA)

    def test_escaped_comment_in_kept_loader_branch_is_verbatim(self):
        line = "    /*/^* The {@link #renderData()} in the block entity's model data on NeoForge and Forge. ^/"
        for branch in BRANCHES:
            with self.subTest(branch=branch):
                result = resolve(CBE_RENDER_DATA[branch][0], BRANCHES[branch]).split("\n")
                self.assertIn(line, result)
                self.assertEqual("    *///?}", result[-1])

    def test_platform_chain(self):
        self.check(PLATFORM)

    def test_client_platform_chain(self):
        self.check(CLIENT_PLATFORM)

    def test_loader_chain_with_neoforge_is_fabric_else_on_every_branch(self):
        for case in (PLATFORM, CLIENT_PLATFORM):
            for branch in BRANCHES:
                with self.subTest(branch=branch):
                    lines = [line for line in resolve(case[branch][0], BRANCHES[branch]).split("\n")
                             if "//?" in line]
                    self.assertEqual(["        //? if fabric {", "        //?} else {", "        *///?}"], lines)

    def test_nested_chain_inside_active_branch(self):
        self.check(CASES["CompatScreen 1.21.x"])

    def test_nested_chain_oml_inside_active_branch(self):
        self.check(CASES["OML nested condition 1.21.x"])

    # The nested chain of an outer branch that is commented out: Stonecutter escapes the `*/` of the inner marker as
    # `^/`, so the line reads `^///?} else {` (and `^///?}` when the inner else branch is the commented one). The
    # CompatScreen.java and FabricBlockEnabledCondition.java fixtures are the real 1.20.1-fabric files (switched with
    # Stonecutter in a throwaway copy); the whole outer chain is dropped.
    def test_nested_chain_inside_commented_branch_is_removed_whole_compat_screen(self):
        self.assertEqual(CS_1201_OUT, resolve(CS_1201_IN, B1201))

    def test_nested_chain_inside_commented_branch_is_removed_whole_oml(self):
        self.assertEqual(OML_NESTED_OUT["1.20.1"], resolve(OML_NESTED_IN["1.20.1"], B1201))

    def test_escaped_inner_markers_are_in_the_fixtures(self):
        self.assertIn("        ^///?} else {", CS_1201_IN.split("\n"))
        self.assertIn("        ^///?} else {", OML_NESTED_IN["1.20.1"].split("\n"))
        self.assertIn("        /^super.renderBackground(graphics, mouseX, mouseY, partialTick);", CS_1201_IN.split("\n"))

    def test_escaped_close_marker_of_a_commented_inner_else_branch(self):
        # synthetic: the inner else branch is the commented one, so its close marker is `^///?}`
        source = j("""
    //? if >=1.21 {
    /*A
    //? if <1.21.2 {
    B
    //?} else {
    /^C
    ^///?}
    D
    *///?} else {
    E
    //?}
""")
        self.assertEqual("    E", resolve(source, B1201))

    def test_escaped_markers_in_a_dropped_chain_are_not_kept(self):
        for source, expected in ((CS_1201_IN, CS_1201_OUT), (OML_NESTED_IN["1.20.1"], OML_NESTED_OUT["1.20.1"])):
            result = resolve(source, B1201)
            self.assertEqual(expected, result)
            self.assertNotIn("^/", result)
            self.assertNotIn("/^", result)

    def test_dropped_nested_chain_with_a_wrong_inner_state_is_refused(self):
        # the inner `>=1.21.11` chain is dropped with its outer branch, so only the outer state is checked; the
        # outer branch is active here although it is dropped
        source = CS_1201_IN.replace("    //? if >=1.21 {\n    /*@Override", "    //? if >=1.21 {\n    @Override")
        source = source.replace("    *///?} else {", "    //?} else {")
        with self.assertRaisesRegex(ResolveError, "is dropped but active"):
            resolve(source, B1201, "CompatScreen.java")

    def test_common_tags_escaped_comments(self):
        self.check(COMMON_TAGS)

    def test_dropped_escaped_comments_leave_no_escape_behind(self):
        for branch in ("1.21.1", "1.21.11"):
            result = resolve(COMMON_TAGS[branch][0], BRANCHES[branch])
            self.assertNotIn("^/", result)
            self.assertNotIn("/^*", result)

    def test_common_tags_1201_unescaped_else_body_is_plain_code(self):
        result = resolve(COMMON_TAGS["1.20.1"][0], B1201)
        self.assertNotIn("//?", result)
        self.assertNotIn("/*", result.replace("/**", ""))

    def test_omt_empty_structure(self):
        self.check(OMT_EMPTY_STRUCTURE)

    def test_ammo_sort_imports(self):
        self.check(AS_IMPORTS)

    def test_ammo_sort_holder_annotations(self):
        self.check(AS_HOLDER)

    def test_neoforge_and_less_than_12111_or_forge_per_branch(self):
        chain = j(AS_OMT_IMPORT)
        self.assertEqual("//? if forge {\n/*import omtteam.openmodularturrets.OMT;\n*///?}", resolve(chain, B1201))
        self.assertEqual("//? if neoforge {\n/*import omtteam.openmodularturrets.OMT;\n*///?}", resolve(chain, B1211))
        self.assertEqual("", resolve(chain, B12111))

    def test_solar_panel_fabric_or_neoforge(self):
        self.check(SOLAR)

    def test_oml_lookup_import(self):
        self.check(OML_LOOKUP)

    def test_every_result_is_free_of_version_predicates(self):
        for name, case in CASES.items():
            for branch, (source, _) in case.items():
                with self.subTest(case=name, branch=branch):
                    for line in resolve(source, BRANCHES[branch]).split("\n"):
                        self.assertIsNone(VERSION_PREDICATE.search(line), line)

    def test_resolving_twice_changes_nothing(self):
        for name, case in CASES.items():
            for branch, (source, _) in case.items():
                with self.subTest(case=name, branch=branch):
                    once = resolve(source, BRANCHES[branch])
                    self.assertEqual(once, resolve(once, BRANCHES[branch]))


class SemanticsTest(unittest.TestCase):
    def test_version_order_is_numeric_not_textual(self):
        # 1.21.1 < 1.21.2 < 1.21.11 (as strings, "1.21.11" < "1.21.2")
        on_1211 = "//? if >=1.21.2 {\n/*newer\n*///?} else {\nolder\n//?}"
        on_12111 = "//? if >=1.21.2 {\nnewer\n//?} else {\n/*older\n*///?}"
        self.assertEqual("older", resolve(on_1211, B1211))
        self.assertEqual("newer", resolve(on_12111, B12111))

    def test_version_order_less_than(self):
        self.assertEqual("old", resolve("//? if <1.21.2 {\nold\n//?} else {\n/*new\n*///?}", B1211))
        self.assertEqual("new", resolve("//? if <1.21.2 {\n/*old\n*///?} else {\nnew\n//?}", B12111))

    def test_short_version_bounds_compare_with_patch_versions(self):
        self.assertEqual("x", resolve("//? if >=1.20 {\nx\n//?}", B1201))
        self.assertEqual("x", resolve("//? if <1.21 {\nx\n//?}", B1201))
        self.assertEqual("", resolve("//? if <1.21 {\n/*x\n*///?}", B1211))
        self.assertEqual("x", resolve("//? if >=1.21 {\nx\n//?}", B12111))

    def test_and_binds_tighter_than_or(self):
        source = "//? if fabric || neoforge && >=1.21.11 {\nA\n//?} else {\n/*B\n*///?}"
        # fabric || (neoforge && >=1.21.11): only fabric on 1.21.1, both loaders on 1.21.11
        self.assertEqual("//? if fabric {\nA\n//?} else {\n/*B\n*///?}", resolve(source, B1211))
        self.assertEqual("A", resolve(source, B12111))

    def test_parentheses(self):
        # (fabric || neoforge) && >=1.21.11: false on 1.21.1 for both loaders, true on 1.21.11 for both
        self.assertEqual("B", resolve("//? if (fabric || neoforge) && >=1.21.11 {\n/*A\n*///?} else {\nB\n//?}",
                                      B1211))
        self.assertEqual("A", resolve("//? if (fabric || neoforge) && >=1.21.11 {\nA\n//?} else {\n/*B\n*///?}",
                                      B12111))

    def test_loader_not_built_counts_as_false(self):
        self.assertEqual("", resolve("//? if neoforge {\n/*A\n*///?}", B1201))
        self.assertEqual("", resolve("//? if forge {\n/*A\n*///?}", B1211))
        self.assertEqual("", resolve("//? if forge {\n/*A\n*///?}", B12111))
        self.assertEqual("//? if forge {\n/*A\n*///?}", resolve("//? if forge {\n/*A\n*///?}", B1201))

    def test_else_branch_of_a_dropped_loader_chain_is_unconditional(self):
        source = "//? if neoforge {\n/*A\n*///?} else {\nB\n//?}"
        self.assertEqual("B", resolve(source, B1201))

    def test_chain_independent_of_the_loader_keeps_its_body(self):
        self.assertEqual("A", resolve("//? if >=1.21 || forge {\nA\n//?}", B1211))
        # on 1.20.1 only fabric holds (forge is false), so a loader condition is left
        self.assertEqual("//? if fabric {\nA\n//?}", resolve("//? if >=1.21 || fabric {\nA\n//?}", B1201))

    def test_nested_chain_inside_active_loader_branch_is_resolved(self):
        source = "//? if fabric {\n//? if >=1.21 {\nA\n//?} else {\n/*B\n*///?}\n//?}"
        self.assertEqual("//? if fabric {\nA\n//?}", resolve(source, B1211))

    def test_nested_loader_chain_inside_resolved_branch_stays(self):
        source = "//? if >=1.21 {\n//? if fabric {\nA\n//?} else {\n/*B\n*///?}\n//?}"
        self.assertEqual("//? if fabric {\nA\n//?} else {\n/*B\n*///?}", resolve(source, B1211))

    def test_indentation_of_rewritten_markers_follows_the_chain(self):
        source = "\t\t//? if fabric || >=1.21.11 {\n\t\tA\n\t\t//?} else {\n\t\t/*B\n\t\t*///?}"
        self.assertEqual("\t\t//? if fabric {\n\t\tA\n\t\t//?} else {\n\t\t/*B\n\t\t*///?}", resolve(source, B1211))
        self.assertEqual("\t\tA", resolve(source, B12111))

    def test_trailing_newline_and_blank_lines_survive(self):
        source = "a\n\n//? if >=1.21 {\nX\n//?}\n\nb\n"
        self.assertEqual("a\n\nX\n\nb\n", resolve(source, B1211))

    def test_text_without_markers_is_unchanged(self):
        source = "class A {\n    /* comment */\n    int x; // plain\n}\n"
        self.assertEqual(source, resolve(source, B1201))


class UnchangedTest(unittest.TestCase):
    def test_pure_loader_chains_are_byte_identical(self):
        cases = [
            (VC_NEOFORGE, B1211),
            (VC_NEOFORGE, B12111),
            (VC_FORGE, B1201),
            ("""
//? if fabric {
A
//?}
""", B1201),
            ("""
//? if fabric {
A
//?} else {
/*B
*///?}
""", B1201),
            ("""
//? if fabric {
A
//?} else {
/*B
*///?}
""", B12111),
            ("""
    //? if neoforge {
    /*A
    B
    *///?}
""", B1211),
        ]
        for source, branch in cases:
            with self.subTest(source=source, branch=branch[0]):
                text = t(source)
                self.assertEqual(text, resolve(text, branch))
                self.assertEqual(text + "\n", resolve(text + "\n", branch))

    def test_a_file_made_of_pure_loader_chains_is_identical(self):
        source = j(CBE_HEAD, VC_NEOFORGE, VC_FORGE, """
class A {
    //? if fabric {
    int x = 1;
    //?} else {
    /*int x = 2;
    *///?}
}
""") + "\n"
        for branch in (B1211, B12111):
            with self.subTest(branch=branch[0]):
                result, stats = resolve_text(source, branch[0], branch[1])
                self.assertEqual(source.replace(t(VC_FORGE) + "\n", ""), result)
        result, stats = resolve_text(source, B1201[0], B1201[1])
        self.assertEqual(source.replace(t(VC_NEOFORGE) + "\n", ""), result)

    def test_pure_loader_chain_with_nested_version_chain_resolves_inside_only(self):
        source = "//? if fabric {\n//? if >=1.21 {\nA\n//?}\n//?}"
        result, stats = resolve_text(source, B1211[0], B1211[1])
        self.assertEqual("//? if fabric {\nA\n//?}", result)
        self.assertEqual(1, stats.unchanged)
        self.assertEqual(1, stats.unconditional)

    def test_loader_chain_with_else_if_is_rewritten_with_else(self):
        source = "//? if fabric {\nA\n//?} else if neoforge {\n/*B\n*///?}"
        self.assertEqual("//? if fabric {\nA\n//?} else {\n/*B\n*///?}", resolve(source, B1211))
        self.assertEqual("//? if fabric {\nA\n//?}", resolve(source, B1201))


class StatsTest(unittest.TestCase):
    def counts(self, text, branch):
        stats = resolve_text(text, branch[0], branch[1])[1]
        return stats.unconditional, stats.dropped, stats.loader, stats.unchanged

    def test_counts_on_the_version_compat_imports(self):
        self.assertEqual((1, 2, 0, 1), self.counts(VC_IMPORTS["1.21.1"][0], B1211))
        self.assertEqual((1, 1, 1, 1), self.counts(VC_IMPORTS["1.21.11"][0], B12111))

    def test_counts_on_the_1201_imports(self):
        self.assertEqual((0, 3, 0, 1), self.counts(VC_IMPORTS["1.20.1"][0], B1201))

    def test_stats_text(self):
        stats = resolve_text(VC_IMPORTS["1.21.11"][0], B12111[0], B12111[1])[1]
        self.assertEqual("1 unconditional, 1 dropped, 1 left as loader chains, 1 unchanged", str(stats))


class ErrorTest(unittest.TestCase):
    def assertRejects(self, source, branch, pattern):
        with self.assertRaisesRegex(ResolveError, pattern):
            resolve(source, branch, "Test.java")

    def test_block_comment_condition(self):
        self.assertRejects("/*? if >=1.21 {*/\nA\n/*?}*/", B1211, r"Test\.java:1: unsupported Stonecutter syntax")

    def test_swap_and_dollar_syntax(self):
        self.assertRejects("int x = 1; //$ version\n", B1211, r"Test\.java:1: unsupported Stonecutter syntax")
        self.assertRejects("int x = /*$ version*/ 1;\n", B1211, r"unsupported Stonecutter syntax")

    def test_negation(self):
        self.assertRejects("//? if !fabric {\n/*A\n*///?}", B1211, "unsupported condition syntax")

    def test_less_or_equal(self):
        self.assertRejects("//? if <=1.21 {\nA\n//?}", B1211, "unsupported condition syntax")

    def test_greater_than_without_equals(self):
        self.assertRejects("//? if >1.21 {\nA\n//?}", B1211, "unsupported condition syntax")

    def test_unknown_constant(self):
        self.assertRejects("//? if quilt {\n/*A\n*///?}", B1211, "unknown constant 'quilt'")
        self.assertRejects("//? if fabric && quilt {\n/*A\n*///?}", B1211, "unknown constant 'quilt'")

    def test_unclosed_chain(self):
        self.assertRejects("a\n//? if fabric {\nA\n", B1211, r"'//\? if' at line 2 is never closed")

    def test_unclosed_nested_chain(self):
        self.assertRejects("//? if >=1.21 {\n//? if fabric {\nA\n//?}\n", B1211, "at line 1 is never closed")

    def test_marker_without_an_open_chain(self):
        self.assertRejects("A\n//?}\n", B1211, "marker without an open")
        self.assertRejects("A\n//?} else {\n", B1211, "marker without an open")

    def test_marker_not_at_the_start_of_the_line(self):
        self.assertRejects("int x; //? if fabric {\n", B1211, "marker not at the start of the line")

    def test_closing_comment_before_an_opening_marker(self):
        self.assertRejects("*///? if fabric {\nA\n//?}", B1211, r"'\*/' before an opening marker")

    def test_unsupported_marker_text(self):
        self.assertRejects("//? if fabric {\nA\n//?} elseif neoforge {\n//?}", B1211, "unsupported marker")

    def test_unbalanced_parenthesis(self):
        # Suspected tool bug: `take()` runs past the token list and raises IndexError instead of ResolveError, so the
        # command line tool dies with a traceback instead of reporting the file and line.
        self.assertRejects("//? if (fabric || neoforge {\n/*A\n*///?}", B1211, "missing|unexpected")

    def test_unbalanced_closing_parenthesis(self):
        self.assertRejects("//? if fabric) {\nA\n//?}", B1211, "unexpected")

    def test_dangling_operator(self):
        self.assertRejects("//? if fabric && {\nA\n//?}", B1211, "unexpected")

    def test_tokenize_accepts_the_supported_syntax(self):
        self.assertEqual(["(", "neoforge", "&&", "<1.21.11", ")", "||", "forge"],
                         tokenize("(neoforge && <1.21.11) || forge"))
        self.assertEqual([">=1.21.11"], tokenize(">=1.21.11"))

    def test_branch_that_becomes_unconditional_but_is_still_commented(self):
        # the 1.20.1-fabric state fed to the 1.21.1 branch
        source = VC_ID["1.20.1"][0]
        with self.assertRaisesRegex(ResolveError, r"Test\.java:1: branch at line 1 is inactive on the Fabric node, "
                                                  r"but its condition says otherwise; switch to the 1\.21\.1-fabric "
                                                  r"node first"):
            resolve(source, B1211, "Test.java")

    def test_wrong_node_state_for_an_else_branch(self):
        source = "//? if >=1.21 {\nA\n//?} else {\nB\n//?}"
        self.assertRejects(source, B1201, r"is dropped but active")

    def test_wrong_node_state_for_a_remaining_else_branch(self):
        source = "//? if >=1.21 {\n/*A\n*///?} else {\n/*B\n*///?}"
        self.assertRejects(source, B1201, r"branch at line 3 is inactive on the Fabric node")

    def test_wrong_node_state_unconditional_all_inactive(self):
        for branch, name in ((B1211, "1.21.1"), (B12111, "1.21.11")):
            with self.subTest(branch=name):
                self.assertRejects(VC_IMPORTS_GE_121_COMMENTED.strip("\n"), branch,
                                   r"is inactive on the Fabric node.*switch to the " + re.escape(name) + "-fabric")

    def test_dropped_branch_that_is_active(self):
        # the 1.21.1-fabric state fed to the 1.20.1 branch
        source = VC_IMPORTS_GE_121_ACTIVE.strip("\n")
        self.assertRejects(source, B1201, r"Test\.java:1: branch at line 1 is dropped but active; "
                                          r"switch to the 1\.20\.1-fabric node first")

    def test_dropped_loader_branch_that_is_active(self):
        source = "//? if neoforge {\nA\n//?}"
        self.assertRejects(source, B1201, "is dropped but active")
        self.assertRejects("//? if forge {\nA\n//?}", B1211, "is dropped but active")

    def test_kept_loader_branch_that_is_active_on_fabric(self):
        self.assertRejects("//? if neoforge {\nA\n//?}", B1211, r"is active on the Fabric node")

    def test_fabric_branch_that_is_commented(self):
        self.assertRejects("//? if fabric {\n/*A\n*///?}", B1211, r"is inactive on the Fabric node")

    def test_version_node_state_of_the_wrong_minecraft_version(self):
        # the 1.21.1 state of the builder chain on 1.21.11: branch 1 should be active but is commented
        self.assertRejects(VC_BUILDER["1.21.1"][0], B12111, r"inactive on the Fabric node")

    def test_nested_chain_in_a_kept_inactive_loader_branch_is_refused(self):
        source = "//? if neoforge && >=1.21 {\n/*A\n//? if >=1.21 {\nB\n//?}\n*///?}"
        self.assertRejects(source, B1211, "nested condition inside a commented branch that is kept is not supported")

    def test_nested_chain_in_an_unchanged_inactive_loader_branch_is_refused(self):
        source = "//? if neoforge {\n/*A\n//? if >=1.21 {\nB\n//?}\n*///?}"
        self.assertRejects(source, B1211, "nested condition inside a commented branch that is kept is not supported")

    def test_error_names_the_file_and_the_line_of_the_chain(self):
        with self.assertRaisesRegex(ResolveError, r"^Src\.java:3: branch at line 3 is dropped but active"):
            resolve("x\ny\n//? if >=1.21 {\nA\n//?}", B1201, "Src.java")


if __name__ == "__main__":
    unittest.main()
