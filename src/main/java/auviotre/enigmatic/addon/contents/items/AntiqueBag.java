package auviotre.enigmatic.addon.contents.items;

import auviotre.enigmatic.addon.contents.gui.AntiqueBagContainerMenu;
import com.aizistral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBase;
import com.aizistral.omniconfig.wrappers.OmniconfigWrapper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AntiqueBag extends ItemBase {
    public static final List<ResourceLocation> extraBookList = new ArrayList<>();

    @SubscribeConfig
    public static void onConfig(OmniconfigWrapper builder) {
        extraBookList.clear();
        String[] list = builder.config.getStringList("AntiqueBookBagList", "Balance Options", new String[0], "List of items that can be stored in the Antique Book Bag. Examples: enigmaticaddons:false_justice. Changing this option required game restart to take effect.");
        Arrays.stream(list).forEach((entry) -> {
            if (!entry.equals("enigmaticaddons:antique_bag")) extraBookList.add(new ResourceLocation(entry));
        });
    }

    public AntiqueBag() {
        super(ItemBase.getDefaultProperties().stacksTo(1).rarity(Rarity.RARE));
    }


    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> list, TooltipFlag flagIn) {
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.antiqueBag1");
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.antiqueBag2");
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.antiqueBag3");
    }

    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        if (!world.isClientSide) player.openMenu(new AntiqueBagContainerMenu.Provider());
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    public static boolean isBook(ItemStack stack) {
        return extraBookList.contains(ForgeRegistries.ITEMS.getKey(stack.getItem())) || stack.is(ItemTags.BOOKSHELF_BOOKS);
    }
}
