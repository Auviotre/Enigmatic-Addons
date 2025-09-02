package auviotre.enigmatic.addon.handlers;

import auviotre.enigmatic.addon.EnigmaticAddons;
import auviotre.enigmatic.addon.helpers.MixinOmniconfigHelper;
import com.aizistral.enigmaticlegacy.api.items.IPerhaps;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.omniconfig.Configuration;
import com.aizistral.omniconfig.wrappers.Omniconfig;
import com.aizistral.omniconfig.wrappers.OmniconfigWrapper;
import com.google.common.base.Objects;
import com.google.common.collect.Multimap;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class OmniconfigAddonHandler {
    public static final Map<Field, Omniconfig.BooleanParameter> ITEMS_OPTIONS = new HashMap<>();
    public static Omniconfig.BooleanParameter frostParticle;
    public static Omniconfig.BooleanParameter etheriumShieldIcon;
    public static Omniconfig.BooleanParameter EnableCurseBoost;
    public static Omniconfig.BooleanParameter ImmediatelyCurseBoost;
    public static Omniconfig.BooleanParameter NearDeathAnger;
    public static Omniconfig.BooleanParameter TabResorted;
    public static Omniconfig.BooleanParameter FutureItemDisplay;
    public static Omniconfig.BooleanParameter HiddenRecipeJEIDisplay;

    public static boolean isItemEnabled(Object item) {
        if (item == null) {
            return false;
        } else {
            for (Field optionalItemField : ITEMS_OPTIONS.keySet()) {
                try {
                    if (optionalItemField.get(null) != null) {
                        Object optionalItem = optionalItemField.get(null);
                        if (Objects.equal(item, optionalItem) && ITEMS_OPTIONS.get(optionalItemField) != null) {
                            return ITEMS_OPTIONS.get(optionalItemField).getValue();
                        }
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
            return !(item instanceof IPerhaps) || ((IPerhaps) item).isForMortals();
        }
    }

    public static void initialize() {
        OmniconfigWrapper configBuilder = OmniconfigWrapper.setupBuilder("enigmaticaddons-common", true, "1.2.5.2");
        configBuilder.pushVersioningPolicy(Configuration.VersioningPolicy.AGGRESSIVE);
        configBuilder.pushTerminateNonInvokedKeys(true);
        loadCommon(configBuilder);
        configBuilder.setReloadable();
        OmniconfigWrapper clientBuilder = OmniconfigWrapper.setupBuilder("enigmaticaddons-client", true, "1.2.5.2");
        clientBuilder.pushSidedType(Configuration.SidedConfigType.CLIENT);
        clientBuilder.pushVersioningPolicy(Configuration.VersioningPolicy.AGGRESSIVE);
        clientBuilder.pushTerminateNonInvokedKeys(true);
        loadClient(clientBuilder);
        clientBuilder.setReloadable();
    }


    private static void loadClient(OmniconfigWrapper client) {
        client.loadConfigFile();
        client.pushCategory("Generic Config", "Some more different stuff");
        frostParticle = client.comment("If false, disables the particle effect for fully frozen entities.").clientOnly().getBoolean("CustomFrostParticle", true);
        etheriumShieldIcon = client.comment("If false, disables the icon display of the Etherium Shield.").clientOnly().getBoolean("EtheriumShieldIconDisplay", true);
        client.popCategory();
        client.build();
    }

    private static void loadCommon(OmniconfigWrapper builder) {
        builder.loadConfigFile();
        builder.forceSynchronized(true);
        builder.pushCategory("Accessibility Options", "You may disable certain items or features from being obtainable/usable here." + System.lineSeparator() + "Check more details in Enigmatic Legacy's Config Files.");
        Multimap<String, Field> accessibilityGeneratorMap = SuperpositionHandler.retainAccessibilityGeneratorMap(EnigmaticAddons.MODID);
        ITEMS_OPTIONS.clear();

        for (String itemName : accessibilityGeneratorMap.keySet()) {
            String optionName = itemName.replaceAll("[^a-zA-Z0-9]", "") + "Enabled";
            Omniconfig.BooleanParameter param = builder.comment("Whether or not " + itemName + " should be enabled.").getBoolean(optionName, true);
            for (Field associatedField : accessibilityGeneratorMap.get(itemName)) {
                ITEMS_OPTIONS.put(associatedField, param);
            }
        }

        builder.popCategory();
        builder.forceSynchronized(true);
        builder.pushCategory("Balance Options", "Various options that mostly affect individual items");
        SuperpositionHandler.dispatchWrapperToHolders(EnigmaticAddons.MODID, builder);
        builder.popCategory();
        builder.forceSynchronized(true);
        builder.pushCategory("Legacy Balance Options", "Various options that mostly affect Enigmatic Legacy's items");
        builder.pushPrefix("");
        MixinOmniconfigHelper.MixConfig(builder);
        builder.popCategory();
        builder.forceSynchronized(true);
        builder.pushCategory("The Worthy One Options", "Various options that about the Events related to The Worthy One.");
        EnableCurseBoost = builder.comment("If true, When the proportion of curse time is long enough, there will be some changes in creatures' AI.").getBoolean("EnableCurseBoost", true);
        ImmediatelyCurseBoost = builder.comment("If true, when a creature spawned, it will immediately gain Curse Boost.").getBoolean("ImmediatelyCurseBoost", false);
        NearDeathAnger = builder.comment("If true, when a creature dies, it will anger its surrounding peers.").getBoolean("PartnerAnger", true);
        builder.popCategory();
        builder.forceSynchronized(false);
        builder.pushCategory("Else Options", "Various options that relates to secondary contents.");
        TabResorted = builder.comment("If true, the main creative tab of Enigmatic Legacy will be resorted.").getBoolean("TabResorted", true);
        FutureItemDisplay = builder.comment("If true, the main creative tab of Enigmatic Legacy will display some uncompleted item.").getBoolean("FutureItemDisplay", false);
        HiddenRecipeJEIDisplay = builder.comment("If true, the Hidden Recipe will display in JEI.").getBoolean("HiddenRecipeJEIDisplay", true);
        builder.popCategory();
        builder.build();
    }

}
