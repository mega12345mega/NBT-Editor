package com.luneruniverse.minecraft.mod.nbteditor.screens.factories;

import java.util.ArrayList;
import java.util.List;

import com.luneruniverse.minecraft.mod.nbteditor.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MultiVersionDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MultiVersionMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MultiVersionRegistry;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ItemEditorScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.FormattedTextFieldWidget;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class SignboardScreen extends ItemEditorScreen {
	
	private final Identifier texture;
	private FormattedTextFieldWidget lines;
	
	public SignboardScreen(ItemReference ref) {
		super(TextInst.of("Signboard"), ref);
		this.texture = new Identifier("minecraft", "textures/block/" +
				MultiVersionRegistry.ITEM.getId(ref.getItem().getItem()).getPath().replace("_sign", "_planks") + ".png");
	}
	
	private void setGlowing(boolean glowing) {
		item.getOrCreateSubNbt("BlockEntityTag").putBoolean("GlowingText", glowing);
		checkSave();
	}
	private boolean isGlowing() {
		return item.hasNbt() && item.getNbt().contains("BlockEntityTag", NbtElement.COMPOUND_TYPE) &&
				item.getNbt().getCompound("BlockEntityTag").getBoolean("GlowingText");
	}
	
	private Formatting getColor() {
		if (!item.hasNbt() || !item.getNbt().contains("BlockEntityTag", NbtElement.COMPOUND_TYPE))
			return Formatting.BLACK;
		NbtCompound blockData = item.getNbt().getCompound("BlockEntityTag");
		if (!blockData.contains("Color", NbtElement.STRING_TYPE))
			return Formatting.BLACK;
		Formatting color = Formatting.byName(blockData.getString("Color"));
		if (color == null || !color.isColor())
			return Formatting.BLACK;
		return color;
	}
	
	private void setLines(List<Text> lines) {
		NbtCompound blockData = item.getOrCreateSubNbt("BlockEntityTag");
		for (int i = 0; i < lines.size(); i++)
			blockData.putString("Text" + (i + 1), Text.Serializer.toJson(fixClickEvent(lines.get(i))));
		for (int i = lines.size(); i < 4; i++)
			blockData.putString("Text" + (i + 1), "{\"text\":\"\"}");
		checkSave();
	}
	private List<Text> getLines() {
		List<Text> output = new ArrayList<>();
		boolean hasBlockData = item.hasNbt() && item.getNbt().contains("BlockEntityTag", NbtElement.COMPOUND_TYPE);
		for (int i = 1; i <= 4; i++) {
			Text line;
			if (hasBlockData && item.getNbt().getCompound("BlockEntityTag").contains("Text" + i, NbtElement.STRING_TYPE))
				line = Text.Serializer.fromJson(item.getNbt().getCompound("BlockEntityTag").getString("Text" + i));
			else
				line = TextInst.of("");
			output.add(line);
		}
		return output;
	}
	
	private Text fixClickEvent(Text line) { // https://bugs.mojang.com/browse/MC-62833
		ClickEvent event = getClickEvent(line);
		if (event == null)
			return line;
		return TextInst.copy(line).styled(style -> style.withClickEvent(event));
	}
	private ClickEvent getClickEvent(Text text) {
		ClickEvent event = text.getStyle().getClickEvent();
		if (event != null)
			return event;
		for (Text child : text.getSiblings()) {
			event = getClickEvent(child);
			if (event != null)
				return event;
		}
		return null;
	}
	
	@Override
	protected void initEditor() {
		addDrawableChild(MultiVersionMisc.newButton(16, 64, 100, 20,
				TextInst.translatable("nbteditor.signboard.glowing." + (isGlowing() ? "enabled" : "disabled")), btn -> {
			boolean prevGlowing = isGlowing();
			setGlowing(!prevGlowing);
			btn.setMessage(TextInst.translatable("nbteditor.signboard.glowing." + (prevGlowing ? "disabled" : "enabled")));
		}));
		
		lines = addDrawableChild(FormattedTextFieldWidget.create(lines, 16, 64 + 24, width - 32, height - 80 - 24,
				getLines(), Style.EMPTY.withColor(getColor()), this::setLines));
		lines.setMaxLines(4);
		lines.setBackgroundColor(0);
		lines.setShadow(false);
	}
	
	@Override
	protected void preRenderEditor(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		MultiVersionDrawableHelper.drawTexture(matrices, texture, 16, 64 + 24 * 2, 0, 0, width - 32, height - 80 - 24 * 2);
	}
	
}
