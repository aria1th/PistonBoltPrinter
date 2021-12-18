package aria1th.main.pistonboltbuilder.mixins;

import aria1th.main.pistonboltbuilder.utils.Actionhandler;
import aria1th.main.pistonboltbuilder.utils.NetworkUtils;
import net.minecraft.block.RailBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;

@Mixin(ClientPlayerEntity.class)
public abstract class ChatManager {

    @Inject(at = @At("HEAD"), method = "sendChatMessage(Ljava/lang/String;)V", cancellable = true)
    private void handleChatCommand(String text, CallbackInfo ci) {
        if (text.startsWith(".")) {
            if (text.length() == 1) {
                //print help message
                Actionhandler.chatMessage(Text.of(getHelpMessage()));
                ci.cancel();
                return;
            }
            if (text.startsWith(". .")) {
                NetworkUtils.sendPacket(new ChatMessageC2SPacket(text.substring(2)));
            }
            try {
                text = text.substring(1);
                String target = text.split("[ :]")[0].toLowerCase();
                switch (target.toLowerCase()){
                    case "powerableblock" :
                        try{
                            String value = text.split("[ :]")[1];
                            Actionhandler.setPowerableBlockItem(value);
                            Actionhandler.chatMessage(Text.of("set powerableblock to: " + Registry.ITEM.getId(Actionhandler.getPowerableBlockItem())));
                        } catch (Exception e){
                            Actionhandler.chatMessage(Text.of("powerableblock: " + Actionhandler.getPowerableBlockItem()));
                        }
                        break;
                    case "carpetblock" :
                        try{
                            String value = text.split("[ :]")[1];
                            Actionhandler.setCarpetBlockItem(value);
                            Actionhandler.chatMessage(Text.of("set carpetblock to: " + Actionhandler.getCarpetBlockItem()));
                        } catch (Exception e){
                            Actionhandler.chatMessage(Text.of("carpetblock: " + Actionhandler.getCarpetBlockItem()));
                        }
                        break;
                    case "pushableblock" :
                        try{
                            String value = text.split("[ :]")[1];
                            Actionhandler.setPushableBlockItem(value);
                            Actionhandler.chatMessage(Text.of("set pushableblock to: " + Actionhandler.getPushableBlockItem()));
                        } catch (Exception e){
                            Actionhandler.chatMessage(Text.of("pushableblock: " + Actionhandler.getPushableBlockItem()));
                        }
                        break;
                    case "toggleimproved" :
                        Actionhandler.toggleImproved();
                        Actionhandler.chatMessage(Text.of("improved: " + Actionhandler.isImproved()));
                        break;
                    case "pos1" :
                        Actionhandler.registerStartPos(MinecraftClient.getInstance().player.getBlockPos().asLong());
                        Actionhandler.chatMessage(Text.of("pos1: " + MinecraftClient.getInstance().player.getBlockPos()));
                        break;
                    case "pos2" :
                        Actionhandler.registerEndPos(MinecraftClient.getInstance().player.getBlockPos().asLong());
                        Actionhandler.chatMessage(Text.of("pos2: " + MinecraftClient.getInstance().player.getBlockPos()));
                        break;
                    case "toggle" :
                        Actionhandler.toggleOnOff();
                        break;
                    case "resume" :
                        if(MinecraftClient.getInstance().world.getBlockState(MinecraftClient.getInstance().player.getBlockPos()).getBlock() instanceof RailBlock){
                            Actionhandler.resumeExisting(MinecraftClient.getInstance().player.getBlockPos().asLong());
                            System.out.println("resume");
                        }
                        break;
                    case "help" :
                        Actionhandler.chatMessage(Text.of(getHelpMessage()));
                        break;
                    default: Actionhandler.chatMessage(Text.of("Command not found!"));
                }
                ci.cancel();
            } catch (Exception e) {
                Actionhandler.chatMessage(Text.of("nothing"));
                ci.cancel();
            }
        }
    }

    private static String getHelpMessage() {
        return """
                
                Pistonboltbuilder by aria1th expanded by fladenbrot133
                Command prefix: '. '
                to write a message that is supposed to begin write '. .'
                and then the rest of your message

                type '.commands' to see the commands
                """;
    }
    private static String getCommandsHelpMessage() {
        return """
               
               Commands:
               .pos1 : sets the first position
               .pos2 : sets the second postition
               .powerableblock : shows the current powerable block
               .powerableblock {Block id} : sets the powerable block
               """;
    }
}