package auviotre.enigmatic.addon.registries;

import auviotre.enigmatic.addon.contents.entities.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;

public class EnigmaticAddonEntities extends AbstractRegistry<EntityType<?>> {
    @ObjectHolder(value = "enigmaticaddons:dragon_breath_arrow", registryName = "entity_type")
    public static final EntityType<DragonBreathArrow> DRAGON_BREATH_ARROW = null;
    @ObjectHolder(value = "enigmaticaddons:cobweb_ball", registryName = "entity_type")
    public static final EntityType<CobwebBall> COBWEB_BALL = null;
    @ObjectHolder(value = "enigmaticaddons:ultimate_dragon_fireball", registryName = "entity_type")
    public static final EntityType<UltimateDragonFireball> ULTIMATE_DRAGON_FIREBALL = null;
    @ObjectHolder(value = "enigmaticaddons:split_dragon_breath", registryName = "entity_type")
    public static final EntityType<SplitDragonBreath> SPLIT_DRAGON_BREATH = null;
    @ObjectHolder(value = "enigmaticaddons:ichor_spear", registryName = "entity_type")
    public static final EntityType<ThrownIchorSpear> ICHOR_SPEAR = null;
    @ObjectHolder(value = "enigmaticaddons:astral_spear", registryName = "entity_type")
    public static final EntityType<ThrownAstralSpear> ASTRAL_SPEAR = null;
    @ObjectHolder(value = "enigmaticaddons:evil_dagger", registryName = "entity_type")
    public static final EntityType<ThrownEvilDagger> EVIL_DAGGER = null;
    @ObjectHolder(value = "enigmaticaddons:disaster_chaos", registryName = "entity_type")
    public static final EntityType<DisasterChaos> DISASTER_CHAOS = null;
    private static final EnigmaticAddonEntities INSTANCE = new EnigmaticAddonEntities();

    private EnigmaticAddonEntities() {
        super(ForgeRegistries.ENTITY_TYPES);
        this.register("dragon_breath_arrow",
                () -> EntityType.Builder.<DragonBreathArrow>of(DragonBreathArrow::new, MobCategory.MISC).sized(0.25F, 0.25F).clientTrackingRange(4)
                        .setCustomClientFactory((spawnEntity, world) -> new DragonBreathArrow(DRAGON_BREATH_ARROW, world)).setUpdateInterval(20)
                        .setShouldReceiveVelocityUpdates(true).build("enigmaticaddons:dragon_breath_arrow"));
        this.register("cobweb_ball",
                () -> EntityType.Builder.<CobwebBall>of(CobwebBall::new, MobCategory.MISC).sized(0.4F, 0.4F).clientTrackingRange(4)
                        .setCustomClientFactory((spawnEntity, world) -> new CobwebBall(DRAGON_BREATH_ARROW, world)).setUpdateInterval(10)
                        .setShouldReceiveVelocityUpdates(true).build("enigmaticaddons:cobweb_ball"));
        this.register("ultimate_dragon_fireball",
                () -> EntityType.Builder.<UltimateDragonFireball>of(UltimateDragonFireball::new, MobCategory.MISC).sized(1.0F, 1.0F).clientTrackingRange(4)
                        .setCustomClientFactory((spawnEntity, world) -> new UltimateDragonFireball(ULTIMATE_DRAGON_FIREBALL, world)).setUpdateInterval(10)
                        .setShouldReceiveVelocityUpdates(true).build("enigmaticaddons:ultimate_dragon_fireball"));
        this.register("split_dragon_breath",
                () -> EntityType.Builder.<SplitDragonBreath>of(SplitDragonBreath::new, MobCategory.MISC).sized(0.4F, 0.4F).clientTrackingRange(4)
                        .setCustomClientFactory((spawnEntity, world) -> new SplitDragonBreath(SPLIT_DRAGON_BREATH, world)).setUpdateInterval(10).fireImmune()
                        .setShouldReceiveVelocityUpdates(true).build("enigmaticaddons:split_dragon_breath"));
        this.register("ichor_spear",
                () -> EntityType.Builder.<ThrownIchorSpear>of(ThrownIchorSpear::new, MobCategory.MISC).sized(0.35F, 0.35F).clientTrackingRange(4)
                        .setCustomClientFactory((spawnEntity, world) -> new ThrownIchorSpear(ICHOR_SPEAR, world)).setUpdateInterval(20)
                        .setShouldReceiveVelocityUpdates(true).build("enigmaticaddons:ichor_spear"));
        this.register("astral_spear",
                () -> EntityType.Builder.<ThrownAstralSpear>of(ThrownAstralSpear::new, MobCategory.MISC).sized(0.45F, 0.45F).clientTrackingRange(24)
                        .setCustomClientFactory((spawnEntity, world) -> new ThrownAstralSpear(ASTRAL_SPEAR, world)).setUpdateInterval(20).fireImmune()
                        .setShouldReceiveVelocityUpdates(true).build("enigmaticaddons:astral_spear"));
        this.register("evil_dagger",
                () -> EntityType.Builder.<ThrownEvilDagger>of(ThrownEvilDagger::new, MobCategory.MISC).sized(0.3F, 0.3F).clientTrackingRange(8)
                        .setCustomClientFactory((spawnEntity, world) -> new ThrownEvilDagger(EVIL_DAGGER, world)).setUpdateInterval(20).fireImmune()
                        .setShouldReceiveVelocityUpdates(true).build("enigmaticaddons:evil_dagger"));
        this.register("disaster_chaos",
                () -> EntityType.Builder.<DisasterChaos>of(DisasterChaos::new, MobCategory.MISC).sized(0.4F, 0.4F).clientTrackingRange(4)
                        .setCustomClientFactory((spawnEntity, world) -> new DisasterChaos(DISASTER_CHAOS, world)).setUpdateInterval(10).fireImmune()
                        .setShouldReceiveVelocityUpdates(true).build("enigmaticaddons:disaster_chaos"));

    }
}
