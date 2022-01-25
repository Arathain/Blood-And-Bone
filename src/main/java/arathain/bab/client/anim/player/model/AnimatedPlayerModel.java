package arathain.bab.client.anim.player.model;

import com.google.common.collect.Lists;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.LivingEntity;

import java.util.List;

public class AnimatedPlayerModel<T extends LivingEntity> extends PlayerEntityModel<T> {
    private List<ModelPart> modelParts = Lists.newArrayList();

    public AnimatedPlayerModel(ModelPart root, boolean thinArms) {
        super(root, thinArms);
        this.body = new ModelRendererMatrix(body);
        this.head = new ModelRendererMatrix(head);
        this.rightArm = new ModelRendererMatrix(rightArm);
        this.leftArm = new ModelRendererMatrix(leftArm);
        this.rightLeg = new ModelRendererMatrix(rightLeg);
        this.leftLeg = new ModelRendererMatrix(leftLeg);

        this.hat = new ModelRendererMatrix(hat);
        this.jacket = new ModelRendererMatrix(jacket);
        this.leftSleeve = new ModelRendererMatrix(leftSleeve);
        this.rightSleeve = new ModelRendererMatrix(rightSleeve);
        this.leftPants = new ModelRendererMatrix(leftPants);
        this.rightPants = new ModelRendererMatrix(rightPants);
        this.ear = new ModelRendererMatrix(ear);

        modelParts.add(ear);
        modelParts.add(cloak);
        if (thinArms) {
            modelParts.add(leftArm);
            modelParts.add(rightArm);
            modelParts.add(leftSleeve);
            modelParts.add(rightSleeve);
        }
        else {
            modelParts.add(leftArm);
            modelParts.add(leftSleeve);
            modelParts.add(rightSleeve);
        }
        modelParts.add(leftLeg);
        modelParts.add(leftPants);
        modelParts.add(rightPants);
        modelParts.add(jacket);
    }

    @Override
    public void setRotationAngles(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.bipedLeftLegwear.copyModelAngles(this.bipedLeftLeg);
        this.bipedRightLegwear.copyModelAngles(this.bipedRightLeg);
        this.bipedLeftArmwear.copyModelAngles(this.bipedLeftArm);
        this.bipedRightArmwear.copyModelAngles(this.bipedRightArm);
        this.bipedBodyWear.copyModelAngles(this.bipedBody);
        this.bipedHeadwear.copyModelAngles(this.bipedHead);
        this.bipedDeadmau5Head.copyModelAngles(this.bipedHead);
    }

    @Override
    public ModelRenderer getRandomModelRenderer(Random randomIn) {
        return this.modelParts.get(randomIn.nextInt(this.modelParts.size()));
    }

    @Override
    public void copyModelAttributesTo(EntityModel<T> p_217111_1_) {
        super.copyModelAttributesTo(p_217111_1_);
        if (p_217111_1_ instanceof MowzieElytraModel) {
            MowzieElytraModel<?> elytraModel = (MowzieElytraModel<?>) p_217111_1_;
            elytraModel.bipedBody.copyModelAngles(this.bipedBody);
        }
    }

    @Override
    public void setModelAttributes(BipedModel<T> modelIn) {
        if (!(modelIn.bipedBody instanceof ModelRendererMatrix)) {
            modelIn.bipedHead = new ModelRendererMatrix(modelIn.bipedHead);
            modelIn.bipedHeadwear = new ModelRendererMatrix(modelIn.bipedHeadwear);
            modelIn.bipedBody = new ModelRendererMatrix(modelIn.bipedBody);
            modelIn.bipedLeftArm = new ModelRendererMatrix(modelIn.bipedLeftArm);
            modelIn.bipedRightArm = new ModelRendererMatrix(modelIn.bipedRightArm);
            modelIn.bipedLeftLeg = new ModelRendererMatrix(modelIn.bipedLeftLeg);
            modelIn.bipedRightLeg = new ModelRendererMatrix(modelIn.bipedRightLeg);
        }
        super.setModelAttributes(modelIn);
    }
}
