package arathain.bab.client.anim.player;

import arathain.bab.client.anim.player.model.GeckoLibPlayerFirstPersonModel;
import arathain.bab.client.anim.player.model.GeckoLibPlayerThirdPersonModel;
import arathain.bab.client.anim.util.BABAnimationController;
import arathain.bab.client.anim.util.BABGeoModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.IAnimationTickable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;
import software.bernie.geckolib3.util.GeckoLibUtil;


/**
 * Taken from https://github.com/BobMowzie/MowziesMobs/blob/master/src/main/java/com/bobmowzie/mowziesmobs/client/render/entity/player/GeckoPlayer.java
 * **/
@Environment(EnvType.CLIENT)
public abstract class GeckoLibPlayer implements IAnimatable, IAnimationTickable {
    protected IGeoRenderer<GeckoLibPlayer> renderer;
    protected BABGeoModel<GeckoLibPlayer> model;

    private int tickTimer = 0;

    private PlayerEntity player;
    private AnimationFactory factory = new AnimationFactory(this);
    public static final String THIRD_PERSON_CONTROLLER_NAME = "thirdPersonAnimation";
    public static final String FIRST_PERSON_CONTROLLER_NAME = "firstPersonAnimation";

    public enum Perspective {
        FIRST_PERSON,
        THIRD_PERSON
    }

    public GeckoLibPlayer(PlayerEntity player) {
        this.player = player;
        setup(player);
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new BABAnimationController<>(this, getControllerName(), 0, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }

    public PlayerEntity getPlayer() {
        return player;
    }

    @Override
    public void tick() {
        tickTimer++;
    }

    @Override
    public int tickTimer() {
        return tickTimer;
    }

    public <E extends IAnimatable> PlayState predicate(AnimationEvent<E> e) {
        e.getController().transitionLengthTicks = 0;
        PlayerEntity player = getPlayer();
        if (player == null) {
            return PlayState.STOP;
        }
        AbilityCapability.IAbilityCapability abilityCapability = AbilityHandler.INSTANCE.getAbilityCapability(player);
        if (abilityCapability == null) {
            return PlayState.STOP;
        }

        if (abilityCapability.getActiveAbility() != null) {
            return abilityCapability.animationPredicate(e, getPerspective());
        }
        else {
            e.getController().setAnimation(new AnimationBuilder().addAnimation("idle"));
            return PlayState.CONTINUE;
        }
    }

    @Nullable
    public static GeckoLibPlayer getGeckoLibPlayer(PlayerEntity player, Perspective perspective) {
        if (perspective == Perspective.FIRST_PERSON) return GeckoLibFirstPersonRenderer.GECKO_PLAYER_FIRST_PERSON;
        PlayerCapability.IPlayerCapability playerCapability = CapabilityHandler.getCapability(player, PlayerCapability.PlayerProvider.PLAYER_CAPABILITY);
        if (playerCapability != null) {
            return playerCapability.getGeckoPlayer();
        }
        return null;
    }

    public static BABAnimationController<GeckoLibPlayer> getAnimationController(PlayerEntity player, Perspective perspective) {
        PlayerCapability.IPlayerCapability playerCapability = CapabilityHandler.getCapability(player, PlayerCapability.PlayerProvider.PLAYER_CAPABILITY);
        if (playerCapability != null) {
            GeckoLibPlayer geckoPlayer;
            if (perspective == Perspective.FIRST_PERSON) geckoPlayer = GeckoFirstPersonRenderer.GECKO_PLAYER_FIRST_PERSON;
            else geckoPlayer = playerCapability.getGeckoPlayer();
            if (geckoPlayer != null) {
                String name = perspective == Perspective.FIRST_PERSON ? FIRST_PERSON_CONTROLLER_NAME : THIRD_PERSON_CONTROLLER_NAME;
                return (BABAnimationController<GeckoLibPlayer>) GeckoLibUtil.getControllerForID(geckoPlayer.getFactory(), player.getUniqueID().hashCode(), name);
            }
        }
        return null;
    }

    public IGeoRenderer<GeckoLibPlayer> getPlayerRenderer() {
        return renderer;
    }

    public BABGeoModel<GeckoLibPlayer> getModel() {
        return model;
    }

    public abstract String getControllerName();

    public abstract Perspective getPerspective();

    public abstract void setup(PlayerEntity player);

    public static class GeckoLibPlayerFirstPerson extends GeckoLibPlayer {
        public GeckoLibPlayerFirstPerson(PlayerEntity player) {
            super(player);
        }

        @Override
        public String getControllerName() {
            return FIRST_PERSON_CONTROLLER_NAME;
        }

        @Override
        public Perspective getPerspective() {
            return Perspective.FIRST_PERSON;
        }

        @Override
        public void setup(PlayerEntity player) {
            GeckoLibPlayerFirstPersonModel modelGeckoPlayer = new GeckoLibPlayerFirstPersonModel();
            model = modelGeckoPlayer;
            model.resourceForModelId((AbstractClientPlayerEntity) player);
            GeckoLibFirstPersonRenderer geckoRenderer = new GeckoLibFirstPersonRenderer(MinecraftClient.getInstance(), modelGeckoPlayer);
            renderer = geckoRenderer;
            if (!geckoRenderer.getModelsToLoad().containsKey(this.getClass())) {
                geckoRenderer.getModelsToLoad().put(this.getClass(), geckoRenderer);
            }
        }
    }

    public static class GeckoPlayerThirdPerson extends GeckoLibPlayer {
        public GeckoPlayerThirdPerson(PlayerEntity player) {
            super(player);
        }

        @Override
        public String getControllerName() {
            return THIRD_PERSON_CONTROLLER_NAME;
        }

        @Override
        public Perspective getPerspective() {
            return Perspective.THIRD_PERSON;
        }

        @Override
        public void setup(PlayerEntity player) {
            GeckoLibPlayerThirdPersonModel modelGeckoPlayer = new GeckoLibPlayerThirdPersonModel();
            model = modelGeckoPlayer;
            model.resourceForModelId((AbstractClientPlayerEntity) player);
            GeckoLibPlayerRenderer geckoRenderer = new GeckoLibPlayerRenderer(MinecraftClient.getInstance().getEntityRenderDispatcher(), modelGeckoPlayer);
            renderer = geckoRenderer;
            if (!geckoRenderer.getModelsToLoad().containsKey(this.getClass())) {
                geckoRenderer.getModelsToLoad().put(this.getClass(), geckoRenderer);
            }
        }
    }

}
