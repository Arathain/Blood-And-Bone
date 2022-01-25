package arathain.bab.client.anim.player.render;

import arathain.bab.client.anim.player.GeckoLibPlayerRenderer;
import arathain.bab.client.anim.util.BABBone;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ShoulderParrotFeatureRenderer;
import net.minecraft.client.util.math.MatrixStack;

public class GeckoLibParrotVariantFeatureRenderer extends ShoulderParrotFeatureRenderer<AbstractClientPlayerEntity> implements IGeckoLibRenderLayer {
    private final GeckoLibPlayerRenderer renderPlayerAnimated;

    public GeckoLibParrotVariantFeatureRenderer(GeckoLibPlayerRenderer rendererIn) {
        super(rendererIn);
        //how does this even work
        renderPlayerAnimated = rendererIn;
    }

    @Override
    public void render(MatrixStack matrixStackIn, VertexConsumerProvider bufferIn, int packedLightIn, AbstractClientPlayerEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        BABBone bone = renderPlayerAnimated.getAnimatedPlayerModel().getBABBone("Body");
        MatrixStack newMatrixStack = new MatrixStack();
        newMatrixStack.peek().getNormalMatrix().multiply(bone.getWorldSpaceNormal());
        newMatrixStack.peek().getPositionMatrix().multiply(bone.getWorldSpaceXform());
        this.renderShoulderParrot(newMatrixStack, bufferIn, packedLightIn, entitylivingbaseIn, limbSwing, limbSwingAmount, netHeadYaw, headPitch, true);
        this.renderShoulderParrot(newMatrixStack, bufferIn, packedLightIn, entitylivingbaseIn, limbSwing, limbSwingAmount, netHeadYaw, headPitch, false);
    }
}
