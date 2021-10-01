package aria1th.main.pistonboltbuilder.mixins;

import net.minecraft.block.RailBlock;
import net.minecraft.block.WetSpongeBlock;
import net.minecraft.block.SlimeBlock;
import net.minecraft.block.HoneyBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import aria1th.main.pistonboltbuilder.utils.Actionhandler;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {
    @Shadow
    @Nullable
    public ClientWorld world;
    @Shadow
    @Nullable
    public ClientPlayerEntity player;
    @Shadow
    @Nullable
    public HitResult crosshairTarget;
    @Inject(method = "doItemUse", at = @At(value = "HEAD"))
    private void switchOnOff(CallbackInfo ci){
        if (this.crosshairTarget.getType() == HitResult.Type.MISS) {
            Actionhandler.toggleOnOff();
            System.out.println("on/off");
        }
        if (this.crosshairTarget.getType() == HitResult.Type.BLOCK && world.getBlockState(((BlockHitResult)this.crosshairTarget).getBlockPos()).getBlock() instanceof SlimeBlock && player.getMainHandStack().isEmpty()) {
            Actionhandler.registerStartPos(((BlockHitResult)this.crosshairTarget).getBlockPos().asLong());
            System.out.println("pos1");
        }
        if (this.crosshairTarget.getType() == HitResult.Type.BLOCK && world.getBlockState(((BlockHitResult)this.crosshairTarget).getBlockPos()).getBlock() instanceof HoneyBlock && player.getMainHandStack().isEmpty()) {
            Actionhandler.registerEndPos(((BlockHitResult)this.crosshairTarget).getBlockPos().asLong());
            System.out.println("pos2");
        }
        if (this.crosshairTarget.getType() == HitResult.Type.BLOCK && world.getBlockState(((BlockHitResult)this.crosshairTarget).getBlockPos()).getBlock() instanceof RailBlock && player.getMainHandStack().isEmpty()) {
            Actionhandler.resumeExisting(((BlockHitResult)this.crosshairTarget).getBlockPos().asLong());
            System.out.println("resume");
        }
    }
}