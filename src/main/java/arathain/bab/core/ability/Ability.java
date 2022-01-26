package arathain.bab.core.ability;

import arathain.bab.client.anim.player.GeckoLibPlayer;
import arathain.bab.client.anim.util.BABAnimationController;
import arathain.bab.client.anim.util.BABGeoModel;
import arathain.bab.core.ability.AbilitySection.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;

import java.util.List;
import java.util.Random;

public class Ability {
    private final AbilitySection[] sectionTrack;
    private final int cooldownMax;
    private final AbilityType<? extends Ability> abilityType;
    private final LivingEntity user;
    private final AbilityCapability.IAbilityCapability abilityCapability;

    private int ticksInUse;
    private int ticksInSection;
    private int currentSectionIndex;
    private boolean isUsing;
    private int cooldownTimer;

    protected Random rand;

    @Environment(EnvType.CLIENT)
    protected AnimationBuilder activeThirdPersonAnimation;
    @Environment(EnvType.CLIENT)
    protected AnimationBuilder activeFirstPersonAnimation;

    @Environment(EnvType.CLIENT)
    protected ItemStack heldItemMainHandVisualOverride;
    @Environment(EnvType.CLIENT)
    protected ItemStack heldItemOffHandVisualOverride;

    public enum HandDisplay {
        DEFAULT,
        DONT_RENDER,
        FORCE_RENDER
    }

    @Environment(EnvType.CLIENT)
    protected HandDisplay firstPersonMainHandDisplay;
    @Environment(EnvType.CLIENT)
    protected HandDisplay firstPersonOffHandDisplay;

    public Ability(AbilityType<? extends Ability> abilityType, LivingEntity user, AbilitySection[] sectionTrack, int cooldownMax) {
        this.abilityType = abilityType;
        this.user = user;
        this.abilityCapability = AbilityHandler.INSTANCE.getAbilityCapability(user);
        this.sectionTrack = sectionTrack;
        this.cooldownMax = cooldownMax;
        this.rand = new Random();
        if (user.world.isClient) {
            this.activeThirdPersonAnimation = new AnimationBuilder().addAnimation("idle");
            heldItemMainHandVisualOverride = null;
            heldItemOffHandVisualOverride = null;
            firstPersonMainHandDisplay = HandDisplay.DEFAULT;
            firstPersonOffHandDisplay = HandDisplay.DEFAULT;
        }
    }

    public Ability(AbilityType<? extends Ability> abilityType, LivingEntity user, AbilitySection[] sectionTrack) {
        this(abilityType, user, sectionTrack, 0);
    }

    public void start() {
        if (!runsInBackground()) abilityCapability.setActiveAbility(this);
        ticksInUse = 0;
        ticksInSection = 0;
        currentSectionIndex = 0;
        isUsing = true;
    }

    public void playAnimation(String animationName, GeckoLibPlayer.Perspective perspective, boolean shouldLoop) {
        if (getUser() instanceof PlayerEntity && getUser().world.isRemote()) {
            AnimationBuilder newActiveAnimation = new AnimationBuilder().addAnimation(animationName, shouldLoop);
            if (perspective == GeckoLibPlayer.Perspective.FIRST_PERSON) {
                activeFirstPersonAnimation = newActiveAnimation;
            }
            else {
                activeThirdPersonAnimation = newActiveAnimation;
            }
            BABAnimationController<GeckoLibPlayer> controller = GeckoLibPlayer.getAnimationController((PlayerEntity) getUser(), perspective);
            GeckoLibPlayer geckoPlayer = GeckoLibPlayer.getGeckoLibPlayer((PlayerEntity) getUser(), perspective);
            if (controller != null && geckoPlayer != null) {
                controller.playAnimation(geckoPlayer, newActiveAnimation);
            }
        }
    }

    public void playAnimation(String animationName, boolean shouldLoop) {
        playAnimation(animationName, GeckoLibPlayer.Perspective.FIRST_PERSON, shouldLoop);
        playAnimation(animationName, GeckoLibPlayer.Perspective.THIRD_PERSON, shouldLoop);
    }

    public void tick() {
        if (isUsing()) {
            if (getUser().canMoveVoluntarily() && !canContinueUsing()) AbilityHandler.INSTANCE.sendInterruptAbilityMessage(getUser(), this.abilityType);

            tickUsing();

            ticksInUse++;
            ticksInSection++;
            AbilitySection section = getCurrentSection();
            if (section instanceof AbilitySectionInstant) {
                nextSection();
            } else if (section instanceof AbilitySectionDuration) {
                AbilitySectionDuration sectionDuration = (AbilitySectionDuration) section;
                if (ticksInSection > sectionDuration.duration) nextSection();
            }
        }
        else {
            tickNotUsing();
            if (getCooldownTimer() > 0) cooldownTimer--;
        }
    }

    public void tickUsing() {

    }

    public void tickNotUsing() {

    }

    public void end() {
        ticksInUse = 0;
        ticksInSection = 0;
        isUsing = false;
        cooldownTimer = getMaxCooldown();
        currentSectionIndex = 0;
        if (!runsInBackground()) abilityCapability.setActiveAbility(null);

        if (getUser().world.isClient) {
            heldItemMainHandVisualOverride = null;
            heldItemOffHandVisualOverride = null;
            firstPersonMainHandDisplay = HandDisplay.DEFAULT;
            firstPersonOffHandDisplay = HandDisplay.DEFAULT;
        }
    }

    public void interrupt() {
        end();
    }

    public void complete() {
        end();
    }

    /**
     * Server-only check to see if the user can use this ability. Checked before packet is sent.
     * @return Whether or not the ability can be used
     */
    public boolean canUse() {
        boolean nonBackgroundCheck = runsInBackground() || abilityCapability.getActiveAbility() == null || canCancelActiveAbility();
        return (!isUsing() || canCancelActiveAbility()) && cooldownTimer == 0 && nonBackgroundCheck;
    }

    /**
     * Both sides check and behavior when user tries to use this ability. Ability only starts if this returns true.
     * Called after packet is received.
     * @return Whether or not the ability try succeeded
     */
    public boolean tryAbility() {
        return true;
    }

    public boolean canCancelActiveAbility() {
        return false;
    }

    protected boolean canContinueUsing() {
        return true;
    }

    public boolean isUsing() {
        return isUsing;
    }

    public LivingEntity getUser() {
        return user;
    }

    public int getTicksInUse() {
        return ticksInUse;
    }

    public int getTicksInSection() {
        return ticksInSection;
    }

    public int getCooldownTimer() {
        return cooldownTimer;
    }

    public void nextSection() {
        jumpToSection(currentSectionIndex + 1);
    }

    public void jumpToSection(int sectionIndex) {
        currentSectionIndex = sectionIndex;
        ticksInSection = 0;
        if (currentSectionIndex >= getSectionTrack().length) {
            complete();
        }
        else {
            beginSection(getCurrentSection());
        }
    }

    protected void beginSection(AbilitySection section) {

    }

    public AbilitySection getCurrentSection() {
        if (currentSectionIndex >= getSectionTrack().length) return null;
        return getSectionTrack()[currentSectionIndex];
    }

    /**
     * Non-background abilities require no other non-background abilities running to run.
     * Only one non-background ability can run at once.
     * Background abilities can all run simultaneously
     * @return
     */
    public boolean runsInBackground() {
        return false;
    }

    /**
     * Unused for background abilities
     * @return
     */
    public boolean preventsAttacking() {
        return true;
    }

    /**
     * Unused for background abilities
     * @return
     */
    public boolean preventsBlockBreakingBuilding() {
        return true;
    }

    /**
     * Unused for background abilities
     * @return
     */
    public boolean preventsInteracting() {
        return true;
    }

    public AbilitySection[] getSectionTrack() {
        return sectionTrack;
    }

    public int getMaxCooldown() {
        return cooldownMax;
    }

    //TODO replace with CCA
    public AbilityCapability.IAbilityCapability getAbilityCapability() {
        return abilityCapability;
    }

    public <E extends IAnimatable> PlayState animationPredicate(AnimationEvent<E> e, GeckoLibPlayer.Perspective perspective) {
        AnimationBuilder whichAnimation;
        if (perspective == GeckoLibPlayer.Perspective.FIRST_PERSON) {
            whichAnimation = activeFirstPersonAnimation;
        }
        else {
            whichAnimation = activeThirdPersonAnimation;
        }
        if (whichAnimation == null || whichAnimation.getRawAnimationList().isEmpty())
            return PlayState.STOP;
        e.getController().setAnimation(whichAnimation);
        return PlayState.CONTINUE;
    }

    public void codeAnimations(BABGeoModel<? extends IAnimatable> model, float partialTick) {

    }

    public boolean isAnimating() {
        return isUsing();
    }

    public List<LivingEntity> getEntityLivingBaseNearby(LivingEntity player, double distanceX, double distanceY, double distanceZ, double radius) {
        return getEntitiesNearby(player, LivingEntity.class, distanceX, distanceY, distanceZ, radius);
    }

    public <T extends Entity> List<T> getEntitiesNearby(LivingEntity player, Class<T> entityClass, double r) {
        return player.world.getEntitiesByClass(entityClass, player.getBoundingBox().expand(r, r, r), e -> e != player && player.distanceTo(e) <= r);
    }

    public <T extends Entity> List<T> getEntitiesNearby(LivingEntity player, Class<T> entityClass, double dX, double dY, double dZ, double r) {
        return player.world.getEntitiesByClass(entityClass, player.getBoundingBox().expand(dX, dY, dZ), e -> e != player && player.distanceTo(e) <= r);
    }

    @Environment(EnvType.CLIENT)
    public ItemStack heldItemMainHandOverride() {
        return heldItemMainHandVisualOverride;
    }

    @Environment(EnvType.CLIENT)
    public ItemStack heldItemOffHandOverride() {
        return heldItemOffHandVisualOverride;
    }

    @Environment(EnvType.CLIENT)
    public HandDisplay getFirstPersonMainHandDisplay() {
        return firstPersonMainHandDisplay;
    }

    @Environment(EnvType.CLIENT)
    public HandDisplay getFirstPersonOffHandDisplay() {
        return firstPersonOffHandDisplay;
    }

    public NbtCompound writeNBT() {
        NbtCompound compound = new NbtCompound();
        if (isUsing()) {
            compound.putInt("ticks_in_use", ticksInUse);
            compound.putInt("ticks_in_section", ticksInSection);
            compound.putInt("current_section", currentSectionIndex);
        }
        else if (cooldownTimer > 0) {
            compound.putInt("cooldown_timer", cooldownTimer);
        }
        return compound;
    }

    public void readNBT(NbtElement nbt) {
        NbtCompound compound = (NbtCompound) nbt;
        isUsing = compound.contains("ticks_in_use");
        if (isUsing) {
            ticksInUse = compound.getInt("ticks_in_use");
            ticksInSection = compound.getInt("ticks_in_section");
            currentSectionIndex = compound.getInt("current_section");
        }
        else {
            cooldownTimer = compound.getInt("cooldown_timer");
        }
    }

    // Events, this will need a restructuring

    public void onRightClickWithItem(PlayerInteractEvent.RightClickItem event) {

    }

    public void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {

    }

    public void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {

    }

    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {

    }

    public void onLeftClickEntity(AttackEntityEvent event) {

    }

    public void onTakeDamage(LivingHurtEvent event) {

    }

    public void onJump(LivingEvent.LivingJumpEvent event) {

    }

    public void onRightMouseDown(PlayerEntity player) {

    }

    public void onLeftMouseDown(PlayerEntity player) {

    }

    public void onRightMouseUp(PlayerEntity player) {

    }

    public void onLeftMouseUp(PlayerEntity player) {

    }

    public void onSneakDown(PlayerEntity player) {

    }

    public void onSneakUp(PlayerEntity player) {

    }

    public void onRenderTick(ClientTickEvents.EndTick event) {

    }
}
