package com.luneruniverse.minecraft.mod.nbteditor.screens.factories;

import java.util.ArrayList;
import java.util.List;

import com.luneruniverse.minecraft.mod.nbteditor.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVRegistry;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ItemEditorScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.FormattedTextFieldWidget;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.block.AbstractSignBlock;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.SignItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class SignboardScreen extends ItemEditorScreen {
	
	// Double sided & waxable
	private static boolean NEW_FEATURES = Version.<Boolean>newSwitch()
			.range("1.20.0", null, true)
			.range(null, "1.19.4", false)
			.get();
	
	public static void importSign(ItemReference ref) {
		Text version = TextInst.literal(NEW_FEATURES ? ">=1.20" : "<=1.19.4").formatted(Formatting.GOLD);
		
		NbtCompound nbt = ref.getItem().getNbt();
		if (nbt != null) {
			NbtCompound blockTag = nbt.getCompound("BlockEntityTag");
			boolean alreadyImported = false;
			if (NEW_FEATURES) {
				if (blockTag.contains("front_text") || blockTag.contains("back_text") || blockTag.contains("is_waxed"))
					alreadyImported = true;
			} else {
				if (blockTag.contains("Text1") || blockTag.contains("Text2") || blockTag.contains("Text3") ||
						blockTag.contains("Text4") || blockTag.contains("Color") || blockTag.contains("GlowingText"))
					alreadyImported = true;
			}
			if (alreadyImported) {
				MainUtil.client.player.sendMessage(
						TextInst.translatable("nbteditor.signboard.import.fail", version), false);
				return;
			}
		}
		
		SignboardScreen screen = new SignboardScreen(ref);
		screen.newFeatures = !NEW_FEATURES;
		boolean glowing = screen.isGlowing();
		Formatting color = screen.getColor();
		List<Text> lines = screen.getLines();
		screen.newFeatures = NEW_FEATURES;
		screen.setGlowing(glowing);
		screen.setColor(color);
		screen.setLines(lines);
		ref.saveItem(screen.item, () -> MainUtil.client.player.sendMessage(
				TextInst.translatable("nbteditor.signboard.import.success", version), false));
	}
	
	private boolean newFeatures;
	private final Identifier texture;
	private boolean back;
	private FormattedTextFieldWidget lines;
	
	public SignboardScreen(ItemReference ref) {
		super(TextInst.of("Signboard"), ref);
		newFeatures = NEW_FEATURES;
		if (newFeatures) {
			this.texture = new Identifier("minecraft", "textures/block/" +
					AbstractSignBlock.getWoodType(((SignItem) item.getItem()).getBlock()).name() + "_planks.png");
		} else {
			this.texture = new Identifier("minecraft", "textures/block/" +
					MVRegistry.ITEM.getId(ref.getItem().getItem()).getPath().replace("_sign", "_planks") + ".png");
		}
	}
	
	private NbtCompound getBlockTag(boolean create) {
		NbtCompound blockTag = item.getSubNbt("BlockEntityTag");
		if (blockTag != null || !create)
			return blockTag;
		return item.getOrCreateSubNbt("BlockEntityTag");
	}
	private NbtCompound getBlockSideTag(boolean create) {
		NbtCompound blockTag = getBlockTag(create);
		if (blockTag == null || !newFeatures)
			return blockTag;
		String key = (back ? "back_text" : "front_text");
		if (blockTag.contains(key, NbtElement.COMPOUND_TYPE))
			return blockTag.getCompound(key);
		if (!create)
			return null;
		NbtCompound sideTag = new NbtCompound();
		blockTag.put(key, sideTag);
		return sideTag;
	}
	
	private void setWaxed(boolean waxed) {
		if (!newFeatures)
			throw new IllegalStateException("Incorrect version!");
		getBlockTag(true).putBoolean("is_waxed", waxed);
		checkSave();
	}
	private boolean isWaxed() {
		NbtCompound blockTag = getBlockTag(false);
		return blockTag != null && blockTag.getBoolean("is_waxed");
	}
	
	private void setGlowing(boolean glowing) {
		getBlockSideTag(true).putBoolean(newFeatures ? "has_glowing_text" : "GlowingText", glowing);
		checkSave();
	}
	private boolean isGlowing() {
		NbtCompound sideTag = getBlockSideTag(false);
		return sideTag != null && sideTag.getBoolean(newFeatures ? "has_glowing_text" : "GlowingText");
	}
	
	private void setColor(Formatting color) {
		getBlockSideTag(true).putString(newFeatures ? "color" : "Color", color.getName());
	}
	private Formatting getColor() {
		NbtCompound sideTag = getBlockSideTag(false);
		if (sideTag == null)
			return Formatting.BLACK;
		Formatting color = Formatting.byName(sideTag.getString(newFeatures ? "color" : "Color"));
		if (color == null || !color.isColor())
			return Formatting.BLACK;
		return color;
	}
	
	private void setLines(List<Text> lines) {
		NbtCompound sideTag = getBlockSideTag(true);
		while (lines.size() < 4)
			lines.add(TextInst.of(""));
		if (newFeatures) {
			NbtList messages = new NbtList();
			for (Text line : lines)
				messages.add(NbtString.of(Text.Serializer.toJson(fixEditable(fixClickEvent(line)))));
			sideTag.put("messages", messages);
		} else {
			for (int i = 0; i < 4; i++)
				sideTag.putString("Text" + (i + 1), Text.Serializer.toJson(fixClickEvent(lines.get(i))));
		}
		checkSave();
	}
	private List<Text> getLines() {
		List<Text> output = new ArrayList<>();
		NbtCompound sideTag = getBlockSideTag(false);
		if (sideTag != null) {
			if (newFeatures) {
				NbtList messages = sideTag.getList("messages", NbtElement.STRING_TYPE);
				for (int i = 0; i < messages.size() && i < 4; i++)
					output.add(Text.Serializer.fromJson(messages.getString(i)));
			} else {
				for (int i = 1; i <= 4; i++) {
					if (sideTag.contains("Text" + i, NbtElement.STRING_TYPE))
						output.add(Text.Serializer.fromJson(sideTag.getString("Text" + i)));
					else
						output.add(TextInst.of(""));
				}
			}
		}
		while (output.size() < 4)
			output.add(TextInst.of(""));
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
	
	private Text fixEditable(Text line) { // {"extra":[{...}]} makes the sign uneditable
		if (MainUtil.styleEqualsExact(line.getStyle(), Style.EMPTY) && line.getSiblings().size() == 1 &&
				line.getSiblings().get(0).getSiblings().isEmpty()) {
			return line.getSiblings().get(0);
		}
		return line;
	}
	
	@Override
	protected void initEditor() {
		if (newFeatures) {
			addDrawableChild(MVMisc.newButton(16, 64, 100, 20,
					TextInst.translatable("nbteditor.signboard.side." + (back ? "back" : "front")), btn -> {
				back = !back;
				clearChildren();
				init();
			}));
			addDrawableChild(MVMisc.newButton(16 + 104, 64, 100, 20,
					TextInst.translatable("nbteditor.signboard.wax." + (isWaxed() ? "enabled" : "disabled")), btn -> {
				boolean prevWaxed = isWaxed();
				setWaxed(!prevWaxed);
				btn.setMessage(TextInst.translatable("nbteditor.signboard.wax." + (prevWaxed ? "disabled" : "enabled")));
			}));
		}
		addDrawableChild(MVMisc.newButton(16 + (newFeatures ? 104 * 2 : 0), 64, 100, 20,
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
		MVDrawableHelper.drawTexture(matrices, texture, 16, 64 + 24 * 2, 0, 0, width - 32, height - 80 - 24 * 2);
	}
	
}
