package arathain.bab.core.component;

import arathain.bab.client.anim.player.GeckoLibPlayer;
import arathain.bab.client.anim.util.BABGeoModel;
import arathain.bab.core.ability.Ability;
import arathain.bab.core.ability.AbilityType;
import net.minecraft.entity.LivingEntity;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;

import java.util.Map;

public interface IAbilityComponent {
    void activateAbility(AbilityType<?> ability);

    void instanceAbilities();

    void tick(LivingEntity entity);

    AbilityType<?>[] getAbilityTypes();

    Map<AbilityType<?>, Ability> getAbilityMap();

    AbilityType<?>[] getAbilities();

    Ability getActiveAbility();

    void setActiveAbility(Ability activeAbility);

    boolean attackingPrevented();

    boolean blockBreakingBuildingPrevented();

    boolean interactingPrevented();

    <E extends IAnimatable> PlayState animationPredicate(AnimationEvent<E> e, GeckoLibPlayer.Perspective perspective);

    void codeAnimations(BABGeoModel<? extends IAnimatable> model, float partialTick);
}
