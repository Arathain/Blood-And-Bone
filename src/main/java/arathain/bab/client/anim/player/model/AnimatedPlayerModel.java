package arathain.bab.client.anim.player.model;

import com.google.common.collect.Lists;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;

import java.util.List;
import java.util.Random;

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
    public void setAngles(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        this.bipedLeftLegwear.copyModelAngles(this.bipedLeftLeg);
        this.bipedRightLegwear.copyModelAngles(this.bipedRightLeg);
        this.bipedLeftArmwear.copyModelAngles(this.bipedLeftArm);
        this.bipedRightArmwear.copyModelAngles(this.bipedRightArm);
        this.bipedBodyWear.copyModelAngles(this.bipedBody);
        this.bipedHeadwear.copyModelAngles(this.bipedHead);
        this.bipedDeadmau5Head.copyModelAngles(this.bipedHead);
    }


    @Override
    public ModelPart getRandomPart(Random random) {
        return this.modelParts.get(random.nextInt(this.modelParts.size()));
    }


    @Override
    public void setAttributes(BipedEntityModel<T> model) {
        if (!(model.bipedBody instanceof ModelRendererMatrix)) {
            model.bipedHead = new ModelRendererMatrix(model.bipedHead);
            model.bipedHeadwear = new ModelRendererMatrix(model.bipedHeadwear);
            model.bipedBody = new ModelRendererMatrix(model.bipedBody);
            model.bipedLeftArm = new ModelRendererMatrix(model.bipedLeftArm);
            model.bipedRightArm = new ModelRendererMatrix(model.bipedRightArm);
            model.bipedLeftLeg = new ModelRendererMatrix(model.bipedLeftLeg);
            model.bipedRightLeg = new ModelRendererMatrix(model.bipedRightLeg);
        }
        super.setAttributes(model);


    }

    @Override
    public void renderCape(MatrixStack matrices, VertexConsumer vertices, int light, int overlay) {
        super.renderCape(matrices, vertices, light, overlay);
        if(model instanceof AnimatedElytraModel){
            AnimatedElytraModel<?> elytraModel = (AnimatedElytraModel<?>) model;
            elytraModel.bipedBody.copyModelAngles(this.bipedBody);
        }
    }
}
