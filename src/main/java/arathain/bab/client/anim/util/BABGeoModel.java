package arathain.bab.client.anim.util;

import com.eliotlash.molang.MolangParser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.IAnimationTickable;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.resource.GeckoLibCache;

import java.util.Collections;

/**
 * Taken from https://github.com/BobMowzie/MowziesMobs/blob/master/src/main/java/com/bobmowzie/mowziesmobs/client/model/tools/geckolib/MowzieAnimatedGeoModel.java
 * **/

public abstract class BABGeoModel <T extends IAnimatable & IAnimationTickable> extends AnimatedGeoModel<T> {
    public BABGeoModel() {
    }

    public BABBone getBABBone(String boneName) {
        IBone bone = this.getBone(boneName);
        return (BABBone) bone;
    }

    public boolean isInitialized() {
        return !this.getAnimationProcessor().getModelRendererList().isEmpty();
    }

    @Override
    public void setLivingAnimations(T entity, Integer uniqueID, @Nullable AnimationEvent customPredicate) {
        // Each animation has it's own collection of animations (called the
        // EntityAnimationManager), which allows for multiple independent animations
        AnimationData manager = entity.getFactory().getOrCreateAnimationData(uniqueID);
        if (manager.startTick == null) {
            manager.startTick = (double) (entity.tickTimer() + MinecraftClient.getInstance().getTickDelta());    // Set start ticks when animation starts playing
        }

        if (!MinecraftClient.getInstance().isPaused() || manager.shouldPlayWhilePaused) {
            manager.tick = (entity.tickTimer() + MinecraftClient.getInstance().getTickDelta());
            double gameTick = manager.tick;
            double deltaTicks = gameTick - lastGameTickTime;
            seekTime += deltaTicks;
            lastGameTickTime = gameTick;
        }

        AnimationEvent<T> predicate;
        if (customPredicate == null) {
            predicate = new AnimationEvent<T>(entity, 0, 0, 0, false, Collections.emptyList());
        } else {
            predicate = customPredicate;
        }

        predicate.animationTick = seekTime;
        getAnimationProcessor().preAnimationSetup(predicate.getAnimatable(), seekTime);
        if (!this.getAnimationProcessor().getModelRendererList().isEmpty()) {
        //TODO make this run   getAnimationProcessor().tickAnimation(entity, uniqueID, seekTime, predicate, (MolangParser) GeckoLibCache.getInstance().parser, shouldCrashOnMissing);
        }

        if (!MinecraftClient.getInstance().isPaused() || manager.shouldPlayWhilePaused) {
            codeAnimations(entity, uniqueID, customPredicate);
        }
    }

    public void codeAnimations(T entity, Integer uniqueID, AnimationEvent<?> customPredicate) {

    }

    public boolean resourceForModelId(AbstractClientPlayerEntity player) {
        return true;
    }

    public float getControllerValue(String controllerName) {
        if (!isInitialized()) return 1.0f;
        return 1.0f - getBone(controllerName).getPositionX();
    }
}
