package arathain.bab.client.anim.player.model;

import arathain.bab.BloodAndBone;
import arathain.bab.client.anim.player.GeckoLibPlayer;
import arathain.bab.client.anim.util.BABBone;
import arathain.bab.client.anim.util.BABGeoModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.util.Identifier;

/**
 * Taken from https://github.com/BobMowzie/MowziesMobs/blob/1.16.5/src/main/java/com/bobmowzie/mowziesmobs/client/model/entity/ModelGeckoPlayerThirdPerson.java
 * **/
@Environment(EnvType.CLIENT)
public class GeckoLibPlayerFirstPersonModel extends BABGeoModel<GeckoLibPlayer> {

    private Identifier animationFileLocation;
    private Identifier modelLocation;
    private Identifier textureLocation;

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

    public void setUseSmallArms(boolean useSmallArms) {
        this.useSmallArms = useSmallArms;
    }

    public boolean isUsingSmallArms() {
        return useSmallArms;
    }

    @Override
    public void setLivingAnimations(GeckoLibPlayer entity, Integer uniqueID) {
        super.setLivingAnimations(entity, uniqueID);
        if (isInitialized()) {
            BABBone rightArmLayerClassic = getBABBone("RightArmLayerClassic");
            BABBone leftArmLayerClassic = getBABBone("LeftArmLayerClassic");
            BABBone rightArmLayerSlim = getBABBone("RightArmLayerSlim");
            BABBone leftArmLayerSlim = getBABBone("LeftArmLayerSlim");
            BABBone rightArmClassic = getBABBone("RightArmClassic");
            BABBone leftArmClassic = getBABBone("LeftArmClassic");
            BABBone rightArmSlim = getBABBone("RightArmSlim");
            BABBone leftArmSlim = getBABBone("LeftArmSlim");
            if (useSmallArms) {
                rightArmLayerClassic.setHidden(true);
                rightArmClassic.setHidden(true);
                leftArmLayerClassic.setHidden(true);
                leftArmClassic.setHidden(true);
                rightArmLayerSlim.setHidden(false);
                rightArmSlim.setHidden(false);
                leftArmLayerSlim.setHidden(false);
                leftArmSlim.setHidden(false);
            }
            else {
                rightArmLayerSlim.setHidden(true);
                rightArmSlim.setHidden(true);
                leftArmLayerSlim.setHidden(true);
                leftArmSlim.setHidden(true);
                rightArmLayerClassic.setHidden(false);
                rightArmClassic.setHidden(false);
                leftArmLayerClassic.setHidden(false);
                leftArmClassic.setHidden(false);
            }


            getBABBone("LeftHeldItem").setHidden(true);
            getBABBone("RightHeldItem").setHidden(true);
            getBABBone("RightArmClassic").setHidden(true);
            getBABBone("LeftArmClassic").setHidden(true);
            getBABBone("RightArmLayerClassic").setHidden(true);
            getBABBone("LeftArmLayerClassic").setHidden(true);
        }
    }

    /** Check if the modelId has some Identifier **/
    @Override
    public boolean resourceForModelId(AbstractClientPlayerEntity player) {
        this.animationFileLocation = new Identifier(BloodAndBone.MODID, "animations/animated_player_first_person.animation.json");
        this.modelLocation = new Identifier(BloodAndBone.MODID, "geo/animated_player_first_person.geo.json");
        this.textureLocation = player.getSkinTexture();
        return true;
    }
}
