package auviotre.enigmatic.addon.contents.crafting;

import auviotre.enigmatic.addon.EnigmaticAddons;
import auviotre.enigmatic.addon.handlers.OmniconfigAddonHandler;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;
import net.minecraftforge.registries.ForgeRegistries;

public class EnabledCondition implements ICondition {
    private static final ResourceLocation ID = new ResourceLocation(EnigmaticAddons.MODID, "is_enabled");
    private final ResourceLocation item;

    public EnabledCondition(ResourceLocation item) {
        this.item = item;
    }

    public ResourceLocation getID() {
        return ID;
    }

    public boolean test(ICondition.IContext context) {
        return OmniconfigAddonHandler.isItemEnabled(ForgeRegistries.ITEMS.getValue(this.item));
    }

    public static class Serializer implements IConditionSerializer<EnabledCondition> {
        public static final EnabledCondition.Serializer INSTANCE = new EnabledCondition.Serializer();

        public Serializer() {
        }

        public void write(JsonObject json, EnabledCondition value) {
            json.addProperty("item", value.item.toString());
        }

        public EnabledCondition read(JsonObject json) {
            return new EnabledCondition(new ResourceLocation(GsonHelper.getAsString(json, "item")));
        }

        public ResourceLocation getID() {
            return EnabledCondition.ID;
        }
    }
}

