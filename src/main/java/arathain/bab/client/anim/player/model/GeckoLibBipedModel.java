package arathain.bab.client.anim.player.model;

import arathain.bab.BloodAndBone;
import arathain.bab.client.anim.player.GeckoLibPlayer;
import arathain.bab.client.anim.util.BABBone;
import arathain.bab.client.anim.util.BABGeoModel;
import arathain.bab.core.ability.AbilityHandler;
import arathain.bab.core.component.AbilityComponent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import software.bernie.geckolib3.geo.render.built.GeoBone;

/**
 * Taken from https://github.com/BobMowzie/MowziesMobs/blob/1.16.5/src/main/java/com/bobmowzie/mowziesmobs/client/model/entity/ModelGeckoBiped.java
 * **/
@Environment(EnvType.CLIENT)
public class GeckoLibBipedModel extends BABGeoModel<GeckoLibPlayer> {
    private Identifier animationFileLocation;
    private Identifier modelLocation;
    private Identifier textureLocation;

    public boolean isSitting = false;
    public boolean isChild = true;
    public float swingProgress;
    public boolean isSneak;
    public float swimAnimation;

    public BipedEntityModel.ArmPose leftArmPose = BipedEntityModel.ArmPose.EMPTY;
    public BipedEntityModel.ArmPose rightArmPose = BipedEntityModel.ArmPose.EMPTY;

    protected boolean useSmallArms;

    @Override
    public Identifier getAnimationFileLocation(GeckoLibPlayer animatable) {
        return animationFileLocation;
    }

    @Override
    public Identifier getModelLocation(GeckoLibPlayer animatable) {
        return modelLocation;
    }

    @Override
    public Identifier getTextureLocation(GeckoLibPlayer animatable) {
        return textureLocation;
    }

    /** Check if the modelId has some Identifier **/
    public boolean resourceForModelId(AbstractClientPlayerEntity player) {
        this.animationFileLocation = new Identifier(BloodAndBone.MODID, "animations/animated_player.animation.json");
        this.modelLocation = new Identifier(BloodAndBone.MODID, "geo/animated_player.geo.json");
        this.textureLocation = player.getSkinTexture();
        return true;
    }

    public void setUseSmallArms(boolean useSmallArms) {
        this.useSmallArms = useSmallArms;
    }

    public boolean isUsingSmallArms() {
        return useSmallArms;
    }

    public BABBone bipedHead() {
        return getBABBone("Head");
    }

    public BABBone bipedHeadwear() {
        return getBABBone("HatLayer");
    }

    public BABBone bipedBody() {
        return getBABBone("Body");
    }

    public BABBone bipedRightArm() {
        return getBABBone("RightArm");
    }

    public BABBone bipedLeftArm() {
        return getBABBone("LeftArm");
    }

    public BABBone bipedRightLeg() {
        return getBABBone("RightLeg");
    }

    public BABBone bipedLeftLeg() {
        return getBABBone("LeftLeg");
    }

    public void setVisible(boolean visible) {
        this.bipedHead().setHidden(!visible);
        this.bipedHeadwear().setHidden(!visible);
        this.bipedBody().setHidden(!visible);
        this.bipedRightArm().setHidden(!visible);
        this.bipedLeftArm().setHidden(!visible);
        this.bipedRightLeg().setHidden(!visible);
        this.bipedLeftLeg().setHidden(!visible);
    }

    public void setRotationAngles() {
        BABBone head = getBABBone("Head");
        BABBone neck = getBABBone("Neck");
        float yaw = 0;
        float pitch = 0;
        float roll = 0;
        GeoBone parent = neck.parent;
        while (parent != null) {
            pitch += parent.getRotationX();
            yaw += parent.getRotationY();
            roll += parent.getRotationZ();
            parent = parent.parent;
        }
        neck.addRotation(-yaw, -pitch, -roll);
    }


    public void setRotationAngles(PlayerEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float partialTick) {
        if (!isInitialized()) return;
        if (MinecraftClient.getInstance().isPaused()) return;

        BABBone rightArmClassic = getBABBone("RightArmClassic");
        BABBone leftArmClassic = getBABBone("LeftArmClassic");
        BABBone rightArmSlim = getBABBone("RightArmSlim");
        BABBone leftArmSlim = getBABBone("LeftArmSlim");
        if (useSmallArms) {
            rightArmClassic.setHidden(true);
            leftArmClassic.setHidden(true);
            rightArmSlim.setHidden(false);
            leftArmSlim.setHidden(false);
        }
        else {
            rightArmSlim.setHidden(true);
            leftArmSlim.setHidden(true);
            rightArmClassic.setHidden(false);
            leftArmClassic.setHidden(false);
        }

        this.swimAnimation = entityIn.getLeaningPitch(partialTick);

        float headLookAmount = getControllerValue("HeadLookController");
        float armLookAmount = 1f - getControllerValue("ArmPitchController");
        float armLookAmountRight = getBone("ArmPitchController").getPositionY();
        float armLookAmountLeft = getBone("ArmPitchController").getPositionZ();
        boolean flag = entityIn.getRoll() > 4;
        boolean flag1 = entityIn.isInSwimmingPose();
        this.bipedHead().addYaw(headLookAmount * -netHeadYaw * ((float)Math.PI / 180F));
        this.getBABBone("LeftClavicle").addYaw(Math.min(armLookAmount + armLookAmountLeft, 1) * -netHeadYaw * ((float)Math.PI / 180F));
        this.getBABBone("RightClavicle").addYaw(Math.min(armLookAmount + armLookAmountRight, 1) * -netHeadYaw * ((float)Math.PI / 180F));
        if (flag) {
            this.bipedHead().addPitch((-(float)Math.PI / 4F));
        } else if (this.swimAnimation > 0.0F) {
            if (flag1) {
                this.bipedHead().addPitch(headLookAmount * this.rotLerpRad(this.swimAnimation, this.bipedHead().getRotationX(), (-(float)Math.PI / 4F)));
            } else {
                this.bipedHead().addPitch(headLookAmount * this.rotLerpRad(this.swimAnimation, this.bipedHead().getRotationX(), headPitch * ((float)Math.PI / 180F)));
            }
        } else {
            this.bipedHead().addPitch(headLookAmount * -headPitch * ((float)Math.PI / 180F));
            this.getBABBone("LeftClavicle").addPitch(Math.min(armLookAmount + armLookAmountLeft, 1) * -headPitch * ((float)Math.PI / 180F));
            this.getBABBone("RightClavicle").addPitch(Math.min(armLookAmount + armLookAmountRight, 1) * -headPitch * ((float)Math.PI / 180F));
        }

        float f = 1.0F;
        if (flag) {
            f = (float)entityIn.getVelocity().lengthSquared();
            f = f / 0.2F;
            f = f * f * f;
        }

        if (f < 1.0F) {
            f = 1.0F;
        }

        float legWalkAmount = getControllerValue("LegWalkController");
        float armSwingAmount = getControllerValue("ArmSwingController");
        float armSwingAmountRight = 1.0f - getBone("ArmSwingController").getPositionY();
        float armSwingAmountLeft = 1.0f - getBone("ArmSwingController").getPositionZ();
        this.bipedRightArm().addPitch(armSwingAmount * armSwingAmountRight *MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 2.0F * limbSwingAmount * 0.5F / f);
        this.bipedLeftArm().addPitch(armSwingAmount * armSwingAmountLeft * MathHelper.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F / f);
        this.bipedRightLeg().addPitch(legWalkAmount * MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount / f);
        this.bipedLeftLeg().addPitch(legWalkAmount * MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount / f);

        if (this.isSitting) {
            this.bipedRightArm().setRotationX(bipedRightArm().getRotationX() + (-(float)Math.PI / 5F));
            this.bipedLeftArm().setRotationX(bipedRightArm().getRotationX() + (-(float)Math.PI / 5F));
            this.bipedRightLeg().setRotationX(-1.4137167F);
            this.bipedRightLeg().setRotationY(((float)Math.PI / 10F));
            this.bipedRightLeg().setRotationZ(0.07853982F);
            this.bipedLeftLeg().setRotationX(-1.4137167F);
            this.bipedLeftLeg().setRotationY((-(float)Math.PI / 10F));
            this.bipedLeftLeg().setRotationZ(-0.07853982F);
            getBABBone("Waist").setRotation(0, 0, 0);
            getBABBone("Root").setRotation(0, 0, 0);
        }

        boolean flag2 = entityIn.getMainArm() == Arm.RIGHT;
        boolean flag3 = flag2 ? this.leftArmPose.isTwoHanded() : this.rightArmPose.isTwoHanded();
        if (flag2 != flag3) {
            this.func_241655_c_(entityIn);
            this.func_241654_b_(entityIn);
        } else {
            this.func_241654_b_(entityIn);
            this.func_241655_c_(entityIn);
        }

//		this.swingAnim(entityIn, ageInTicks);

        float sneakController = getControllerValue("CrouchController");
        if (this.isSneak) {
            this.bipedBody().addPitch(-0.5F * sneakController);
            this.getBABBone("Neck").addPitch(0.5F * sneakController);
            this.bipedRightArm().addRotation(0.4F * sneakController, 0, 0);
            this.bipedLeftArm().addRotation(0.4F * sneakController, 0, 0);
            this.bipedHead().addPositionY(-1F * sneakController);
            this.bipedBody().addPosition(0, -1.5F * sneakController, 1.7f * sneakController);
            this.getBABBone("Waist").addPosition(0, -0.2f * sneakController, 4F * sneakController);
            this.bipedLeftArm().addPitch(-0.4f * sneakController);
            this.bipedLeftArm().addPosition(0, 0.2f * sneakController, -1f * sneakController);
            this.bipedRightArm().addPitch(-0.4f * sneakController);
            this.bipedRightArm().addPosition(0, 0.2f * sneakController, -1f * sneakController);

            this.getBABBone("Waist").addPositionY(2f * (1f - sneakController));
        }

        float armBreathAmount = getControllerValue("ArmBreathController");
        breathAnim(this.bipedRightArm(), this.bipedLeftArm(), ageInTicks, armBreathAmount);

//		if (this.swimAnimation > 0.0F) {
//			float f1 = limbSwing % 26.0F;
//			HandSide handside = this.getMainHand(entityIn);
//			float f2 = handside == Arm.RIGHT && this.swingProgress > 0.0F ? 0.0F : this.swimAnimation;
//			float f3 = handside == Arm.LEFT && this.swingProgress > 0.0F ? 0.0F : this.swimAnimation;
//			if (f1 < 14.0F) {
//				this.bipedLeftArm().setRotationX(this.rotLerpRad(f3, this.bipedLeftArm().getRotationX(), 0.0F));
//				this.bipedRightArm().setRotationX(MathHelper.lerp(f2, this.bipedRightArm().getRotationX(), 0.0F));
//				this.bipedLeftArm().setRotationY(this.rotLerpRad(f3, this.bipedLeftArm().getRotationY(), (float)Math.PI));
//				this.bipedRightArm().setRotationY(MathHelper.lerp(f2, this.bipedRightArm().getRotationY(), (float)Math.PI));
//				this.bipedLeftArm().setRotationZ(this.rotLerpRad(f3, this.bipedLeftArm().getRotationZ(), (float)Math.PI + 1.8707964F * this.getArmAngleSq(f1) / this.getArmAngleSq(14.0F)));
//				this.bipedRightArm().setRotationZ(MathHelper.lerp(f2, this.bipedRightArm().getRotationZ(), (float)Math.PI - 1.8707964F * this.getArmAngleSq(f1) / this.getArmAngleSq(14.0F)));
//			} else if (f1 >= 14.0F && f1 < 22.0F) {
//				float f6 = (f1 - 14.0F) / 8.0F;
//				this.bipedLeftArm().setRotationX(this.rotLerpRad(f3, this.bipedLeftArm().getRotationX(), ((float)Math.PI / 2F) * f6));
//				this.bipedRightArm().setRotationX(MathHelper.lerp(f2, this.bipedRightArm().getRotationX(), ((float)Math.PI / 2F) * f6));
//				this.bipedLeftArm().setRotationY(this.rotLerpRad(f3, this.bipedLeftArm().getRotationY(), (float)Math.PI));
//				this.bipedRightArm().setRotationY(MathHelper.lerp(f2, this.bipedRightArm().getRotationY(), (float)Math.PI));
//				this.bipedLeftArm().setRotationZ(this.rotLerpRad(f3, this.bipedLeftArm().getRotationZ(), 5.012389F - 1.8707964F * f6));
//				this.bipedRightArm().setRotationZ(MathHelper.lerp(f2, this.bipedRightArm().getRotationZ(), 1.2707963F + 1.8707964F * f6));
//			} else if (f1 >= 22.0F && f1 < 26.0F) {
//				float f4 = (f1 - 22.0F) / 4.0F;
//				this.bipedLeftArm().setRotationX(this.rotLerpRad(f3, this.bipedLeftArm().getRotationX(), ((float)Math.PI / 2F) - ((float)Math.PI / 2F) * f4));
//				this.bipedRightArm().setRotationX(MathHelper.lerp(f2, this.bipedRightArm().getRotationX(), ((float)Math.PI / 2F) - ((float)Math.PI / 2F) * f4));
//				this.bipedLeftArm().setRotationY(this.rotLerpRad(f3, this.bipedLeftArm().getRotationY(), (float)Math.PI));
//				this.bipedRightArm().setRotationY(MathHelper.lerp(f2, this.bipedRightArm().getRotationY(), (float)Math.PI));
//				this.bipedLeftArm().setRotationZ(this.rotLerpRad(f3, this.bipedLeftArm().getRotationZ(), (float)Math.PI));
//				this.bipedRightArm().setRotationZ(MathHelper.lerp(f2, this.bipedRightArm().getRotationZ(), (float)Math.PI));
//			}
//
//			float f7 = 0.3F;
//			float f5 = 0.33333334F;
//			this.bipedLeftLeg().setRotationX(MathHelper.lerp(this.swimAnimation, this.bipedLeftLeg().getRotationX(), 0.3F * MathHelper.cos(limbSwing * 0.33333334F + (float)Math.PI)));
//			this.bipedRightLeg().setRotationX(MathHelper.lerp(this.swimAnimation, this.bipedRightLeg().getRotationX(), 0.3F * MathHelper.cos(limbSwing * 0.33333334F)));
//		}


        //TODO CCA this shit asap
        AbilityComponent abilityCapability = AbilityHandler.INSTANCE.getAbilityComponent(entityIn);
        if (abilityCapability != null && abilityCapability.getActiveAbility() != null) {
            abilityCapability.codeAnimations(this, partialTick);
        }
    }

    protected BABBone getArmForSide(Arm arm) {
        return arm == Arm.LEFT ? this.bipedLeftArm() : this.bipedRightArm();
    }

    protected float rotLerpRad(float angleIn, float maxAngleIn, float mulIn) {
        float f = (mulIn - maxAngleIn) % ((float)Math.PI * 2F);
        if (f < -(float)Math.PI) {
            f += ((float)Math.PI * 2F);
        }

        if (f >= (float)Math.PI) {
            f -= ((float)Math.PI * 2F);
        }

        return maxAngleIn + angleIn * f;
    }

    private float getArmAngleSq(float limbSwing) {
        return -65.0F * limbSwing + limbSwing * limbSwing;
    }

    protected Arm getMainHand(PlayerEntity entity) {
        Arm arm = entity.getMainArm();
        return entity.preferredHand == Hand.MAIN_HAND ? arm : arm.getOpposite();
    }

    public static void breathAnim(BABBone rightArm, BABBone leftArm, float ageInTicks, float armBreathAmount) {
        rightArm.addRoll(armBreathAmount * MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F);
        leftArm.addRoll(armBreathAmount * -MathHelper.cos(ageInTicks * 0.09F) * 0.05F - 0.05F);
        rightArm.addPitch(armBreathAmount * MathHelper.sin(ageInTicks * 0.067F) * 0.05F);
        leftArm.addPitch(armBreathAmount * -MathHelper.sin(ageInTicks * 0.067F) * 0.05F);
    }

    private void func_241654_b_(PlayerEntity player) {
        float armSwingAmount = getControllerValue("ArmSwingController");
        switch(this.rightArmPose) {
            case EMPTY:
                break;
            case BLOCK:
                this.bipedRightArm().addPitch(0.9424779F * armSwingAmount);
                break;
            case ITEM:
                this.bipedRightArm().addPitch( ((float)Math.PI / 10F) * armSwingAmount);
                break;
        }

    }

    private void func_241655_c_(PlayerEntity p_241655_1_) {
        float armSwingAmount = getControllerValue("ArmSwingController");
        switch(this.leftArmPose) {
            case EMPTY:
                break;
            case BLOCK:
                this.bipedLeftArm().addPitch(0.9424779F * armSwingAmount);
                break;
            case ITEM:
                this.bipedLeftArm().addPitch(((float)Math.PI / 10F) * armSwingAmount);
                break;
        }
    }
}
