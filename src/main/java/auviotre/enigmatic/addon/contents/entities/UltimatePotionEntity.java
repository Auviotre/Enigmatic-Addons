package auviotre.enigmatic.addon.contents.entities;

import auviotre.enigmatic.addon.contents.items.UltimatePotionAddon;
import auviotre.enigmatic.addon.helpers.PotionAddonHelper;
import com.aizistral.enigmaticlegacy.helpers.PotionHelper;
import com.aizistral.enigmaticlegacy.registries.EnigmaticEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;

public class UltimatePotionEntity extends ThrowableItemProjectile implements ItemSupplier {
    private static final EntityDataAccessor<ItemStack> ITEM = SynchedEntityData.defineId(UltimatePotionEntity.class, EntityDataSerializers.ITEM_STACK);

    public UltimatePotionEntity(EntityType<UltimatePotionEntity> type, Level world) {
        super(type, world);
    }

    public UltimatePotionEntity(Level world, LivingEntity entity) {
        super(EnigmaticEntities.ENIGMATIC_POTION, entity, world);
    }

    public UltimatePotionEntity(Level world, double x, double y, double z) {
        super(EnigmaticEntities.ENIGMATIC_POTION, x, y, z, world);
    }

    protected void defineSynchedData() {
        this.getEntityData().define(ITEM, ItemStack.EMPTY);
    }

    public ItemStack getItem() {
        ItemStack itemstack = this.getEntityData().get(ITEM);
        return PotionHelper.isAdvancedPotion(itemstack) ? itemstack : new ItemStack(Items.SPLASH_POTION);
    }

    public void setItem(ItemStack stack) {
        this.getEntityData().set(ITEM, stack.copy());
    }

    protected float getGravity() {
        return 0.05F;
    }

    public void tick() {
        super.tick();
    }

    protected void onHit(HitResult result) {
        if (!this.level().isClientSide) {
            ItemStack itemstack = this.getItem();
            List<MobEffectInstance> list = PotionAddonHelper.getEffects(itemstack);
            int i = 2002;
            if (list != null && !list.isEmpty()) {
                if (this.isLingering()) {
                    this.makeAreaOfEffectCloud(itemstack, list);
                } else {
                    this.triggerSplash(list, result.getType() == HitResult.Type.ENTITY ? ((EntityHitResult) result).getEntity() : null);
                }

                for (MobEffectInstance instance : list) {
                    if (instance.getEffect().isInstantenous()) {
                        i = 2007;
                    }
                }
            }

            this.level().levelEvent(i, new BlockPos(this.blockPosition()), PotionAddonHelper.getColor(itemstack));
            this.discard();
        }

    }

    private void triggerSplash(List<MobEffectInstance> instances, @Nullable Entity entity) {
        AABB axis = this.getBoundingBox().inflate(4.0, 2.0, 4.0);
        List<LivingEntity> list = this.level().getEntitiesOfClass(LivingEntity.class, axis);
        if (!list.isEmpty()) {
            Iterator<LivingEntity> var5 = list.iterator();

            while (true) {
                LivingEntity livingentity;
                double d0;
                do {
                    do {
                        if (!var5.hasNext()) {
                            return;
                        }

                        livingentity = var5.next();
                    } while (!livingentity.isAffectedByPotions());

                    d0 = this.distanceToSqr(livingentity);
                } while (!(d0 < 16.0));

                double d1 = 1.0 - Math.sqrt(d0) / 4.0;
                if (livingentity == entity) {
                    d1 = 1.0;
                }

                for (MobEffectInstance effectInstance : instances) {
                    MobEffect effect = effectInstance.getEffect();
                    if (effect.isInstantenous()) {
                        effect.applyInstantenousEffect(this, this.getOwner(), livingentity, effectInstance.getAmplifier(), d1);
                    } else {
                        int i = (int) (d1 * (double) effectInstance.getDuration() + 0.5);
                        if (i > 20) {
                            livingentity.addEffect(new MobEffectInstance(effect, i, effectInstance.getAmplifier(), effectInstance.isAmbient(), effectInstance.isVisible()));
                        }
                    }
                }
            }
        }
    }

    private void makeAreaOfEffectCloud(ItemStack stack, List<MobEffectInstance> list) {
        AreaEffectCloud cloudEntity = new AreaEffectCloud(this.level(), this.getX(), this.getY(), this.getZ());
        cloudEntity.setOwner((LivingEntity) this.getOwner());
        cloudEntity.setRadius(3.0F);
        cloudEntity.setRadiusOnUse(-0.5F);
        cloudEntity.setWaitTime(10);
        cloudEntity.setRadiusPerTick(-cloudEntity.getRadius() / (float) cloudEntity.getDuration());

        for (MobEffectInstance effectInstance : list) {
            cloudEntity.addEffect(new MobEffectInstance(effectInstance.getEffect(), effectInstance.getDuration() / 4, effectInstance.getAmplifier(), effectInstance.isAmbient(), effectInstance.isVisible()));
        }

        cloudEntity.setFixedColor(PotionAddonHelper.getColor(stack));
        this.level().addFreshEntity(cloudEntity);
    }

    private boolean isLingering() {
        return this.getItem().getItem() instanceof UltimatePotionAddon.Lingering;
    }

    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        ItemStack itemstack = ItemStack.of(compound.getCompound("Potion"));
        if (itemstack.isEmpty()) {
            this.discard();
        } else {
            this.setItem(itemstack);
        }

    }

    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        ItemStack itemstack = this.getItem();
        if (!itemstack.isEmpty()) {
            compound.put("Potion", itemstack.save(new CompoundTag()));
        }

    }

    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    protected Item getDefaultItem() {
        return Items.SPLASH_POTION;
    }
}