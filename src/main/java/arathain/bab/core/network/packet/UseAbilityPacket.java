package arathain.bab.core.network.packet;

import arathain.bab.BloodAndBone;
import arathain.bab.core.component.AbilityComponent;
import arathain.bab.core.init.BABComponents;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class UseAbilityPacket {
    public static final Identifier ID = new Identifier(BloodAndBone.MODID, "useAbility");

    public UseAbilityPacket() {

    }

    public static void send(int entityID, int index) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeVarInt(entityID);
        buf.writeVarInt(index);
        ClientPlayNetworking.send(ID, buf);
    }
    public static void handle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler network, PacketByteBuf buf, PacketSender sender) {
        int entityID = buf.readVarInt();
        int index = buf.readVarInt();
        server.execute(() -> {
            LivingEntity entity = (LivingEntity) player.getEntityWorld().getEntityById(entityID);
            if (entity != null) {
                AbilityComponent abilityComponent = BABComponents.ABILITY_COMPONENT.get(entity);
                abilityComponent.activateAbility(abilityComponent.getAbilityTypes()[index]);
            }
        });
    }
}
