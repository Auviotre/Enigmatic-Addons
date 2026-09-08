package auviotre.enigmatic.addon.contents.entities;

import auviotre.enigmatic.addon.registries.EnigmaticAddonEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class ExplorerMarker extends Entity implements OwnableEntity {
    protected static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID = SynchedEntityData.defineId(ExplorerMarker.class, EntityDataSerializers.OPTIONAL_UUID);
    private int lifetime = 0;

    public ExplorerMarker(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    public ExplorerMarker(Level level, BlockPos pos, LivingEntity owner) {
        super(EnigmaticAddonEntities.EXPLORER_MARKER, level);
        this.setGlowingTag(true);
        this.setOwnerUUID(owner.getUUID());
        this.setPos(Vec3.atBottomCenterOf(pos));
        this.lifetime = 180;
    }

    public void tick() {
        if (!this.level().isClientSide()) {
            this.lifetime--;
            if (lifetime <= 0) this.discard();
            BlockEntity blockEntity = this.level().getBlockEntity(BlockPos.containing(this.getX(), this.getEyeY(), this.getZ()));
            if (!(blockEntity instanceof RandomizableContainerBlockEntity entity) || entity.lootTable == null)
                this.discard();
        }
    }

    protected void defineSynchedData() {
        this.entityData.define(DATA_OWNER_UUID, Optional.empty());
    }

    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        if (compoundTag.hasUUID("Owner")) {
            UUID uuid = compoundTag.getUUID("Owner");
            this.setOwnerUUID(uuid);
        }
    }

    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        if (this.getOwnerUUID() != null) {
            compoundTag.putUUID("Owner", this.getOwnerUUID());
        }
    }

    public @Nullable UUID getOwnerUUID() {
        return this.entityData.get(DATA_OWNER_UUID).orElse(null);
    }

    public void setOwnerUUID(@Nullable UUID uuid) {
        this.entityData.set(DATA_OWNER_UUID, Optional.ofNullable(uuid));
    }
}
