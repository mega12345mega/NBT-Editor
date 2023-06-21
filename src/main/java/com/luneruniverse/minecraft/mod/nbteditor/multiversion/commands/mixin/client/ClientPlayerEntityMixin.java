/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Desc;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.commands.ClientCommandInternals;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;

@Mixin(ClientPlayerEntity.class)
abstract class ClientPlayerEntityMixin {
	// 1.19.1
	@Inject(target = @Desc(value = "method_44099", args = String.class, ret = boolean.class), at = @At("HEAD"), cancellable = true, remap = false, require = 0) // boolean sendCommand(String)
//	@Group(name = "sendChatMessage", min = 1)
	@SuppressWarnings("target")
	private void onSendCommand(String command, CallbackInfoReturnable<Boolean> cir) {
		if (ClientCommandInternals.executeCommand(command)) {
			cir.setReturnValue(true);
		}
	}
	// 1.19.0
	@Inject(target = @Desc(value = "method_44099", args = String.class, ret = void.class), at = @At("HEAD"), cancellable = true, remap = false, require = 0) // void sendCommand(String)
//	@Group(name = "sendChatMessage", min = 1)
	@SuppressWarnings("target")
	private void onSendCommand(String command, CallbackInfo info) {
		if (ClientCommandInternals.executeCommand(command)) {
			info.cancel();
		}
	}
	
	// 1.19.2 - 1.19.0
	@Inject(method = "method_44098(Ljava/lang/String;Lnet/minecraft/text/Text;)V", at = @At("HEAD"), cancellable = true, require = 0) // void sendCommand(String, Text)
//	@Group(name = "sendChatMessage", min = 1)
	@SuppressWarnings("target")
	private void onSendCommand(String command, Text preview, CallbackInfo info) {
		if (ClientCommandInternals.executeCommand(command)) {
			info.cancel();
		}
	}
	
	// 1.18
	@Inject(at = @At("HEAD"), cancellable = true, target = @Desc(value = "method_3142", args = String.class), remap = false, require = 0) // void sendChatMessage(String)
//	@Group(name = "sendChatMessage", min = 1)
	@SuppressWarnings("target")
	private void onSendChatMessage(String message, CallbackInfo info) {
		if (!message.isEmpty() && message.charAt(0) == '/' && ClientCommandInternals.executeCommand(message.substring(1))) {
			info.cancel();
		}
	}
}
