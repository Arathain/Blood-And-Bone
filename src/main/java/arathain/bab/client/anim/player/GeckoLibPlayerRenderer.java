package arathain.bab.client.anim.player;

import arathain.bab.client.anim.player.model.AnimatedPlayerModel;
import arathain.bab.client.anim.player.model.GeckoLibPlayerThirdPersonModel;
import arathain.bab.client.anim.player.render.GeckoLibParrotVariantFeatureRenderer;
import arathain.bab.client.anim.player.render.IGeckoLibRenderLayer;
import arathain.bab.client.anim.util.BABBone;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.Bipedmodel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.model.Playermodel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
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


/**
 * Taken from https://github.com/BobMowzie/MowziesMobs/blob/master/src/main/java/com/bobmowzie/mowziesmobs/client/render/entity/player/GeckoRenderPlayer.java
 * **/
public class GeckoLibPlayerRenderer extends PlayerEntityRenderer implements IGeoRenderer<GeckoLibPlayer> {
    private static HashMap<Class<? extends GeckoLibPlayer>, GeckoLibPlayerRenderer> modelsToLoad = new HashMap<>();
    private GeckoLibPlayerThirdPersonModel modelProvider;

    private Matrix4f worldRenderMat;

    public Vec3d betweenHandsPos;

    public GeckoLibPlayerRenderer(EntityRendererFactory.Context ctx, GeckoLibPlayerThirdPersonModel modelProvider) {
        super(ctx, false);

        this.model = new ModelPlayerAnimated<>(0.0f, false);

        this.features.clear();
        this.addFeature(new BipedEntityArmorLayer<>(this, new ModelBipedAnimated<>(0.5F), new ModelBipedAnimated<>(1.0F)));
        this.addFeature(new GeckoHeldItemLayer(this));
        this.addFeature(new ArrowLayer<>(this));
        this.addFeature(new Deadmau5HeadLayer(this));
        this.addFeature(new GeckoCapeLayer(this));
        this.addFeature(new HeadLayer<>(this));
        this.addFeature(new GeckoElytraLayer<>(this, this.model.bipedBody));
        this.addFeature(new GeckoLibParrotVariantFeatureRenderer(this, ctx.getModelLoader()));
        this.addFeature(new SpinAttackEffectLayer<>(this));
        this.addFeature(new BeeStingerLayer<>(this));
        this.addFeature(new FrozenRenderHandler.LayerFrozen<>(this));

        this.modelProvider = modelProvider;

        worldRenderMat = new Matrix4f();
        worldRenderMat.setIdentity();
    }

    static {
        AnimationController.addModelFetcher((IAnimatable object) -> {
            if (object instanceof GeckoLibPlayer.GeckoPlayerThirdPerson) {
                GeckoLibPlayerRenderer render = modelsToLoad.get(object.getClass());
                return (IAnimatableModel<Object>) render.getGeoModelProvider();
            } else {
                return null;
            }
        });
    }

    public GeckoLibPlayerRenderer getModelProvider(Class<? extends GeckoLibPlayer> animatable) {
        return modelsToLoad.get(animatable);
    }

    public HashMap<Class<? extends GeckoLibPlayer>, GeckoLibPlayerRenderer> getModelsToLoad() {
        return modelsToLoad;
    }

    public void setSmallArms() {
        this.model = new AnimatedPlayerModel<>(0.0f, true);
        this.modelProvider.setUseSmallArms(true);
    }

    @Override
    public void render(AbstractClientPlayerEntity abstractClientPlayerEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        this.setModelVisibilities(abstractClientPlayerEntity);
        renderLiving(abstractClientPlayerEntity, f, g, matrixStack, vertexConsumerProvider, i, geckoPlayer);
    }

    private void setModelVisibilities(AbstractClientPlayerEntity clientPlayer) {
        GeckoLibPlayerThirdPersonModel playermodel = (GeckoLibPlayerThirdPersonModel) getGeoModelProvider();
        if (playermodel.isInitialized()) {
            if (clientPlayer.isSpectator()) {
                playermodel.setVisible(false);
                playermodel.bipedHead().setHidden(false);
                playermodel.bipedHeadwear().setHidden(false);
            } else {
                playermodel.setVisible(true);
                playermodel.bipedHeadwear().setHidden(!clientPlayer.isWearing(PlayerModelPart.HAT));
                playermodel.bipedBodywear().setHidden(!clientPlayer.isWearing(PlayerModelPart.JACKET));
                playermodel.bipedLeftLegwear().setHidden(!clientPlayer.isWearing(PlayerModelPart.LEFT_PANTS_LEG));
                playermodel.bipedRightLegwear().setHidden(!clientPlayer.isWearing(PlayerModelPart.RIGHT_PANTS_LEG));
                playermodel.bipedLeftArmwear().setHidden(!clientPlayer.isWearing(PlayerModelPart.LEFT_SLEEVE));
                playermodel.bipedRightArmwear().setHidden(!clientPlayer.isWearing(PlayerModelPart.RIGHT_SLEEVE));
                playermodel.isSneak = clientPlayer.isCrouching();
                Bipedmodel.ArmPose bipedmodel$armpose = func_241741_a_(clientPlayer, Hand.MAIN_HAND);
                Bipedmodel.ArmPose bipedmodel$armpose1 = func_241741_a_(clientPlayer, Hand.OFF_HAND);
                if (bipedmodel$armpose.func_241657_a_()) {
                    bipedmodel$armpose1 = clientPlayer.getHeldItemOffhand().isEmpty() ? BipedModel.ArmPose.EMPTY : BipedModel.ArmPose.ITEM;
                }

                if (clientPlayer.getPrimaryHand() == HandSide.RIGHT) {
                    modelProvider.rightArmPose = bipedmodel$armpose;
                    modelProvider.leftArmPose = bipedmodel$armpose1;
                } else {
                    modelProvider.rightArmPose = bipedmodel$armpose1;
                    modelProvider.leftArmPose = bipedmodel$armpose;
                }
            }
        }
    }

    public void renderLiving(AbstractClientPlayerEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, GeckoPlayer geckoPlayer) {
        if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.RenderLivingEvent.Pre<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>(entityIn, this, partialTicks, matrixStackIn, bufferIn, packedLightIn))) return;
        matrixStackIn.push();
        this.model.swingProgress = this.getSwingProgress(entityIn, partialTicks);

        boolean shouldSit = entityIn.isPassenger() && (entityIn.getRidingEntity() != null && entityIn.getRidingEntity().shouldRiderSit());
        this.model.isSitting = shouldSit;
        this.model.isChild = entityIn.isChild();
        float f = MathHelper.lerpAngle(partialTicks, entityIn.prevRenderYawOffset, entityIn.renderYawOffset);
        float f1 = MathHelper.lerpAngle(partialTicks, entityIn.yHeadRot0, entityIn.getYRot()Head);
        float f2 = f1 - f;
        if (shouldSit && entityIn.getVehicle() instanceof LivingEntity livingentity) {
            f = MathHelper.lerpAngle(partialTicks, livingentity.prevRenderYawOffset, livingentity.renderYawOffset);
            f2 = f1 - f;
            float f3 = MathHelper.wrapDegrees(f2);
            if (f3 < -85.0F) {
                f3 = -85.0F;
            }

            if (f3 >= 85.0F) {
                f3 = 85.0F;
            }

            f = f1 - f3;
            if (f3 * f3 > 2500.0F) {
                f += f3 * 0.2F;
            }

            f2 = f1 - f;
        }

        float f6 = MathHelper.lerp(partialTicks, entityIn.xRot0, entityIn.getXRot());
        if (entityIn.getPose() == Pose.SLEEPING) {
            Direction direction = entityIn.getBedDirection();
            if (direction != null) {
                float f4 = entityIn.getEyeHeight(Pose.STANDING) - 0.1F;
                matrixStackIn.translate((double)((float)(-direction.getXOffset()) * f4), 0.0D, (double)((float)(-direction.getZOffset()) * f4));
            }
        }

        float f7 = this.handleRotationFloat(entityIn, partialTicks);
        this.preRenderCallback(entityIn, matrixStackIn, partialTicks);
        float f8 = 0.0F;
        float f5 = 0.0F;
        if (!shouldSit && entityIn.isAlive()) {
            f8 = MathHelper.lerp(partialTicks, entityIn.prevLimbSwingAmount, entityIn.limbSwingAmount);
            f5 = entityIn.limbSwing - entityIn.limbSwingAmount * (1.0F - partialTicks);
            if (entityIn.isChild()) {
                f5 *= 3.0F;
            }

            if (f8 > 1.0F) {
                f8 = 1.0F;
            }
        }

        this.modelProvider.setLivingAnimations(geckoPlayer, entityIn.getUuid().hashCode());
        if (this.modelProvider.isInitialized()) {
            this.applyRotationsPlayerRenderer(entityIn, matrixStackIn, f7, f, partialTicks, f1);
            float bodyRotateAmount = this.modelProvider.getControllerValue("BodyRotateController");
            this.modelProvider.setRotationAngles(entityIn, f5, f8, f7, MathHelper.lerpAngle(bodyRotateAmount, 0, f2), f6, partialTicks);

            BABBone leftHeldItem = modelProvider.getBABBone("LeftHeldItem");
            BABBone rightHeldItem = modelProvider.getBABBone("RightHeldItem");

            Matrix4f worldMatInverted = matrixStackIn.peek().getPositionMatrix().copy();
            worldMatInverted.invert();
            Matrix3f worldNormInverted = matrixStackIn.peek().getNormalMatrix().copy();
            worldNormInverted.invert();
            MatrixStack toWorldSpace = new MatrixStack();
            toWorldSpace.multiply(new Quaternion(0, -entityYaw + 180, 0, true));
            toWorldSpace.translate(0, -1.5f, 0);
            toWorldSpace.peek().getNormalMatrix().multiply(worldNormInverted);
            toWorldSpace.peek().getPositionMatrix().multiply(worldMatInverted);


            Vector4f leftHeldItemPos = new Vector4f(0, 0, 0, 1);
            leftHeldItemPos.transform(leftHeldItem.getWorldSpaceXform());
            leftHeldItemPos.transform(toWorldSpace.getLast().getMatrix());
            Vec3 leftHeldItemPos3 = new Vec3(leftHeldItemPos.getX(), leftHeldItemPos.getY(), leftHeldItemPos.getZ());

            Vector4f rightHeldItemPos = new Vector4f(0, 0, 0, 1);
            rightHeldItemPos.transform(rightHeldItem.getWorldSpaceXform());
            rightHeldItemPos.transform(toWorldSpace.getLast().getMatrix());
            Vec3 rightHeldItemPos3 = new Vec3(rightHeldItemPos.getX(), rightHeldItemPos.getY(), rightHeldItemPos.getZ());

            betweenHandsPos = rightHeldItemPos3.add(leftHeldItemPos3.subtract(rightHeldItemPos3).scale(0.5));
        }
        Minecraft minecraft = Minecraft.getInstance();
        boolean flag = this.isVisible(entityIn);
        boolean flag1 = !flag && !entityIn.isInvisibleToPlayer(minecraft.player);
        boolean flag2 = minecraft.isEntityGlowing(entityIn);
        RenderType rendertype = this.func_230496_a_(entityIn, flag, flag1, flag2);
        if (rendertype != null) {
            IVertexBuilder ivertexbuilder = bufferIn.getBuffer(rendertype);
            int i = getPackedOverlay(entityIn, this.getOverlayProgress(entityIn, partialTicks));
            matrixStackIn.push();
            worldRenderMat.set(matrixStackIn.getLast().getMatrix());
            render(
                    getGeoModelProvider().getModel(getGeoModelProvider().getModelLocation(geckoPlayer)),
                    geckoPlayer, partialTicks, rendertype, matrixStackIn, bufferIn, ivertexbuilder, packedLightIn, i, 1.0F, 1.0F, 1.0F, flag1 ? 0.15F : 1.0F
            );
            matrixStackIn.pop();
            this.model.setRotationAngles(entityIn, f5, f8, f7, f2, f6);
            ModelBipedAnimated.copyFromGeckoModel(this.model, this.modelProvider);
        }

        if (!entityIn.isSpectator()) {
            for(LayerRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> layerrenderer : this.layerRenderers) {
                layerrenderer.render(matrixStackIn, bufferIn, packedLightIn, entityIn, f5, f8, partialTicks, f7, f2, f6);
            }
        }

        matrixStackIn.pop();
        renderEntity(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.RenderLivingEvent.Post<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>(entityIn, this, partialTicks, matrixStackIn, bufferIn, packedLightIn));
    }

    public void renderEntity(AbstractClientPlayer entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        net.minecraftforge.client.event.RenderNameplateEvent renderNameplateEvent = new net.minecraftforge.client.event.RenderNameplateEvent(entityIn, entityIn.getDisplayName(), this, matrixStackIn, bufferIn, packedLightIn, partialTicks);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(renderNameplateEvent);
        if (renderNameplateEvent.getResult() != net.minecraftforge.eventbus.api.Event.Result.DENY && (renderNameplateEvent.getResult() == net.minecraftforge.eventbus.api.Event.Result.ALLOW || this.canRenderName(entityIn))) {
            this.renderName(entityIn, renderNameplateEvent.getContent(), matrixStackIn, bufferIn, packedLightIn);
        }
    }

    protected void applyRotationsPlayerRenderer(AbstractClientPlayer entityLiving, MatrixStack matrixStackIn, float ageInTicks, float rotationYaw, float partialTicks, float headYaw) {
        float f = entityLiving.getSwimAnimation(partialTicks);
        if (entityLiving.isElytraFlying()) {
            this.applyRotationsLivingRenderer(entityLiving, matrixStackIn, ageInTicks, rotationYaw, partialTicks, headYaw);
            float f1 = (float)entityLiving.getTicksElytraFlying() + partialTicks;
            float f2 = MathHelper.clamp(f1 * f1 / 100.0F, 0.0F, 1.0F);
            if (!entityLiving.isSpinAttacking()) {
                matrixStackIn.rotate(Vector3f.XP.rotationDegrees(f2 * (-90.0F - entityLiving.getXRot())));
            }

            Vec3 vector3d = entityLiving.getLook(partialTicks);
            Vec3 vector3d1 = entityLiving.getMotion();
            double d0 = Entity.horizontalMag(vector3d1);
            double d1 = Entity.horizontalMag(vector3d);
            if (d0 > 0.0D && d1 > 0.0D) {
                double d2 = (vector3d1.x * vector3d.x + vector3d1.z * vector3d.z) / Math.sqrt(d0 * d1);
                double d3 = vector3d1.x * vector3d.z - vector3d1.z * vector3d.x;
                matrixStackIn.rotate(Vector3f.YP.rotation((float)(Math.signum(d3) * Math.acos(d2))));
            }
        } else if (f > 0.0F) {
            float swimController = this.modelProvider.getControllerValue("SwimController");
            this.applyRotationsLivingRenderer(entityLiving, matrixStackIn, ageInTicks, rotationYaw, partialTicks, headYaw);
            float f3 = entityLiving.isInWater() ? -90.0F - entityLiving.getXRot() : -90.0F;
            float f4 = MathHelper.lerp(f, 0.0F, f3) * swimController;
            matrixStackIn.rotate(Vector3f.XP.rotationDegrees(f4));
            if (entityLiving.isActualySwimming()) {
                matrixStackIn.translate(0.0D, -1.0D, (double)0.3F);
            }
        } else {
            this.applyRotationsLivingRenderer(entityLiving, matrixStackIn, ageInTicks, rotationYaw, partialTicks, headYaw);
        }
    }

    protected void applyRotationsLivingRenderer(AbstractClientPlayer entityLiving, MatrixStack matrixStackIn, float ageInTicks, float rotationYaw, float partialTicks, float headYaw) {
        if (this.func_230495_a_(entityLiving)) {
            rotationYaw += (float)(Math.cos((double)entityLiving.ticksExisted * 3.25D) * Math.PI * (double)0.4F);
        }

        Pose pose = entityLiving.getPose();
        if (pose != Pose.SLEEPING) {
            float bodyRotateAmount = this.modelProvider.getControllerValue("BodyRotateController");
            matrixStackIn.rotate(Vector3f.YP.rotationDegrees(180.0F - MathHelper.interpolateAngle(bodyRotateAmount, headYaw, rotationYaw)));
        }

        if (entityLiving.deathTime > 0) {
            float f = ((float)entityLiving.deathTime + partialTicks - 1.0F) / 20.0F * 1.6F;
            f = MathHelper.sqrt(f);
            if (f > 1.0F) {
                f = 1.0F;
            }

            matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(f * this.getDeathMaxRotation(entityLiving)));
        } else if (entityLiving.isSpinAttacking()) {
            matrixStackIn.rotate(Vector3f.XP.rotationDegrees(-90.0F - entityLiving.getXRot()));
            matrixStackIn.rotate(Vector3f.YP.rotationDegrees(((float)entityLiving.ticksExisted + partialTicks) * -75.0F));
        } else if (pose == Pose.SLEEPING) {
            Direction direction = entityLiving.getBedDirection();
            float f1 = direction != null ? getFacingAngle(direction) : rotationYaw;
            matrixStackIn.rotate(Vector3f.YP.rotationDegrees(f1));
            matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(this.getDeathMaxRotation(entityLiving)));
            matrixStackIn.rotate(Vector3f.YP.rotationDegrees(270.0F));
        } else if (entityLiving.hasCustomName() || entityLiving instanceof Player) {
            String s = TextFormatting.getTextWithoutFormattingCodes(entityLiving.getName().getString());
            if (("Dinnerbone".equals(s) || "Grumm".equals(s)) && (!(entityLiving instanceof Player) || ((Player)entityLiving).isWearing(PlayerModelPart.CAPE))) {
                matrixStackIn.translate(0.0D, (double)(entityLiving.getHeight() + 0.1F), 0.0D);
                matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(180.0F));
            }
        }
    }

    private static float getFacingAngle(Direction facingIn) {
        switch(facingIn) {
            case SOUTH:
                return 90.0F;
            case WEST:
                return 0.0F;
            case NORTH:
                return 270.0F;
            case EAST:
                return 180.0F;
            default:
                return 0.0F;
        }
    }

    @Override
    public GeoModelProvider<GeckoLibPlayer> getGeoModelProvider() {
        return this.modelProvider;
    }

    public GeckoLibPlayerThirdPersonModel getAnimatedPlayerModel() {
        return this.modelProvider;
    }

    @Override
    public Identifier getTextureLocation(GeckoLibPlayer geckoPlayer) {
        return getEntityTexture((AbstractClientPlayerEntity) geckoPlayer.getPlayer());
    }

    @Override
    public void renderRecursively(GeoBone bone, MatrixStack matrixStack, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        matrixStack.push();
        RenderUtils.translate(bone, matrixStack);
        RenderUtils.moveToPivot(bone, matrixStack);
        RenderUtils.rotate(bone, matrixStack);
        RenderUtils.scale(bone, matrixStack);
        // Record xform matrices for relevant bones
        if (bone instanceof BABBone mowzieBone) {
            if (
                    mowzieBone.name.equals("LeftHeldItem") || mowzieBone.name.equals("RightHeldItem") ||
                            mowzieBone.name.equals("Head") ||
                            mowzieBone.name.equals("Body") ||
                            mowzieBone.name.equals("LeftArm") ||
                            mowzieBone.name.equals("RightArm") ||
                            mowzieBone.name.equals("RightLeg") ||
                            mowzieBone.name.equals("LeftLeg")
            ) {
                matrixStack.push();
                if (!mowzieBone.name.equals("LeftHeldItem") && !mowzieBone.name.equals("RightHeldItem")) {
                    matrixStack.scale(-1.0F, -1.0F, 1.0F);
                }
                if (mowzieBone.name.equals("Body")) {
                    matrixStack.translate(0, -0.75, 0);
                }
                if (mowzieBone.name.equals("LeftArm")) {
                    matrixStack.translate(-0.075, 0, 0);
                }
                if (mowzieBone.name.equals("RightArm")) {
                    matrixStack.translate(0.075, 0, 0);
                }
                MatrixStack.Entry entry = matrixStack.peek();
                mowzieBone.setWorldSpaceNormal(entry.getNormalMatrix().copy());
                mowzieBone.setWorldSpaceXform(entry.getPositionMatrix().copy());
                matrixStack.pop();
            }
        }
        RenderUtils.moveBackFromPivot(bone, matrixStack);
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

        for(FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> layerrenderer : this.features) {
            if (layerrenderer instanceof IGeckoLibRenderLayer) ((IGeckoLibRenderLayer)layerrenderer).renderRecursively(bone, matrixStack, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        }
    }
}
