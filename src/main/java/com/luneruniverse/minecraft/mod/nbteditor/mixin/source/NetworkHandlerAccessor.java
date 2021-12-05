package com.luneruniverse.minecraft.mod.nbteditor.mixin.source;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.ClientConnection;

@Mixin(ClientPlayNetworkHandler.class)
public interface NetworkHandlerAccessor {
    @Accessor
    ClientConnection getConnection();
}
