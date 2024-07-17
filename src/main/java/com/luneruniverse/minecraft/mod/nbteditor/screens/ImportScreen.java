package com.luneruniverse.minecraft.mod.nbteditor.screens;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditor;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalBlock;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalEntity;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalItem;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalNBT;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVTooltip;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.ScreenTexts;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.ImageToLoreWidget;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.ImportPosWidget;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.NamedTextFieldWidget;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.ItemTagReferences;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.luneruniverse.minecraft.mod.nbteditor.util.TextUtil;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class ImportScreen extends OverlaySupportingScreen {
	
	public static void importFiles(List<Path> paths, Optional<Integer> defaultDataVersion) {
		List<Consumer<BlockPos>> posConsumers = new ArrayList<>();
		
		for (Path path : paths) {
			File file = path.toFile();
			if (!file.isFile())
				continue;
			
			if (file.getName().endsWith(".nbt")) {
				try (FileInputStream in = new FileInputStream(file)) {
					NbtCompound nbt = MainUtil.readNBT(in);
					if (defaultDataVersion.isEmpty() && !nbt.contains("DataVersion", NbtElement.NUMBER_TYPE))
						MainUtil.client.player.sendMessage(TextInst.translatable("nbteditor.nbt.import.data_version.missing", file.getName()), false);
					LocalNBT.deserialize(nbt, defaultDataVersion.orElse(Version.getDataVersion())).ifPresent(localNBT -> {
						if (localNBT instanceof LocalItem item)
							item.receive();
						else if (localNBT instanceof LocalBlock block)
							posConsumers.add(pos -> block.place(pos));
						else if (localNBT instanceof LocalEntity entity)
							posConsumers.add(pos -> entity.summon(MainUtil.client.world.getRegistryKey(), Vec3d.ofCenter(pos)));
					});
				} catch (Exception e) {
					NBTEditor.LOGGER.error("Error while importing a .nbt file", e);
					MainUtil.client.player.sendMessage(TextInst.literal(e.getClass().getName() + ": " + e.getMessage()).formatted(Formatting.RED), false);
				}
				continue;
			}
		}
		
		if (!posConsumers.isEmpty()) {
			ImportPosWidget.openImportPos(MainUtil.client.player.getBlockPos(),
					pos -> posConsumers.forEach(consumer -> consumer.accept(pos)));
			return;
		}
		
		ImageToLoreWidget.openImportFiles(paths, (file, imgLore) -> {
			String name = file.getName();
			int nameDot = name.lastIndexOf('.');
			if (nameDot != -1)
				name = name.substring(0, nameDot);
			
			ItemStack painting = new ItemStack(Items.PAINTING);
			painting.manager$setCustomName(TextInst.literal(name).styled(style -> style.withItalic(false).withColor(Formatting.GOLD)));
			ItemTagReferences.LORE.set(painting, imgLore);
			MainUtil.getWithMessage(painting);
		}, () -> {});
	}
	
	private final List<Text> msg;
	private NamedTextFieldWidget dataVersion;
	
	public ImportScreen() {
		super(TextInst.of("Import"));
		msg = TextUtil.getLongTranslatableTextLines("nbteditor.nbt.import.desc");
	}
	
	@Override
	protected void init() {
		super.init();
		dataVersion = addDrawableChild(
				new NamedTextFieldWidget(16, 64 + textRenderer.fontHeight * msg.size() + 16, 100, 16, dataVersion)
				.name(TextInst.translatable("nbteditor.nbt.import.data_version")));
		addDrawableChild(MVMisc.newButton(this.width - 116, this.height - 36, 100, 20, ScreenTexts.DONE, btn -> close()));
	}
	
	@Override
	protected void renderMain(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		dataVersion.setValid(dataVersion.getText().isEmpty() || Version.getDataVersion(dataVersion.getText()).isPresent());
		
		super.renderBackground(matrices);
		super.renderMain(matrices, mouseX, mouseY, delta);
		for (int i = 0; i < msg.size(); i++)
			MVDrawableHelper.drawText(matrices, textRenderer, msg.get(i), 16, 64 + textRenderer.fontHeight * i, -1, true);
		if (dataVersion.isHovered())
			new MVTooltip("nbteditor.nbt.import.data_version.desc").render(matrices, mouseX, mouseY);
		MainUtil.renderLogo(matrices);
	}
	
	@Override
	public void filesDragged(List<Path> paths) {
		importFiles(paths, Version.getDataVersion(dataVersion.getText()));
	}
	
}
