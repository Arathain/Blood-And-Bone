package arathain.bab.core.init;

import arathain.bab.BloodAndBone;
import arathain.bab.core.component.AbilityComponent;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import net.minecraft.util.Identifier;

public class BABComponents implements EntityComponentInitializer {
    public static final ComponentKey<AbilityComponent> ABILITY_COMPONENT = ComponentRegistry.getOrCreate(new Identifier(BloodAndBone.MODID, "ability"), AbilityComponent.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(ABILITY_COMPONENT, AbilityComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
    }
}
