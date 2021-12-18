package aria1th.main.pistonboltbuilder.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.Packet;

public class NetworkUtils {
    public static void sendPacket(Packet<?> packet) {
        if (MinecraftClient.getInstance().getNetworkHandler() != null) {
            MinecraftClient.getInstance().getNetworkHandler().sendPacket(packet);
            return;
        }

        throw new IllegalStateException("Cannot send packets when not in game!");
    }
}
