package auviotre.enigmatic.addon.contents.items;

import auviotre.enigmatic.addon.api.items.IBlessed;
import com.aizistral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.aizistral.enigmaticlegacy.api.items.ICursed;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBase;
import com.aizistral.omniconfig.wrappers.Omniconfig;
import com.aizistral.omniconfig.wrappers.OmniconfigWrapper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BlessAmplifier extends ItemBase implements ICursed, IBlessed {
    public static Omniconfig.BooleanParameter amplifyMode;
    public static final List<ResourceLocation> blacklist = new ArrayList<>();
    public BlessAmplifier() {
        super(getDefaultProperties().stacksTo(1).rarity(Rarity.EPIC));
    }

    @SubscribeConfig
    public static void onConfig(@NotNull OmniconfigWrapper builder) {
        builder.pushPrefix("TomeofDivination");
        amplifyMode = builder.comment("When set to false, only the upper limit of ONE level will be exceeded.").getBoolean("AmplifyMode", true);
        blacklist.clear();
        builder.forceSynchronized(true);
        String[] list = builder.config.getStringList("TomeofDivinationBlacklist", "Balance Options", new String[0], "List of enchantments that will not be affected by the Tome of Divination. Examples: minecraft:sharpness. Changing this option required game restart to take effect.");
        Arrays.stream(list).forEach((entry) -> blacklist.add(new ResourceLocation(entry)));
        builder.popPrefix();
    }


    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> list, TooltipFlag flagIn) {
        if (Screen.hasShiftDown()) {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.blessAmplifier1");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.blessAmplifier2");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.blessAmplifier3");
        } else {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
        }

        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
        ItemLoreHelper.indicateCursedOnesOnly(list);
    }
}
