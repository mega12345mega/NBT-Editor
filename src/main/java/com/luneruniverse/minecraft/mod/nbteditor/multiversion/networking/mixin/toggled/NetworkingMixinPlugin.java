package com.luneruniverse.minecraft.mod.nbteditor.multiversion.networking.mixin.toggled;

import java.util.List;

import com.luneruniverse.minecraft.mod.nbteditor.misc.BasicMixinPlugin;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;

public class NetworkingMixinPlugin extends BasicMixinPlugin {
	
	@Override
	public void addMixins(List<String> output) {
		Version.newSwitch()
				.range("1.20.2", null, () -> output.add("toggled.ServerPlayNetworkHandlerMixin_1_20_2"))
				.range(null, "1.20.1", () -> output.add("toggled.ServerPlayNetworkHandlerMixin_1_20_1"))
				.run();
		
		Version.newSwitch()
				.range("1.20.5", null, () -> output.add("toggled.CustomPayload1Mixin"))
				.range("1.20.2", "1.20.4", () -> {
					output.add("toggled.CustomPayloadC2SPacketMixin");
					output.add("toggled.CustomPayloadS2CPacketMixin");
				})
				.range(null, "1.20.1", () -> {})
				.run();
	}
	
}
