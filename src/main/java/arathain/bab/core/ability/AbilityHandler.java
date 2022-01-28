package arathain.bab.core.ability;

import arathain.bab.core.ability.abilities.SimpleAnimationAbility;
import arathain.bab.core.component.AbilityComponent;
import arathain.bab.core.init.BABComponents;
import arathain.bab.core.network.packet.PlayerUseAbilityPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;

public enum AbilityHandler {
    INSTANCE;

    public static final AbilityType<SimpleAnimationAbility> BACKSTAB_ABILITY = new AbilityType<>("backstab", (type, player) ->
            new SimpleAnimationAbility(type, player, "backstab", 12, true, true)
    );
    public static final AbilityType<?>[] PLAYER_ABILITIES = new AbilityType[] {
            BACKSTAB_ABILITY
    };
    @Nullable
    public AbilityComponent getAbilityComponent(LivingEntity entity) {
        return BABComponents.ABILITY_COMPONENT.get(entity);
    }

    @Nullable
    public Ability getAbility(LivingEntity entity, AbilityType<?> abilityType) {
        AbilityComponent abilityComponent = getAbilityComponent(entity);
        if (abilityComponent != null) {
            return abilityComponent.getAbilityMap().get(abilityType);
        }
        return null;
    }

    public <T extends LivingEntity> void sendAbilityMessage(T entity, AbilityType<?> abilityType) {
        if (entity.world.isClient) {
            return;
        }
        AbilityComponent abilityComponent = getAbilityComponent(entity);
        if (abilityComponent != null) {
            Ability instance = abilityComponent.getAbilityMap().get(abilityType);
            if (instance.canUse()) {
                abilityComponent.activateAbility(abilityType);
                MowziesMobs.NETWORK.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), new MessageUseAbility(entity.getEntityId(), ArrayUtils.indexOf(abilityCapability.getAbilityTypesOnEntity(entity), abilityType)));
            }
        }
    }

    public <T extends LivingEntity> void sendInterruptAbilityMessage(T entity, AbilityType<?> abilityType) {
        if (entity.world.isClient) {
            return;
        }
        AbilityComponent abilityCapability = getAbilityComponent(entity);
        if (abilityCapability != null) {
            Ability instance = abilityCapability.getAbilityMap().get(abilityType);
            if (instance.isUsing()) {
                instance.interrupt();
                MowziesMobs.NETWORK.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), new MessageInterruptAbility(entity.getEntityId(), ArrayUtils.indexOf(abilityCapability.getAbilityTypesOnEntity(entity), abilityType)));
            }
        }
    }

    public <T extends PlayerEntity> void sendPlayerTryAbilityMessage(T entity, AbilityType<?> ability) {
        if (!(entity.world.isClient && entity instanceof ClientPlayerEntity)) {
            return;
        }
        AbilityComponent abilityCapability = getAbilityComponent(entity);
        if (abilityCapability != null) {
            PlayerUseAbilityPacket.send(ArrayUtils.indexOf(abilityCapability.getAbilityTypes(), ability));
        }
    }
}
