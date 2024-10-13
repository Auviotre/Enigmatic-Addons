package auviotre.enigmatic.addon.registries;

import auviotre.enigmatic.addon.contents.items.*;
import com.aizistral.enigmaticlegacy.api.generic.ConfigurableItem;
import com.aizistral.enigmaticlegacy.api.items.IAdvancedPotionItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;

public class EnigmaticAddonItems extends AbstractRegistry<Item> {
    private static final EnigmaticAddonItems INSTANCE = new EnigmaticAddonItems();
    @ConfigurableItem("Ode to Living Beings")
    @ObjectHolder(value = "enigmaticaddons:ode_to_living", registryName = "item")
    public static final OdeToLiving LIVING_ODE = null;

    @ConfigurableItem("Pact of Dark Night")
    @ObjectHolder(value = "enigmaticaddons:night_scroll", registryName = "item")
    public static final NightScroll NIGHT_SCROLL = null;

    @ConfigurableItem("Magic Quartz Ring")
    @ObjectHolder(value = "enigmaticaddons:quartz_ring", registryName = "item")
    public static final QuartzRing QUARTZ_RING = null;

    @ConfigurableItem("Dragon Breath Bow")
    @ObjectHolder(value = "enigmaticaddons:dragon_bow", registryName = "item")
    public static final DragonBow DRAGON_BOW = null;

    @ConfigurableItem("Promise of the Earth")
    @ObjectHolder(value = "enigmaticaddons:earth_promise", registryName = "item")
    public static final EarthPromise EARTH_PROMISE = null;

    @ConfigurableItem("Forgotten Ice Crystal")
    @ObjectHolder(value = "enigmaticaddons:forgotten_ice", registryName = "item")
    public static final ForgottenIce FORGOTTEN_ICE = null;

    @ConfigurableItem("Lost Engine")
    @ObjectHolder(value = "enigmaticaddons:lost_engine", registryName = "item")
    public static final LostEngine LOST_ENGINE = null;

    @ConfigurableItem("Revival Leaf")
    @ObjectHolder(value = "enigmaticaddons:revival_leaf", registryName = "item")
    public static final RevivalLeaf REVIVAL_LEAF = null;

    @ConfigurableItem("Common Potions")
    @ObjectHolder(value = "enigmaticaddons:common_potion", registryName = "item")
    public static final UltimatePotionAddon.Base COMMON_POTION = null;

    @ConfigurableItem("Common Potions")
    @ObjectHolder(value = "enigmaticaddons:common_potion_splash", registryName = "item")
    public static final UltimatePotionAddon.Splash COMMON_POTION_SPLASH = null;

    @ConfigurableItem("Common Potions")
    @ObjectHolder(value = "enigmaticaddons:common_potion_lingering", registryName = "item")
    public static final UltimatePotionAddon.Lingering COMMON_POTION_LINGERING = null;

    @ConfigurableItem("Ultimate Potions")
    @ObjectHolder(value = "enigmaticaddons:ultimate_potion", registryName = "item")
    public static final UltimatePotionAddon.Base ULTIMATE_POTION = null;

    @ConfigurableItem("Ultimate Potions")
    @ObjectHolder(value = "enigmaticaddons:ultimate_potion_splash", registryName = "item")
    public static final UltimatePotionAddon.Splash ULTIMATE_POTION_SPLASH = null;

    @ConfigurableItem("Ultimate Potions")
    @ObjectHolder(value = "enigmaticaddons:ultimate_potion_lingering", registryName = "item")
    public static final UltimatePotionAddon.Lingering ULTIMATE_POTION_LINGERING = null;

    @ConfigurableItem("False Justice")
    @ObjectHolder(value = "enigmaticaddons:false_justice", registryName = "item")
    public static final FalseJustice FALSE_JUSTICE = null;

    @ConfigurableItem("Sanguinary Hunting Handbook")
    @ObjectHolder(value = "enigmaticaddons:sanguinary_handbook", registryName = "item")
    public static final SanguinaryHandbook SANGUINARY_HANDBOOK = null;

    @ConfigurableItem("Scroll of Ignorance Curse")
    @ObjectHolder(value = "enigmaticaddons:cursed_xp_scroll", registryName = "item")
    public static final CursedXPScroll CURSED_XP_SCROLL = null;

    @ConfigurableItem("Charm of Hell Blade")
    @ObjectHolder(value = "enigmaticaddons:hell_blade_charm", registryName = "item")
    public static final HellBladeCharm HELL_BLADE_CHARM = null;

    @ConfigurableItem("Potion of Cosmic")
    @ObjectHolder(value = "enigmaticaddons:astral_potion", registryName = "item")
    public static final AstralPotion ASTRAL_POTION = null;

    @ConfigurableItem("Primeval Cube")
    @ObjectHolder(value = "enigmaticaddons:primeval_cube", registryName = "item")
    public static final PrimevalCube PRIMEVAL_CUBE = null;

    @ConfigurableItem("Fragment of the Earth")
    @ObjectHolder(value = "enigmaticaddons:earth_heart_fragment", registryName = "item")
    public static final EarthHeartFragment EARTH_HEART_FRAGMENT = null;

    @ConfigurableItem("Tome of Void")
    @ObjectHolder(value = "enigmaticaddons:void_tome", registryName = "item")
    public static final VoidTome VOID_TOME = null;

    @ConfigurableItem("Antique Book Bag")
    @ObjectHolder(value = "enigmaticaddons:antique_bag", registryName = "item")
    public static final AntiqueBag ANTIQUE_BAG = null;

    @ConfigurableItem("Emblem of Adventurer")
    @ObjectHolder(value = "enigmaticaddons:adventure_charm", registryName = "item")
    public static final AdventureCharm ADVENTURE_CHARM = null;

    @ConfigurableItem("Ichor Droplet")
    @ObjectHolder(value = "enigmaticaddons:ichor_droplet", registryName = "item")
    public static final IchorDroplet ICHOR_DROPLET = null;

    @ConfigurableItem("Ichoroot")
    @ObjectHolder(value = "enigmaticaddons:ichoroot", registryName = "item")
    public static final Ichoroot ICHOROOT = null;

    @ConfigurableItem("Pure Heart")
    @ObjectHolder(value = "enigmaticaddons:pure_heart", registryName = "item")
    public static final PureHeart PURE_HEART = null;

    @ConfigurableItem("Tome of Divination")
    @ObjectHolder(value = "enigmaticaddons:bless_amplifier", registryName = "item")
    public static final BlessAmplifier BLESS_AMPLIFIER = null;

    @ConfigurableItem("Scroll of Thunder Embrace")
    @ObjectHolder(value = "enigmaticaddons:thunder_scroll", registryName = "item")
    public static final ThunderScroll THUNDER_SCROLL = null;

    @ConfigurableItem("Holy Stone")
    @ObjectHolder(value = "enigmaticaddons:bless_stone", registryName = "item")
    public static final BlessStone BLESS_STONE = null;

    @ConfigurableItem("Ring of Redemption")
    @ObjectHolder(value = "enigmaticaddons:bless_ring", registryName = "item")
    public static final BlessRing BLESS_RING = null;

    @ConfigurableItem("Etherium Core")
    @ObjectHolder(value = "enigmaticaddons:etherium_core", registryName = "item")
    public static final EtheriumCore ETHERIUM_CORE = null;

    @ConfigurableItem("Forgers Gem")
    @ObjectHolder(value = "enigmaticaddons:forger_gem", registryName = "item")
    public static final ForgerGem FORGER_GEM = null;

    @ConfigurableItem("Evil")
    @ObjectHolder(value = "enigmaticaddons:evil_dagger", registryName = "item")
    public static final EvilDagger EVIL_DAGGER = null;

    private EnigmaticAddonItems() {
        super(ForgeRegistries.ITEMS);
        this.register("ode_to_living", OdeToLiving::new);
        this.register("night_scroll", NightScroll::new);
        this.register("quartz_ring", QuartzRing::new);
        this.register("dragon_bow", DragonBow::new);
        this.register("earth_promise", EarthPromise::new);
        this.register("forgotten_ice", ForgottenIce::new);
        this.register("lost_engine", LostEngine::new);
        this.register("revival_leaf", RevivalLeaf::new);
        this.register("false_justice", FalseJustice::new);
        this.register("sanguinary_handbook", SanguinaryHandbook::new);
        this.register("cursed_xp_scroll", CursedXPScroll::new);
        this.register("hell_blade_charm", HellBladeCharm::new);
        this.register("astral_potion", AstralPotion::new);
        this.register("primeval_cube", PrimevalCube::new);
        this.register("adventure_charm", AdventureCharm::new);
        this.register("antique_bag", AntiqueBag::new);
        this.register("void_tome", VoidTome::new);
        this.register("earth_heart_fragment", EarthHeartFragment::new);
        this.register("ichor_droplet", IchorDroplet::new);
        this.register("ichoroot", Ichoroot::new);
        this.register("pure_heart", PureHeart::new);
        this.register("bless_amplifier", BlessAmplifier::new);
        this.register("thunder_scroll", ThunderScroll::new);
        this.register("bless_stone", BlessStone::new);
        this.register("bless_ring", BlessRing::new);
        this.register("etherium_core", EtheriumCore::new);
        this.register("forger_gem", ForgerGem::new);
        this.register("evil_dagger", EvilDagger::new);
        this.register("common_potion", () -> new UltimatePotionAddon.Base(Rarity.RARE, IAdvancedPotionItem.PotionType.COMMON));
        this.register("common_potion_splash", () -> new UltimatePotionAddon.Splash(Rarity.RARE, IAdvancedPotionItem.PotionType.COMMON));
        this.register("common_potion_lingering", () -> new UltimatePotionAddon.Lingering(Rarity.RARE, IAdvancedPotionItem.PotionType.COMMON));
        this.register("ultimate_potion", () -> new UltimatePotionAddon.Base(Rarity.RARE, IAdvancedPotionItem.PotionType.ULTIMATE));
        this.register("ultimate_potion_splash", () -> new UltimatePotionAddon.Splash(Rarity.RARE, IAdvancedPotionItem.PotionType.ULTIMATE));
        this.register("ultimate_potion_lingering", () -> new UltimatePotionAddon.Lingering(Rarity.RARE, IAdvancedPotionItem.PotionType.ULTIMATE));
    }
}
