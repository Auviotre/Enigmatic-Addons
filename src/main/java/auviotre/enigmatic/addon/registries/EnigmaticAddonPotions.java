package auviotre.enigmatic.addon.registries;

import auviotre.enigmatic.addon.helpers.PotionAddonHelper;
import com.aizistral.enigmaticlegacy.objects.AdvancedPotion;
import com.aizistral.enigmaticlegacy.registries.EnigmaticPotions;
import com.google.common.collect.ImmutableList;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public class EnigmaticAddonPotions {
    /**
     * For Developers:<br>
     * You could add new potion to these lists instead of the EnigmaticLegacy's potion lists by using
     * {@link PotionAddonHelper} which can also deal with related issues.<br><br>
     * I'm not really sure why EnigmaticLegacy use {@link ImmutableList}.<br>
     * If needed, I'll provide a safer method to let you register your own PotionType in the future.<br><br>
     *
     * @see EnigmaticPotions
     **/

    public static final AdvancedPotion FROZEN_HEART = new AdvancedPotion("frozen_heart", new MobEffectInstance(EnigmaticAddonEffects.FROZEN_HEART_EFFECT, 3600));
    public static final AdvancedPotion LONG_FROZEN_HEART = new AdvancedPotion("long_frozen_heart", new MobEffectInstance(EnigmaticAddonEffects.FROZEN_HEART_EFFECT, 9600));
    public static final AdvancedPotion ULTIMATE_FROZEN_HEART = new AdvancedPotion("ultimate_frozen_heart", new MobEffectInstance(EnigmaticAddonEffects.FROZEN_HEART_EFFECT, 19200));
    public static final AdvancedPotion MINING_FATIGUE = new AdvancedPotion("mining_fatigue", new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 3600));
    public static final AdvancedPotion LONG_MINING_FATIGUE = new AdvancedPotion("long_mining_fatigue", new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 9600));
    public static final AdvancedPotion STRONG_MINING_FATIGUE = new AdvancedPotion("strong_mining_fatigue", new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 3600, 1));
    public static final AdvancedPotion ULTIMATE_MINING_FATIGUE = new AdvancedPotion("ultimate_mining_fatigue", new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 9600, 1));
}
