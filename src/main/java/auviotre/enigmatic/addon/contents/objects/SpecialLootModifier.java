package auviotre.enigmatic.addon.contents.objects;

import auviotre.enigmatic.addon.handlers.OmniconfigAddonHandler;
import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
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
        if (entity instanceof ServerPlayer player) {
            boolean isNether = SuperpositionHandler.getNetherDungeons().stream().anyMatch((table) -> table.equals(context.getQueriedLootTableId()));
            boolean isOver = SuperpositionHandler.getOverworldDungeons().stream().anyMatch((table) -> table.equals(context.getQueriedLootTableId()));

            if (isNether && player.getRandom().nextInt(3) == 0 && !SuperpositionHandler.hasPersistentTag(player, "LootedHellCharm")) {
                if (OmniconfigAddonHandler.isItemEnabled(EnigmaticAddonItems.HELL_BLADE_CHARM) && generatedLoot.stream().anyMatch(stack -> stack.is(EnigmaticAddonItems.ICHOR_DROPLET))) {
                    generatedLoot.removeIf(stack -> stack.is(EnigmaticAddonItems.ICHOR_DROPLET));
                    generatedLoot.add(EnigmaticAddonItems.HELL_BLADE_CHARM.getDefaultInstance());
                    SuperpositionHandler.setPersistentBoolean(player, "LootedHellCharm", true);
                }
            }
            if (isOver && player.getRandom().nextBoolean() && !SuperpositionHandler.hasPersistentTag(player, "LootedVoidTome")) {
                if (OmniconfigAddonHandler.isItemEnabled(EnigmaticAddonItems.VOID_TOME) && generatedLoot.stream().anyMatch(stack -> stack.is(EnigmaticAddonItems.FORGER_GEM))) {
                    generatedLoot.removeIf(stack -> stack.is(EnigmaticAddonItems.FORGER_GEM));
                    generatedLoot.add(EnigmaticAddonItems.VOID_TOME.getDefaultInstance());
                    SuperpositionHandler.setPersistentBoolean(player, "LootedVoidTome", true);
                }
            }

            if (isOver && player.getRandom().nextInt(100000) == 0) {
                if (generatedLoot.stream().anyMatch(stack -> stack.is(EnigmaticAddonItems.EARTH_HEART_FRAGMENT))) {
                    generatedLoot.removeIf(stack -> stack.is(EnigmaticAddonItems.EARTH_HEART_FRAGMENT));
                    generatedLoot.add(EnigmaticAddonItems.ENIGMATIC_PEARL.getDefaultInstance());
                }
            }
        }
        return generatedLoot;
    }

    public Codec<SpecialLootModifier> codec() {
        return CODEC.get();
    }
}
