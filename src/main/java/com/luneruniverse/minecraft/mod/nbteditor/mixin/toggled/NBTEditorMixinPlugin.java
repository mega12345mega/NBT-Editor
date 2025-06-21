package com.luneruniverse.minecraft.mod.nbteditor.mixin.toggled;

import java.util.List;

import com.luneruniverse.minecraft.mod.nbteditor.misc.BasicMixinPlugin;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

public class NBTEditorMixinPlugin extends BasicMixinPlugin {
	
	@Override
	public void addMixins(List<String> output) {
		Version.newSwitch()
				.range("1.19.3", null, () -> output.add("toggled.ServerPlayNetworkHandlerMixin"))
				.range(null, "1.19.2", () -> {})
				.run();
		Version.newSwitch()
				.range("1.21.0", null, () -> output.add("toggled.ArmorSlotMixin"))
				.range(null, "1.20.6", () -> output.add("toggled.PlayerScreenHandler1Mixin"))
				.run();
		// https://github.com/SpongePowered/Mixin/issues/677
		Version.newSwitch()
				.range("1.21.0", null, () -> {})
				.range(null, "1.20.6", () -> output.add("toggled.HorseScreenHandlerMixin"))
				.run();
		// https://github.com/SpongePowered/Mixin/issues/677
		Version.newSwitch()
				.range("1.21.0", null, () -> {})
				.range(null, "1.20.6", () -> output.add("toggled.HorseScreenHandler2Mixin"))
				.run();
		Version.newSwitch()
				.range("1.21.0", null, () -> output.add("toggled.EnchantmentMixin"))
				.range(null, "1.20.6", () -> {})
				.run();
		
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER)
			return;
		
		Version.newSwitch()
				.range("1.20.0", null, () -> output.add("toggled.DrawContextMixin"))
				.range("1.19.3", "1.19.4", () -> output.add("toggled.ScreenMixin"))
				.range(null, "1.19.2", () -> {})
				.run();
		Version.newSwitch()
				.range("1.20.5", null, () -> output.add("toggled.ItemStackMixin"))
				.range(null, "1.20.4", () -> {})
				.run();
		Version.newSwitch()
				.range("1.20.5", null, () -> {
					output.add("toggled.RegistryEntryReferenceMixin");
					output.add("toggled.Registry1Mixin");
				})
				.range(null, "1.20.4", () -> {})
				.run();
		Version.newSwitch()
				.range("1.21.0", null, () -> output.add("toggled.TooltipMixin"))
				.range(null, "1.20.6", () -> {})
				.run();
		Version.newSwitch()
				.range("1.21.2", null, () -> output.add("toggled.GameRendererMixin_1_21_2"))
				.range(null, "1.21.1", () -> output.add("toggled.GameRendererMixin_1_21_1"))
				.run();
		Version.newSwitch()
				.range("1.21.4", null, () -> {
					output.add("toggled.ItemModelManagerMixin");
					output.add("toggled.ItemRenderStateLayerRenderStateMixin");
				})
				.range(null, "1.21.3", () -> output.add("toggled.BuiltinModelItemRendererMixin"))
				.run();
		Version.newSwitch()
				.range("1.21.2", null, () -> output.add("toggled.ClientPlayNetworkHandlerMixin"))
				.range(null, "1.21.1", () -> {})
				.run();
	}
	
}
