package auviotre.enigmatic.addon.mixin;

import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import auviotre.enigmatic.addon.registries.EnigmaticAddonParticles;
import com.aizistral.enigmaticlegacy.entities.PermanentItemEntity;
import com.aizistral.enigmaticlegacy.helpers.ItemNBTHelper;
import com.aizistral.enigmaticlegacy.registries.EnigmaticBlocks;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemEntity.class)
public abstract class MixinItemEntity extends Entity implements TraceableEntity {
    @Shadow
    private int pickupDelay;
    @Unique
    private boolean enigmaticAddons$primeCubeOn = false;
    @Unique
    private int enigmaticAddons$primeCubeTick = 0;

    public MixinItemEntity(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Shadow
    public abstract ItemStack getItem();

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    public void hurtMix(DamageSource source, float value, CallbackInfoReturnable<Boolean> cir) {
        if (this.isInvulnerableTo(source)) {
            cir.setReturnValue(false);
        } else {
            ItemStack stack = this.getItem();
            if (!stack.isEmpty() && stack.is(EnigmaticAddonItems.BLESS_STONE) && source.is(DamageTypeTags.IS_LIGHTNING)) {
                if (this.level().getLevelData().isHardcore() && !ItemNBTHelper.getBoolean(stack, "Hardcore", false)) {
                    stack.enchant(Enchantments.UNBREAKING, 10);
                    ItemNBTHelper.setBoolean(stack, "Hardcore", true);
                }
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tickMix(CallbackInfo ci) {
        Level world = this.level();
        if (this.getItem().is(EnigmaticAddonItems.PRIMEVAL_CUBE)) {
            BlockPos blockPos = this.blockPosition();
            if (world.getBlockState(blockPos).isAir() && world.getBlockState(blockPos.above()).isAir()) {
                if (this.onGround() && world.getBlockState(blockPos.below()).is(EnigmaticBlocks.ASTRAL_BLOCK)) {
                    this.enigmaticAddons$primeCubeOn = true;
                    this.setGlowingTag(true);
                    this.setNoGravity(true);
                    this.setDeltaMovement(0.0, 0.0, 0.0);
                    world.destroyBlock(blockPos.below(), false);
                }
            }
            if (this.enigmaticAddons$primeCubeOn && !world.isClientSide()) {
                ServerLevel server = (ServerLevel) world;
                if (this.random.nextInt(3) == 0)
                    server.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY(), this.getZ(), 1, 0.0, 0.0, 0.0, 0.02D);
                this.pickupDelay = 20;
                this.enigmaticAddons$primeCubeTick++;
                this.hasImpulse = true;
                this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.001 + 0.0002 * this.enigmaticAddons$primeCubeTick, 0.0));
                if (this.enigmaticAddons$primeCubeTick >= 45) {
                    List<Item> spellstones = List.of(
                            EnigmaticItems.GOLEM_HEART,
                            EnigmaticItems.EYE_OF_NEBULA,
                            EnigmaticItems.BLAZING_CORE,
                            EnigmaticItems.VOID_PEARL,
                            EnigmaticItems.OCEAN_STONE,
                            EnigmaticItems.ANGEL_BLESSING,
                            EnigmaticAddonItems.FORGOTTEN_ICE,
                            EnigmaticAddonItems.REVIVAL_LEAF,
                            EnigmaticAddonItems.LOST_ENGINE
                    );
                    ItemStack stack = new ItemStack(spellstones.get(this.random.nextInt(spellstones.size())));
                    if (this.random.nextInt(9) == 0) stack = new ItemStack(EnigmaticItems.ANGEL_BLESSING);
                    else if (this.random.nextInt(9) == 0) stack = new ItemStack(EnigmaticItems.VOID_PEARL);
                    PermanentItemEntity itemEntity = new PermanentItemEntity(world, this.getX(), this.getY(), this.getZ(), stack);
                    itemEntity.setGlowingTag(true);
                    server.sendParticles(ParticleTypes.FLASH, this.getX(), this.getY(0.5), this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
                    server.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(0.5), this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
                    server.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY(0.5), this.getZ(), 24, 0.0, 0.0, 0.0, 0.25D);
                    world.playSound(null, this, SoundEvents.BEACON_ACTIVATE, SoundSource.AMBIENT, 5.0F, 1.5F);
                    world.addFreshEntity(itemEntity);
                    this.discard();
                }
            }
        } else if (this.getItem().is(EnigmaticAddonItems.BLESS_STONE) && !world.isClientSide() && ItemNBTHelper.getBoolean(this.getItem(), "Hardcore", false)) {
            ServerLevel server = (ServerLevel) world;
            if (this.random.nextInt(3) == 0)
                server.sendParticles(EnigmaticAddonParticles.ICHOR, this.getX(), this.getY(0.5), this.getZ(), 1, 0.2, 0.2, 0.2, 0.02D);
        }
    }
}
