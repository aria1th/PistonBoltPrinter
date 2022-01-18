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
                    case "togglebidirectional" :
                        Actionhandler.toggleBidirectiona();
                        Actionhandler.chatMessage(Text.of("bidirectional: " + Actionhandler.isBidirectional()));
                        break;
                    case "toggleencoded" :
                        Actionhandler.toggleEncoded();
                        Actionhandler.chatMessage(Text.of("encoded: " + Actionhandler.isEncoded()));
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
               .toggle : pauses and resumes the building process
               .powerableblock : shows the current powerable block (block that carrys on the redstone signal)
               .powerableblock {Block id} : sets the powerable block
               .carpetblock : shows the current block placed on top of the pistonbolt (usually carpet)
               .carpetblock {Block id} : sets the carpet block
               .pushableblock : shows the current pushable block (block thats being placed in front of sticky pistons)
               .pushableblock {Block id} : sets the pushable block
               .toggleimproved : toggles the improved pistonbolt mode
                   Improved Design [READ!]: was discoverd by Inspector Talon and Carbsna aka Fake Story at the same time seperately. It uses half the amount of pistons and can be easily repaired. currently it can't be constructed automatically. After you are finished building turn perpendicular to the direction of the pistonbolt and place powered rails in the middle. The break the other powered rails.
               .toggledirectional : toggles the bidirectional (Nutbolt) mode.
                   Bidirectional/Nutbolt [READ!]: The Nutbolt is a bidirectional Pistonbolt that has only one set of rails. It was invented by Members of the Nuttech SMP (I belive MaxEvilMan and Carbsna aka Fake Story)
               .toggleencoded : toggles wether to use repeaters or comparators
               .resume : Stand on top of a pistonbolt and execute command to resume this pistonbolt
               .help : show this message
               
               """;
    }
}
