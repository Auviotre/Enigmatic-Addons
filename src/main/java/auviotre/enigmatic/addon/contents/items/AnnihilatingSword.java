package auviotre.enigmatic.addon.contents.items;

import com.aizistral.enigmaticlegacy.api.items.IEldritch;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeTier;

public class AnnihilatingSword extends SwordItem implements IEldritch {
     private static final Tier TIER = new ForgeTier(6, 2568, 10.8F, 6.0F, 64, BlockTags.NEEDS_DIAMOND_TOOL, () -> Ingredient.EMPTY);
    public AnnihilatingSword() {
        super(TIER, 4, -2.8F, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant());
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    public int getUseDuration(ItemStack stack) {
        return 100;
    }

    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BLOCK;
    }
}
