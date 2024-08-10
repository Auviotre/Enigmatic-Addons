package auviotre.enigmatic.addon.handlers;

import auviotre.enigmatic.addon.api.events.LivingCurseBoostEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class SuperAddonHandler {
    public static boolean isCurseBoosted(LivingEntity entity) {
        return entity.getPersistentData().getBoolean("CurseBoost");
    }

    public static void setCurseBoosted(LivingEntity entity, Boolean flag, Player player) {
        LivingCurseBoostEvent event = AddonHookHandler.onLivingCurseBoosted(entity, player);
        if (!event.isCanceled()) entity.getPersistentData().putBoolean("CurseBoost", flag);
    }

    public static AABB getBoundingBoxAroundEntity(Entity entity, double x, double y, double z) {
        return new AABB(entity.getX() - x, entity.getY() - y, entity.getZ() - z, entity.getX() + x, entity.getY() + y, entity.getZ() + z);
    }

    @Nullable
    public static LootPoolSingletonContainer.Builder<?> createOptionalLootEntry(Item item, int weight, float minCount, float maxCount) {
        return !OmniconfigAddonHandler.isItemEnabled(item) ? null : LootItem.lootTableItem(item).setWeight(weight).apply(SetItemCountFunction.setCount(UniformGenerator.between(minCount, maxCount)));
    }

    @Nullable
    public static LootPoolSingletonContainer.Builder<?> createOptionalLootEntry(Item item, int weight) {
        return !OmniconfigAddonHandler.isItemEnabled(item) ? null : LootItem.lootTableItem(item).setWeight(weight);
    }

    public static List<ResourceLocation> getIceDungeons() {
        List<ResourceLocation> lootChestList = new ArrayList<>();
        lootChestList.add(BuiltInLootTables.IGLOO_CHEST);
        return lootChestList;
    }
    public static List<ResourceLocation> getJungleDungeons() {
        List<ResourceLocation> lootChestList = new ArrayList<>();
        lootChestList.add(BuiltInLootTables.JUNGLE_TEMPLE);
        return lootChestList;
    }
    public static List<ResourceLocation> getEngineDungeons() {
        List<ResourceLocation> lootChestList = new ArrayList<>();
        lootChestList.add(BuiltInLootTables.SIMPLE_DUNGEON);
        lootChestList.add(BuiltInLootTables.UNDERWATER_RUIN_SMALL);
        lootChestList.add(BuiltInLootTables.UNDERWATER_RUIN_BIG);
        lootChestList.add(BuiltInLootTables.STRONGHOLD_CORRIDOR);
        lootChestList.add(BuiltInLootTables.STRONGHOLD_CROSSING);
        return lootChestList;
    }

    public static List<ResourceLocation> getNetherDungeons() {
        List<ResourceLocation> lootChestList = new ArrayList<>();
        lootChestList.add(BuiltInLootTables.NETHER_BRIDGE);
        lootChestList.add(BuiltInLootTables.BASTION_TREASURE);
        lootChestList.add(BuiltInLootTables.BASTION_OTHER);
        lootChestList.add(BuiltInLootTables.BASTION_BRIDGE);
        lootChestList.add(BuiltInLootTables.BASTION_HOGLIN_STABLE);
        return lootChestList;
    }
}
