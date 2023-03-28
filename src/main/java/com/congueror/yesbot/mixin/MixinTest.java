package com.congueror.yesbot.mixin;

import com.congueror.yesbot.YESBot;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.simple.SimpleLogger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(SimpleLogger.class)
public class MixinTest {

    @Inject(method = "innerHandleNormalizedLoggingCall",
            locals = LocalCapture.PRINT,
            at = @At(value = "INVOKE", target = "Lorg/slf4j/simple/SimpleLogger;write(Ljava/lang/StringBuilder;Ljava/lang/Throwable;)V"))
    private void innerHandleNormalizedLoggingCall(Level level, List<Marker> markers, String messagePattern, Object[] arguments, Throwable t, CallbackInfo ci, StringBuilder buf, String levelStr, String formattedMessage) {
        YESBot.test2();
    }
}
