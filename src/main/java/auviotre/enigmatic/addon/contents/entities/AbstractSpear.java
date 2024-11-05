package auviotre.enigmatic.addon.contents.entities;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public abstract class AbstractSpear extends AbstractArrow {
    private final ItemStack itemStack;

    public AbstractSpear(EntityType<? extends AbstractArrow> type, Level world, Item item) {
        super(type, world);
        this.itemStack = item.getDefaultInstance();
    }

    public AbstractSpear(EntityType<? extends AbstractArrow> type, LivingEntity entity, Level world, Item item) {
        super(type, entity, world);
        this.itemStack = item.getDefaultInstance();
    }

    public AbstractSpear(EntityType<? extends AbstractArrow> type, double x, double y, double z, Level world, Item item) {
        super(type, x, y, z, world);
        this.itemStack = item.getDefaultInstance();
    }

    protected boolean tryPickup(Player player) {
        return super.tryPickup(player) || this.isNoPhysics() && this.ownedBy(player) && player.getInventory().add(this.getPickupItem());
    }

    public void playerTouch(Player player) {
        if (this.ownedBy(player) || this.getOwner() == null) {
            super.playerTouch(player);
        }
    }

    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.TRIDENT_HIT_GROUND;
    }

    public ItemStack getItem() {
        return this.itemStack;
    }

    public boolean shouldRender(double x, double y, double z) {
        return true;
    }

    protected float getWaterInertia() {
        return 0.9F;
    }
}
