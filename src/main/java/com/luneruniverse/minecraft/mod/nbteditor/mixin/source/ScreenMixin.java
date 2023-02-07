package com.luneruniverse.minecraft.mod.nbteditor.mixin.source;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditor;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.screens.CreativeTab;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Formatting;

@Mixin(Screen.class)
public class ScreenMixin {
	@Inject(method = "clearChildren", at = @At("RETURN"))
	private void clearChildren(CallbackInfo info) {
		Screen source = (Screen) (Object) this;
		int i = -1;
		List<CreativeTab> tabs = new ArrayList<>();
		for (CreativeTab.CreativeTabData tab : CreativeTab.TABS) {
			if (tab.whenToShow().test(source))
				tabs.add(new CreativeTab(source, (++i) * (CreativeTab.WIDTH + 2) + 10, tab.item(), tab.onClick()));
		}
		source.addDrawableChild(new CreativeTab.CreativeTabGroup(tabs));
	}
	
	@Inject(method = "filesDragged", at = @At("HEAD"))
	private void filesDragged(List<Path> paths, CallbackInfo info) {
		if (((Object) this) instanceof HandledScreen) {
			for (Path path : paths) {
				File file = path.toFile();
				if (file.isFile() && file.getName().endsWith(".nbt")) {
					try (FileInputStream in = new FileInputStream(file)) {
						ItemStack item = ItemStack.fromNbt(MainUtil.readNBT(in));
						if (!item.isEmpty())
							MainUtil.getWithMessage(item);
					} catch (Exception e) {
						NBTEditor.LOGGER.error("Error while importing a .nbt file", e);
						MainUtil.client.player.sendMessage(TextInst.literal(e.getClass().getName() + ": " + e.getMessage()).formatted(Formatting.RED), false);
					}
				}
			}
		}
	}
}
