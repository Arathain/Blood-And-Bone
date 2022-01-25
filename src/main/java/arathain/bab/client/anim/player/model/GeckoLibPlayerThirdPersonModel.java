package arathain.bab.client.anim.player.model;

import arathain.bab.client.anim.util.BABBone;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Taken from https://github.com/BobMowzie/MowziesMobs/blob/1.16.5/src/main/java/com/bobmowzie/mowziesmobs/client/model/entity/ModelGeckoPlayerThirdPerson.java
 * **/
@Environment(EnvType.CLIENT)
public class GeckoLibPlayerThirdPersonModel extends GeckoLibBipedModel {
    public BABBone bipedLeftArmwear() {
        return getBABBone("LeftArmLayer");
    }

    public BABBone bipedRightArmwear() {
        return getBABBone("RightArmLayer");
    }

    public BABBone bipedLeftLegwear() {
        return getBABBone("LeftLegLayer");
    }

    public BABBone bipedRightLegwear() {
        return getBABBone("RightLegLayer");
    }

    public BABBone bipedBodywear() {
        return getBABBone("BodyLayer");
    }

    public void setVisible(boolean visible) {
        super.setVisible(visible);
        this.bipedLeftArmwear().setHidden(!visible);
        this.bipedRightArmwear().setHidden(!visible);
        this.bipedLeftLegwear().setHidden(!visible);
        this.bipedRightLegwear().setHidden(!visible);
        this.bipedBodywear().setHidden(!visible);
    }

    @Override
    public void setRotationAngles(PlayerEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float partialTick) {
        super.setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, partialTick);
        BABBone rightArmLayerClassic = getBABBone("RightArmLayerClassic");
        BABBone leftArmLayerClassic = getBABBone("LeftArmLayerClassic");
        BABBone rightArmLayerSlim = getBABBone("RightArmLayerSlim");
        BABBone leftArmLayerSlim = getBABBone("LeftArmLayerSlim");
        if (useSmallArms) {
            rightArmLayerClassic.setHidden(true);
            leftArmLayerClassic.setHidden(true);
            rightArmLayerSlim.setHidden(false);
            leftArmLayerSlim.setHidden(false);
        }
        else {
            rightArmLayerSlim.setHidden(true);
            leftArmLayerSlim.setHidden(true);
            rightArmLayerClassic.setHidden(false);
            leftArmLayerClassic.setHidden(false);
        }
    }
}
