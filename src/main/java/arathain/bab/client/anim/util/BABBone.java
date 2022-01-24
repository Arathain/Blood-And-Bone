package arathain.bab.client.anim.util;

import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vector4f;
import software.bernie.geckolib3.geo.render.built.GeoBone;


/**
 * https://github.com/BobMowzie/MowziesMobs/blob/master/src/main/java/com/bobmowzie/mowziesmobs/client/model/tools/geckolib/MowzieGeoBone.java
 * **/
public class BABBone extends GeoBone {
    private Matrix4f modelSpaceXform;
    private boolean trackXform;
    public Matrix4f rotMat;

    private Matrix4f worldSpaceXform;
    private Matrix3f worldSpaceNormal;

    public BABBone() {
        super();
        modelSpaceXform = new Matrix4f();
        modelSpaceXform.loadIdentity();
        trackXform = false;
        rotMat = null;

        worldSpaceXform = new Matrix4f();
        worldSpaceXform.loadIdentity();
        worldSpaceNormal = new Matrix3f();
        worldSpaceNormal.loadIdentity();
    }

    public BABBone getParent() {
        return (BABBone) parent;
    }

    public boolean isTrackingXform() {
        return trackXform;
    }

    public void setTrackXform(boolean trackXform) {
        this.trackXform = trackXform;
    }

    public Matrix4f getModelSpaceXform() {
        setTrackXform(true);
        return modelSpaceXform;
    }

    public Vec3d getModelPosition() {
        Matrix4f matrix = getModelSpaceXform();
        Vector4f vec = new Vector4f(0, 0, 0, 1);
        vec.transform(matrix);
        return new Vec3d(-vec.getX() * 16f, vec.getY() * 16f, vec.getZ() * 16f);
    }

    public void setModelPosition(Vec3d pos) {
        // TODO: Doesn't work on bones with parent transforms
        BABBone parent = getParent();
        Matrix4f identity = new Matrix4f();
        identity.loadIdentity();
        Matrix4f matrix = parent == null ? identity : parent.getModelSpaceXform().copy();
        matrix.invert();
        Vector4f vec = new Vector4f(-(float) pos.getX() / 16f, (float) pos.getY() / 16f, (float) pos.getZ() / 16f, 1);
        vec.transform(matrix);
        setPosition(-vec.getX() * 16f, vec.getY() * 16f, vec.getZ() * 16f);
    }

    public Matrix4f getModelRotationMat() {
        Matrix4f matrix = getModelSpaceXform().copy();
        RigUtils.removeMatrixTranslation(matrix);
        return matrix;
    }

    public void setModelRotationMat(Matrix4f mat) {
        rotMat = mat;
    }

    public void setWorldSpaceNormal(Matrix3f worldSpaceNormal) {
        this.worldSpaceNormal = worldSpaceNormal;
    }

    public Matrix3f getWorldSpaceNormal() {
        return worldSpaceNormal;
    }

    public void setWorldSpaceXform(Matrix4f worldSpaceXform) {
        this.worldSpaceXform = worldSpaceXform;
    }

    public Matrix4f getWorldSpaceXform() {
        return worldSpaceXform;
    }

    // Position utils
    public void addPosition(Vec3d vec) {
        addPosition((float) vec.getX(), (float) vec.getY(), (float) vec.getZ());
    }

    public void addPosition(float x, float y, float z) {
        addPositionX(x);
        addPositionY(y);
        addPositionZ(z);
    }

    public void addPositionX(float x) {
        setPositionX(getPositionX() + x);
    }

    public float getPitch() {
        return getRotationX();
    }
    public float getYaw() {
        return getRotationY();
    }

    public float getRoll() {
        return getRotationZ();
    }


    public void addPositionY(float y) {
        setPositionY(getPositionY() + y);
    }

    public void addPositionZ(float z) {
        setPositionZ(getPositionZ() + z);
    }

    public void setPosition(Vec3d vec) {
        setPosition((float) vec.getX(), (float) vec.getY(), (float) vec.getZ());
    }

    public void setPosition(float x, float y, float z) {
        setPositionX(x);
        setPositionY(y);
        setPositionZ(z);
    }

    public Vec3d getPosition() {
        return new Vec3d(getPositionX(), getPositionY(), getPositionZ());
    }

    // Rotation utils
    public void addRotation(Vec3d vec) {
        addRotation((float) vec.getX(), (float) vec.getY(), (float) vec.getZ());
    }

    public void addRotation(float x, float y, float z) {
        addPitch(x);
        addYaw(y);
        addRoll(z);
    }

    public void addPitch(float x) {
        setRotationX(getRotationX() + x);
    }
    public void setPitch(float x) {
        setRotationX(x);
    }
    public void setYaw(float y) {
        setRotationY(y);
    }
    public void setRoll(float z) {
        setRotationZ(z);
    }

    public void addYaw(float y) {
        setRotationY(getYaw() + y);
    }

    public void addRoll(float z) {
        setRotationZ(getRoll() + z);
    }

    public void setRotation(Vec3d vec) {
        setRotation((float) vec.getX(), (float) vec.getY(), (float) vec.getZ());
    }

    public void setRotation(float x, float y, float z) {
        setRotationX(x);
        setYaw(y);
        setRoll(z);
    }

    public Vec3d getRotation() {
        return new Vec3d(getPitch(), getYaw(), getRoll());
    }

    // Scale utils
    public void multiplyScale(Vec3d vec) {
        multiplyScale((float) vec.getX(), (float) vec.getY(), (float) vec.getZ());
    }

    public void multiplyScale(float x, float y, float z) {
        setScaleX(getScaleX() * x);
        setScaleY(getScaleY() * y);
        setScaleZ(getScaleZ() * z);
    }

    public void setScale(Vec3d vec) {
        setScale((float) vec.getX(), (float) vec.getY(), (float) vec.getZ());
    }

    public void setScale(float x, float y, float z) {
        setScaleX(x);
        setScaleY(y);
        setScaleZ(z);
    }

    public Vec3d getScale() {
        return new Vec3d(getScaleX(), getScaleY(), getScaleZ());
    }

    public void addRotationOffsetFromBone(BABBone source) {
        setPitch(getPitch() + source.getPitch() - source.getInitialSnapshot().rotationValueX);
        setYaw(getYaw() + source.getYaw() - source.getInitialSnapshot().rotationValueY);
        setRoll(getRoll() + source.getRoll() - source.getInitialSnapshot().rotationValueZ);
    }
}
