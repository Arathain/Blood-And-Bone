package arathain.bab.client.anim.player.model;

import com.ibm.icu.text.Normalizer;
import net.minecraft.client.model.ModelPart;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;

import java.util.List;
import java.util.Map;

public class ModelRendererMatrix extends ModelPart {
    private ModelPart modelPart = new ModelPart(cuboids, children);
    private Matrix4f worldXform;
    private Matrix3f worldNormal;


    public ModelRendererMatrix(List<Cuboid> cuboids, Map<String, ModelPart> children) {
        super(cuboids, children);
        copyTransform();
    }

    @Override
    public void setAngles(float pitch, float yaw, float roll) {
        super.setAngles(pitch, yaw, roll);
    }

    public Matrix3f getWorldNormal() {
        return worldNormal;
    }

    public void setWorldNormal(Matrix3f worldNormal) {
        this.worldNormal = worldNormal;
    }

    public Matrix4f getWorldXform() {
        return worldXform;
    }

    public void setWorldXform(Matrix4f worldXform) {
        this.worldXform = worldXform;
    }
}
