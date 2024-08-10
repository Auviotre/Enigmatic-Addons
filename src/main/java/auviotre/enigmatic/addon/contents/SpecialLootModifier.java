package auviotre.enigmatic.addon.contents;

import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import com.aizistral.enigmaticlegacy.config.OmniconfigHandler;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;
import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class SpecialLootModifier extends LootModifier {
    public static final Supplier<Codec<SpecialLootModifier>> CODEC = Suppliers.memoize(() -> RecordCodecBuilder.create((inst) -> codecStart(inst).apply(inst, SpecialLootModifier::new)));

    protected SpecialLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, @NotNull LootContext context) {
        ServerLevel level = context.getLevel();
        Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        Vec3 origin = context.getParamOrNull(LootContextParams.ORIGIN);
        if (entity instanceof ServerPlayer player) {
            if (SuperpositionHandler.hasPersistentTag(player, "LootedHellCharm")) {
                generatedLoot.removeIf((stack) -> stack.is(EnigmaticAddonItems.HELL_BLADE_CHARM));
            } else if (generatedLoot.stream().anyMatch((stack) -> stack.is(EnigmaticAddonItems.HELL_BLADE_CHARM))) {
                SuperpositionHandler.setPersistentBoolean(player, "LootedHellCharm", true);
            }
            if (OmniconfigHandler.isItemEnabled(EnigmaticItems.EARTH_HEART) && level.random.nextInt(3) == 0 && generatedLoot.stream().anyMatch((stack) -> stack.is(EnigmaticItems.IRON_RING))) {
                generatedLoot.removeIf((stack) -> stack.is(EnigmaticItems.IRON_RING));
                generatedLoot.add(new ItemStack(EnigmaticAddonItems.EARTH_HEART_FRAGMENT, level.random.nextInt(2) + 1));
            }
        }

        return generatedLoot;
    }

    public Codec<SpecialLootModifier> codec() {
        return CODEC.get();
    }
}
