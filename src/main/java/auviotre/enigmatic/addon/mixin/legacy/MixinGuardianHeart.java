package auviotre.enigmatic.addon.mixin.legacy;

import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.items.GuardianHeart;
import com.aizistral.enigmaticlegacy.items.generic.ItemBase;
import com.aizistral.enigmaticlegacy.objects.Vector3;
import com.aizistral.enigmaticlegacy.packets.clients.PacketGenericParticleEffect;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static com.aizistral.enigmaticlegacy.items.GuardianHeart.*;

@Pseudo
@Mixin(GuardianHeart.class)
public abstract class MixinGuardianHeart extends ItemBase {

    @Shadow(remap = false)
    protected abstract void setAttackTarget(Monster monster, Monster otherMonster);

    @Shadow(remap = false)
    protected abstract boolean isExcluded(LivingEntity entity);

    @Inject(method = "inventoryTick", at = @At("RETURN"))
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int itemSlot, boolean isSelected, CallbackInfo ci) {
        if (entity instanceof Player player && !world.isClientSide) {
            List<Monster> genericMobs = player.level().getEntitiesOfClass(Monster.class, SuperpositionHandler.getBoundingBoxAroundEntity(player, abilityRange.getValue()));
            List<Monster> surroundingMobs;
            Monster closestMonster;
            Monster theOne;
            if (SuperAddonHandler.isTheBlessedOne(player) && Inventory.isHotbarSlot(itemSlot) && !player.getCooldowns().isOnCooldown(this)) {
                Monster oneWatched = genericMobs.stream().filter(monster -> SuperpositionHandler.doesObserveEntity(player, monster) && !this.isExcluded(monster)).findFirst().orElse(null);

                if (oneWatched != null && oneWatched.isAlive()) {
                    theOne = oneWatched;
                    Vector3 vec = Vector3.fromEntityCenter(oneWatched);
                    surroundingMobs = player.level().getEntitiesOfClass(Monster.class, SuperpositionHandler.getBoundingBoxAroundEntity(oneWatched, enrageRange.getValue()), (living) -> living.isAlive() && oneWatched.hasLineOfSight(living));
                    closestMonster = SuperpositionHandler.getClosestEntity(surroundingMobs, (monsters) -> monsters != oneWatched, vec.x, vec.y, vec.z);
                    if (closestMonster != null) {
                        this.setAttackTarget(oneWatched, closestMonster);
                        oneWatched.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 300, 0, false, true));
                        oneWatched.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 1, false, false));
                        oneWatched.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 1, false, false));

                        for (Monster surroundingMob : surroundingMobs) {
                            this.setAttackTarget(surroundingMob, theOne);
                        }

                        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.HOSTILE, 1.0F, 1.0F);
                        if (player instanceof ServerPlayer) {
                            Monster finalTheOne = theOne;
                            EnigmaticLegacy.packetInstance.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(finalTheOne.getX(), finalTheOne.getY(), finalTheOne.getZ(), 64.0, finalTheOne.level().dimension())), new PacketGenericParticleEffect(theOne.getX(), theOne.getEyeY(), theOne.getZ(), 0, false, PacketGenericParticleEffect.Effect.GUARDIAN_CURSE));
                        }

                        player.getCooldowns().addCooldown(this, abilityCooldown.getValue());
                    }
                }
            }

            for (Monster genericMob : genericMobs) {
                theOne = genericMob;
                if (theOne instanceof Guardian guardian && theOne.getClass() != ElderGuardian.class) {
                    if (guardian.getTarget() == null) {
                        surroundingMobs = player.level().getEntitiesOfClass(Monster.class, SuperpositionHandler.getBoundingBoxAroundEntity(guardian, 12.0), (living) -> living.isAlive() && guardian.hasLineOfSight(living));
                        closestMonster = SuperpositionHandler.getClosestEntity(surroundingMobs, (checked) -> !(checked instanceof Guardian), guardian.getX(), guardian.getY(), guardian.getZ());
                        if (closestMonster != null) {
                            this.setAttackTarget(guardian, closestMonster);
                        }
                    }
                }
            }
        }
    }
}
