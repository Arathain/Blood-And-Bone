package arathain.bab.core.ability.abilities;

import arathain.bab.client.anim.player.GeckoLibPlayer;
import arathain.bab.core.ability.Ability;
import arathain.bab.core.ability.AbilitySection;
import arathain.bab.core.ability.AbilityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Arm;

public class SimpleAnimationAbility extends Ability {
    private String animationName;
    private boolean separateLeftAndRight;
    private boolean lockHeldItemMainHand;

    public SimpleAnimationAbility(AbilityType<SimpleAnimationAbility> abilityType, LivingEntity user, String animationName, int duration) {
        super(abilityType, user, new AbilitySection[] {
                new AbilitySection.AbilitySectionInstant(AbilitySection.AbilitySectionType.ACTIVE),
                new AbilitySection.AbilitySectionDuration(AbilitySection.AbilitySectionType.RECOVERY, duration)
        });
        this.animationName = animationName;
    }

    public SimpleAnimationAbility(AbilityType<SimpleAnimationAbility> abilityType, LivingEntity user, String animationName, int duration, boolean separateLeftAndRight, boolean lockHeldItemMainHand) {
        super(abilityType, user, new AbilitySection[] {
                new AbilitySection.AbilitySectionInstant(AbilitySection.AbilitySectionType.ACTIVE),
                new AbilitySection.AbilitySectionDuration(AbilitySection.AbilitySectionType.RECOVERY, duration)
        });
        this.animationName = animationName;
        this.separateLeftAndRight = separateLeftAndRight;
        this.lockHeldItemMainHand = lockHeldItemMainHand;
    }

    @Override
    public void start() {
        super.start();
        if (separateLeftAndRight) {
            boolean handSide = getUser().getMainArm() == Arm.RIGHT;
            playAnimation(animationName + "_" + (handSide ? "right" : "left"), GeckoLibPlayer.Perspective.THIRD_PERSON, false);
            playAnimation(animationName, GeckoLibPlayer.Perspective.FIRST_PERSON, false);
        }
        else {
            playAnimation(animationName, false);
        }
        if (lockHeldItemMainHand)
            heldItemMainHandVisualOverride = getUser().getMainHandStack();
    }
}
