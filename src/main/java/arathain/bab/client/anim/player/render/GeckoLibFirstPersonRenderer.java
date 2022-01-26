package arathain.bab.client.anim.player.render;

import arathain.bab.client.anim.player.GeckoLibPlayer;
import arathain.bab.client.anim.player.model.GeckoLibPlayerFirstPersonModel;
import arathain.bab.client.anim.util.BABBone;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.IAnimatableModel;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoCube;
import software.bernie.geckolib3.model.provider.GeoModelProvider;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;
import software.bernie.geckolib3.util.RenderUtils;

import java.util.HashMap;
import java.util.Iterator;

public class GeckoLibFirstPersonRenderer extends HeldItemRenderer implements IGeoRenderer<GeckoLibPlayer> {
    public static GeckoLibPlayer.GeckoLibPlayerFirstPerson GECKO_PLAYER_FIRST_PERSON;

    private static HashMap<Class<? extends GeckoLibPlayer>, GeckoLibFirstPersonRenderer> modelsToLoad = new HashMap<>();
    private GeckoLibPlayerFirstPersonModel modelProvider;
    boolean mirror;
    public GeckoLibFirstPersonRenderer(MinecraftClient client, GeckoLibPlayerFirstPersonModel model) {
        super(client);
        this.modelProvider = model;
    }
    static {
        AnimationController.addModelFetcher((IAnimatable object) -> {
            if (object instanceof GeckoLibPlayer.GeckoLibPlayerFirstPerson) {
                GeckoLibFirstPersonRenderer render = modelsToLoad.get(object.getClass());
                return (IAnimatableModel<Object>) render.getGeoModelProvider();
            } else {
                return null;
            }
        });
    }

    public void renderFirstPersonItem(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, GeckoLibPlayer geckoPlayer) {
        boolean flag = hand == Hand.MAIN_HAND;
        Arm arm = flag ? player.getMainArm() : player.getMainArm().getOpposite();
        mirror = player.getMainArm() == Arm.LEFT;

        if (flag) {
            this.modelProvider.setLivingAnimations(geckoPlayer, player.getUuid().hashCode());

            RenderLayer rendertype = RenderLayer.getItemEntityTranslucentCull(getTextureLocation(geckoPlayer));
            VertexConsumer ivertexbuilder = vertexConsumers.getBuffer(rendertype);
            matrices.translate(0, -2, -1);
            render(
                    getGeoModelProvider().getModel(getGeoModelProvider().getModelLocation(geckoPlayer)),
                    geckoPlayer, tickDelta, rendertype, matrices, vertexConsumers, ivertexbuilder, light, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F
            );
        }

        Ability.HandDisplay handDisplay = Ability.HandDisplay.DEFAULT;
        float offHandEquipProgress = 0.0f;
        AbilityCapability.IAbilityCapability abilityCapability = AbilityHandler.INSTANCE.getAbilityCapability(player);
        if (abilityCapability != null && abilityCapability.getActiveAbility() != null) {
            Ability ability = abilityCapability.getActiveAbility();
            ItemStack stackOverride = flag ? ability.heldItemMainHandOverride() : ability.heldItemOffHandOverride();
            if (stackOverride != null) stack = stackOverride;

            handDisplay = flag ? ability.getFirstPersonMainHandDisplay() : ability.getFirstPersonOffHandDisplay();

            if (ability.getCurrentSection().sectionType == AbilitySection.AbilitySectionType.STARTUP)
                offHandEquipProgress = MathHelper.clamp(1f - (ability.getTicksInSection() + tickDelta) / 5f, 0f, 1f);
            else if (ability.getCurrentSection().sectionType == AbilitySection.AbilitySectionType.RECOVERY && ability.getCurrentSection() instanceof AbilitySection.AbilitySectionDuration)
                offHandEquipProgress = MathHelper.clamp((ability.getTicksInSection() + tickDelta - ((AbilitySection.AbilitySectionDuration)ability.getCurrentSection()).duration + 5) / 5f, 0f, 1f);
        }

        if (handDisplay != Ability.HandDisplay.DONT_RENDER && modelProvider.isInitialized()) {
            int sideMult = arm == Arm.RIGHT ? -1 : 1;
            if (mirror) arm = arm.getOpposite();
            String sideName = arm == Arm.RIGHT ? "Right" : "Left";
            String boneName = sideName + "Arm";
            BABBone bone = this.modelProvider.getBABBone(boneName);

            MatrixStack newMatrixStack = new MatrixStack();

            float fixedPitchController = 1f - this.modelProvider.getControllerValue("FixedPitchController" + sideName);
            newMatrixStack.multiply(new Quaternion(Vec3f.POSITIVE_X, pitch * fixedPitchController, true));

            newMatrixStack.peek().getNormalMatrix().multiply(bone.getWorldSpaceNormal());
            newMatrixStack.peek().getPositionMatrix().multiply(bone.getWorldSpaceXform());
            newMatrixStack.translate(sideMult * 0.547, 0.7655, 0.625);

            if (mirror) arm = arm.getOpposite();

            if (item.isEmpty() && !flag && handDisplay == Ability.HandDisplay.FORCE_RENDER && !player.isInvisible()) {
                newMatrixStack.translate(0, -1 * offHandEquipProgress, 0);
                super.renderArmHoldingItem(newMatrixStack, vertexConsumers, light, 0.0f, 0.0f, arm);
            }
            else {
                super.renderFirstPersonItem(player, tickDelta, pitch, hand, 0.0f, item, 0.0f, newMatrixStack, vertexConsumers, light);
            }
        }
    }

        public GeckoLibFirstPersonRenderer getModelProvider(Class<? extends GeckoLibPlayer> animatable) {
        return modelsToLoad.get(animatable);
    }

    public HashMap<Class<? extends GeckoLibPlayer>, GeckoLibFirstPersonRenderer> getModelsToLoad() {
        return modelsToLoad;
    }

    @Override
    public GeoModelProvider getGeoModelProvider() {
        return this.modelProvider;
    }

    @Override
    public Identifier getTextureLocation(GeckoLibPlayer instance) {
        return ((AbstractClientPlayerEntity)instance.getPlayer()).getSkinTexture();
    }
    public void setSmallArms() {
        this.modelProvider.setUseSmallArms(true);
    }


    public GeckoLibPlayerFirstPersonModel getAnimatedPlayerModel() {
        return this.modelProvider;
    }

    @Override
    public void renderRecursively(GeoBone bone, MatrixStack matrixStack, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        matrixStack.push();
        if (mirror) {
            translateMirror(bone, matrixStack);
            moveToPivotMirror(bone, matrixStack);
            rotateMirror(bone, matrixStack);
            RenderUtils.scale(bone, matrixStack);
        }
        else {
            RenderUtils.translate(bone, matrixStack);
            RenderUtils.moveToPivot(bone, matrixStack);
            RenderUtils.rotate(bone, matrixStack);
            RenderUtils.scale(bone, matrixStack);
        }
        // Record xform matrices for relevant bones
        if (bone instanceof BABBone) {
            BABBone mowzieBone = (BABBone)bone;
            if (mowzieBone.name.equals("LeftArm") || mowzieBone.name.equals("RightArm")) {
                matrixStack.push();
                MatrixStack.Entry entry = matrixStack.peek();
                mowzieBone.setWorldSpaceNormal(entry.getNormalMatrix().copy());
                mowzieBone.setWorldSpaceXform(entry.getPositionMatrix().copy());
                matrixStack.pop();
            }
        }
        if (mirror) {
            moveBackFromPivotMirror(bone, matrixStack);
        }
        else {
            RenderUtils.moveBackFromPivot(bone, matrixStack);
        }
        if (!bone.isHidden) {
            Iterator var10 = bone.childCubes.iterator();

            while(var10.hasNext()) {
                GeoCube cube = (GeoCube)var10.next();
                matrixStack.push();
                this.renderCube(cube, matrixStack, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
                matrixStack.pop();
            }

            var10 = bone.childBones.iterator();

            while(var10.hasNext()) {
                GeoBone childBone = (GeoBone)var10.next();
                this.renderRecursively(childBone, matrixStack, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
            }
        }

        matrixStack.pop();
    }


    // Mirrored render utils
    public static void moveToPivotMirror(GeoCube cube, MatrixStack stack) {
        Vec3f pivot = cube.pivot;
        stack.translate((double)(-pivot.getX() / 16.0F), (double)(pivot.getY() / 16.0F), (double)(pivot.getZ() / 16.0F));
    }

    public static void moveBackFromPivotMirror(GeoCube cube, MatrixStack stack) {
        Vec3f pivot = cube.pivot;
        stack.translate((double)(pivot.getX() / 16.0F), (double)(-pivot.getY() / 16.0F), (double)(-pivot.getZ() / 16.0F));
    }

    public static void moveToPivotMirror(GeoBone bone, MatrixStack stack) {
        stack.translate((double)(-bone.pivotX / 16.0F), (double)(bone.pivotY / 16.0F), (double)(bone.pivotZ / 16.0F));
    }

    public static void moveBackFromPivotMirror(GeoBone bone, MatrixStack stack) {
        stack.translate((double)(bone.pivotX / 16.0F), (double)(-bone.pivotY / 16.0F), (double)(-bone.pivotZ / 16.0F));
    }

    public static void translateMirror(GeoBone bone, MatrixStack stack) {
        stack.translate((double)(bone.getPositionX() / 16.0F), (double)(bone.getPositionY() / 16.0F), (double)(bone.getPositionZ() / 16.0F));
    }

    public static void rotateMirror(GeoBone bone, MatrixStack stack) {
        if (bone.getRotationZ() != 0.0F) {
            stack.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(-bone.getRotationZ()));
        }

        if (bone.getRotationY() != 0.0F) {
            stack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-bone.getRotationY()));
        }

        if (bone.getRotationX() != 0.0F) {
            stack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(bone.getRotationX()));
        }

    }

    public static void rotateMirror(GeoCube bone, MatrixStack stack) {
        Vec3f rotation = bone.rotation;
        stack.multiply(new Quaternion(0.0F, 0.0F, -rotation.getZ(), false));
        stack.multiply(new Quaternion(0.0F, -rotation.getY(), 0.0F, false));
        stack.multiply(new Quaternion(rotation.getX(), 0.0F, 0.0F, false));
    }
}
