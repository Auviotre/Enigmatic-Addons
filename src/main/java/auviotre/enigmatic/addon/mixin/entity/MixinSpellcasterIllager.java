package auviotre.enigmatic.addon.mixin.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.SpellcasterIllager;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SpellcasterIllager.class)
public abstract class MixinSpellcasterIllager extends AbstractIllager {
    protected MixinSpellcasterIllager(EntityType<? extends AbstractIllager> type, Level level) {
        super(type, level);
    }
}
