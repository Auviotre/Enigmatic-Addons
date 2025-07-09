package auviotre.enigmatic.addon.registries;

import auviotre.enigmatic.addon.contents.items.*;
import com.aizistral.enigmaticlegacy.api.generic.ConfigurableItem;
import com.aizistral.enigmaticlegacy.api.items.IAdvancedPotionItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.registries.RegisterEvent;

public class EnigmaticAddonItems extends AbstractRegistry<Item> {
    @ConfigurableItem("Ode to Living Beings")
    @ObjectHolder(value = "enigmaticaddons:ode_to_living", registryName = "item")
    public static final OdeToLiving LIVING_ODE = null;
    @ConfigurableItem("Pact of Dark Night")
    @ObjectHolder(value = "enigmaticaddons:night_scroll", registryName = "item")
    public static final NightScroll NIGHT_SCROLL = null;
    @ConfigurableItem("Magic Quartz Ring")
    @ObjectHolder(value = "enigmaticaddons:quartz_ring", registryName = "item")
    public static final QuartzRing QUARTZ_RING = null;
    @ConfigurableItem("Magic Quartz Scepter")
    @ObjectHolder(value = "enigmaticaddons:quartz_scepter", registryName = "item")
    public static final QuartzScepter QUARTZ_SCEPTER = null;
    @ConfigurableItem("Dragon Breath Bow")
    @ObjectHolder(value = "enigmaticaddons:dragon_bow", registryName = "item")
    public static final DragonBow DRAGON_BOW = null;
    @ConfigurableItem("Astral Spear")
    @ObjectHolder(value = "enigmaticaddons:astral_spear", registryName = "item")
    public static final AstralSpear ASTRAL_SPEAR = null;
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
    @ConfigurableItem("Soul Lantern of Illusion")
    @ObjectHolder(value = "enigmaticaddons:illusion_lantern", registryName = "item")
    public static final IllusionLantern ILLUSION_LANTERN = null;
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
    @ConfigurableItem("Scepter of Extradimensional")
    @ObjectHolder(value = "enigmaticaddons:extradimensional_scepter", registryName = "item")
    public static final ExtradimensionalScepter EXTRADIMENSIONAL_SCEPTER = null;
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
    @ConfigurableItem("Magic Quartz Flower")
    @ObjectHolder(value = "enigmaticaddons:artificial_flower", registryName = "item")
    public static final ArtificialFlower ARTIFICIAL_FLOWER = null;
    @ConfigurableItem("Emblem of Adventurer")
    @ObjectHolder(value = "enigmaticaddons:adventure_charm", registryName = "item")
    public static final AdventureCharm ADVENTURE_CHARM = null;
    @ConfigurableItem("Insignia of Despair")
    @ObjectHolder(value = "enigmaticaddons:despair_insignia", registryName = "item")
    public static final DespairInsignia DESPAIR_INSIGNIA = null;
    @ConfigurableItem("Ichor Droplet")
    @ObjectHolder(value = "enigmaticaddons:ichor_droplet", registryName = "item")
    public static final IchorDroplet ICHOR_DROPLET = null;
    @ConfigurableItem("Ichoroot")
    @ObjectHolder(value = "enigmaticaddons:ichoroot", registryName = "item")
    public static final Ichoroot ICHOROOT = null;
    @ConfigurableItem("Ichor Spear")
    @ObjectHolder(value = "enigmaticaddons:ichor_spear", registryName = "item")
    public static final IchorSpear ICHOR_SPEAR = null;
    @ConfigurableItem("Pure Heart")
    @ObjectHolder(value = "enigmaticaddons:pure_heart", registryName = "item")
    public static final PureHeart PURE_HEART = null;
    @ConfigurableItem("The Bless")
    @ObjectHolder(value = "enigmaticaddons:the_bless", registryName = "item")
    public static final TheBless THE_BLESS = null;
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
    @ConfigurableItem("Charm of Scorched Sun")
    @ObjectHolder(value = "enigmaticaddons:scorched_charm", registryName = "item")
    public static final ScorchedCharm SCORCHED_CHARM = null;
    @ConfigurableItem("The Curse Carver")
    @ObjectHolder(value = "enigmaticaddons:evil_dagger", registryName = "item")
    public static final EvilDagger EVIL_DAGGER = null;
    @ObjectHolder(value = "enigmaticaddons:quartz_dagger", registryName = "item")
    public static final Item QUARTZ_DAGGER = null;
    @ConfigurableItem("Totem of Malice")
    @ObjectHolder(value = "enigmaticaddons:totem_of_malice", registryName = "item")
    public static final TotemOfMalice TOTEM_OF_MALICE = null;
    @ConfigurableItem("Disaster Broadsword")
    @ObjectHolder(value = "enigmaticaddons:disaster_sword", registryName = "item")
    public static final DisasterSword DISASTER_SWORD = null;
    @ConfigurableItem("The Arrogance of Chaos")
    @ObjectHolder(value = "enigmaticaddons:chaos_elytra", registryName = "item")
    public static final ChaosElytra CHAOS_ELYTRA = null;
    @ObjectHolder(value = "enigmaticaddons:annihilating_sword", registryName = "item")
    public static final Item ANNIHILATING_SWORD = null;
    @ObjectHolder(value = "enigmaticaddons:violence_scroll", registryName = "item")
    public static final ViolenceScroll VIOLENCE_SCROLL = null;
    @ConfigurableItem("Enigmatic Pearl")
    @ObjectHolder(value = "enigmaticaddons:enigmatic_pearl", registryName = "item")
    public static final Item ENIGMATIC_PEARL = null;
    @ObjectHolder(value = "enigmaticaddons:soul_flame_ball", registryName = "item")
    public static final Item SOUL_FLAME_BALL = null;
    private static final EnigmaticAddonItems INSTANCE = new EnigmaticAddonItems();

    private EnigmaticAddonItems() {
        super(ForgeRegistries.ITEMS);
        this.register("enigmatic_pearl", EnigmaticPearl::new);
        this.register("soul_flame_ball", () -> new Item(new Item.Properties().stacksTo(1)));
        this.register("ode_to_living", OdeToLiving::new);
        this.register("night_scroll", NightScroll::new);
        this.register("quartz_ring", QuartzRing::new);
        this.register("quartz_scepter", QuartzScepter::new);
        this.register("quartz_dagger", () -> new Item(new Item.Properties().stacksTo(1)));
        this.register("dragon_bow", DragonBow::new);
        this.register("astral_spear", AstralSpear::new);
        this.register("earth_promise", EarthPromise::new);
        this.register("forgotten_ice", ForgottenIce::new);
        this.register("lost_engine", LostEngine::new);
        this.register("revival_leaf", RevivalLeaf::new);
        this.register("illusion_lantern", IllusionLantern::new);
        this.register("false_justice", FalseJustice::new);
        this.register("sanguinary_handbook", SanguinaryHandbook::new);
        this.register("cursed_xp_scroll", CursedXPScroll::new);
        this.register("hell_blade_charm", HellBladeCharm::new);
        this.register("astral_potion", AstralPotion::new);
        this.register("primeval_cube", PrimevalCube::new);
        this.register("adventure_charm", AdventureCharm::new);
        this.register("despair_insignia", DespairInsignia::new);
        this.register("antique_bag", AntiqueBag::new);
        this.register("artificial_flower", ArtificialFlower::new);
        this.register("void_tome", VoidTome::new);
        this.register("earth_heart_fragment", EarthHeartFragment::new);
        this.register("ichor_droplet", IchorDroplet::new);
        this.register("ichoroot", Ichoroot::new);
        this.register("ichor_spear", IchorSpear::new);
        this.register("pure_heart", PureHeart::new);
        this.register("the_bless", TheBless::new);
        this.register("bless_amplifier", BlessAmplifier::new);
        this.register("thunder_scroll", ThunderScroll::new);
        this.register("bless_stone", BlessStone::new);
        this.register("bless_ring", BlessRing::new);
        this.register("etherium_core", EtheriumCore::new);
        this.register("forger_gem", ForgerGem::new);
        this.register("scorched_charm", ScorchedCharm::new);
        this.register("evil_dagger", EvilDagger::new);
        this.register("totem_of_malice", TotemOfMalice::new);
        this.register("disaster_sword", DisasterSword::new);
        this.register("chaos_elytra", ChaosElytra::new);
        this.register("annihilating_sword", AnnihilatingSword::new);
        this.register("violence_scroll", ViolenceScroll::new);
        this.register("extradimensional_scepter", ExtradimensionalScepter::new);
        this.register("common_potion", () -> new UltimatePotionAddon.Base(Rarity.RARE, IAdvancedPotionItem.PotionType.COMMON));
        this.register("common_potion_splash", () -> new UltimatePotionAddon.Splash(Rarity.RARE, IAdvancedPotionItem.PotionType.COMMON));
        this.register("common_potion_lingering", () -> new UltimatePotionAddon.Lingering(Rarity.RARE, IAdvancedPotionItem.PotionType.COMMON));
        this.register("ultimate_potion", () -> new UltimatePotionAddon.Base(Rarity.RARE, IAdvancedPotionItem.PotionType.ULTIMATE));
        this.register("ultimate_potion_splash", () -> new UltimatePotionAddon.Splash(Rarity.RARE, IAdvancedPotionItem.PotionType.ULTIMATE));
        this.register("ultimate_potion_lingering", () -> new UltimatePotionAddon.Lingering(Rarity.RARE, IAdvancedPotionItem.PotionType.ULTIMATE));
    }

    protected void onRegister(RegisterEvent event) {
        EnigmaticAddonBlocks.getBlockItemMap().forEach((block, item) -> event.register(ForgeRegistries.Keys.ITEMS, block, () -> item.apply(ForgeRegistries.BLOCKS.getValue(block))));
    }
}
