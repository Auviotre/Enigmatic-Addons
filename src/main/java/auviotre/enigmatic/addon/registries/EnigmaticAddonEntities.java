package auviotre.enigmatic.addon.registries;

import auviotre.enigmatic.addon.contents.entities.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;

public class EnigmaticAddonEntities extends AbstractRegistry<EntityType<?>> {
    private static final EnigmaticAddonEntities INSTANCE = new EnigmaticAddonEntities();
    @ObjectHolder(
            value = "enigmaticaddons:dragon_breath_arrow",
            registryName = "entity_type"
    )
    public static final EntityType<DragonBreathArrow> DRAGON_BREATH_ARROW = null;
    @ObjectHolder(
            value = "enigmaticaddons:enigmatic_potion_entity",
            registryName = "entity_type"
    )
    public static final EntityType<UltimatePotionEntity> ENIGMATIC_POTION = null;
    @ObjectHolder(
            value = "enigmaticaddons:cobweb_ball",
            registryName = "entity_type"
    )
    public static final EntityType<CobwebBall> COBWEB_BALL = null;
    @ObjectHolder(
            value = "enigmaticaddons:ultimate_dragon_fireball",
            registryName = "entity_type"
    )
    public static final EntityType<UltimateDragonFireball> ULTIMATE_DRAGON_FIREBALL = null;
    @ObjectHolder(
            value = "enigmaticaddons:split_dragon_breath",
            registryName = "entity_type"
    )
    public static final EntityType<SplitDragonBreath> SPLIT_DRAGON_BREATH = null;

    private EnigmaticAddonEntities() {
        super(ForgeRegistries.ENTITY_TYPES);
        this.register("dragon_breath_arrow",
                () -> EntityType.Builder.<DragonBreathArrow>of(DragonBreathArrow::new, MobCategory.MISC).sized(0.25F, 0.25F).setTrackingRange(64)
                        .setCustomClientFactory((spawnEntity, world) -> new DragonBreathArrow(DRAGON_BREATH_ARROW, world))
                        .setShouldReceiveVelocityUpdates(true).build("enigmaticaddons:dragon_breath_arrow")
        );
        this.register("enigmatic_potion_entity",
                () -> EntityType.Builder.<UltimatePotionEntity>of(UltimatePotionEntity::new, MobCategory.MISC).sized(0.25F, 0.25F).setTrackingRange(64)
                        .setCustomClientFactory((spawnEntity, world) -> new UltimatePotionEntity(ENIGMATIC_POTION, world)).setUpdateInterval(10)
                        .setShouldReceiveVelocityUpdates(true).build("enigmaticaddons:enigmatic_potion_entity"));
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
                        .setCustomClientFactory((spawnEntity, world) -> new SplitDragonBreath(SPLIT_DRAGON_BREATH, world)).setUpdateInterval(10)
                        .setShouldReceiveVelocityUpdates(true).build("enigmaticaddons:split_dragon_breath"));

    }
}
