package com.luneruniverse.minecraft.mod.nbteditor.screens.factories;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalItem;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalNBT;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVRegistry;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVTooltip;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.BlockReference;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.NBTReference;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.screens.LocalEditorScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.ButtonDropdownWidget;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.FormattedTextFieldWidget;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.SignSideTagReference;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;
import com.luneruniverse.minecraft.mod.nbteditor.util.TextUtil;

import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.Block;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.SignItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

public class SignboardScreen<L extends LocalNBT> extends LocalEditorScreen<L, NBTReference<L>> {
	
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
		
		SignboardScreen<LocalItem> screen = new SignboardScreen<>(ref);
		screen.newFeatures = !NEW_FEATURES;
		boolean glowing = screen.isGlowing();
		DyeColor color = screen.getColor();
		List<Text> lines = screen.getLines();
		screen.newFeatures = NEW_FEATURES;
		screen.setGlowing(glowing);
		screen.setColor(color);
		screen.setLines(lines);
		ref.saveLocalNBT(screen.localNBT, () -> MainUtil.client.player.sendMessage(
				TextInst.translatable("nbteditor.signboard.import.success", version), false));
	}
	
	private static int getRenderedColor(DyeColor color) {
		if (color == DyeColor.BLACK)
			return -988212;
		int rgb = color.getSignColor();
		int r = (int) (ColorHelper.Argb.getRed(rgb) * 0.4D);
		int g = (int) (ColorHelper.Argb.getGreen(rgb) * 0.4D);
		int b = (int) (ColorHelper.Argb.getBlue(rgb) * 0.4D);
		return ColorHelper.Argb.getArgb(0, r, g, b);
	}
	
	private boolean newFeatures;
	private final Identifier texture;
	private boolean back;
	private FormattedTextFieldWidget lines;
	
	public SignboardScreen(NBTReference<L> ref) {
		super(TextInst.of("Signboard"), ref);
		newFeatures = NEW_FEATURES;
		if (newFeatures) {
			Block block = null;
			if (ref instanceof ItemReference itemRef)
				block = ((SignItem) itemRef.getItem().getItem()).getBlock();
			else if (ref instanceof BlockReference blockRef)
				block = MVRegistry.BLOCK.get(blockRef.getId());
			this.texture = new Identifier("minecraft", "textures/block/" +
					AbstractSignBlock.getWoodType(block).name() + "_planks.png");
		} else {
			this.texture = new Identifier("minecraft", "textures/block/" +
					ref.getId().getPath().replace("_sign", "_planks") + ".png");
		}
	}
	
	private NbtCompound getSideNbt(boolean create) {
		NbtCompound nbt = localNBT.getOrCreateNBT();
		
		if (localNBT instanceof LocalItem) {
			if (nbt.contains("BlockEntityTag", NbtElement.COMPOUND_TYPE))
				nbt = nbt.getCompound("BlockEntityTag");
			else if (!create)
				return new NbtCompound();
			else {
				NbtCompound blockEntityTag = new NbtCompound();
				nbt.put("BlockEntityTag", blockEntityTag);
				nbt = blockEntityTag;
			}
		}
		
		if (newFeatures) {
			String side = (back ? "back_text" : "front_text");
			if (nbt.contains(side, NbtElement.COMPOUND_TYPE))
				nbt = nbt.getCompound(side);
			else if (!create)
				return new NbtCompound();
			else {
				NbtCompound sideTag = new NbtCompound();
				nbt.put(side, sideTag);
				nbt = sideTag;
			}
		}
		
		return nbt;
	}
	private SignSideTagReference getSideTagRef() {
		SignSideTagReference tagRef = new SignSideTagReference(newFeatures ? new int[] {1, 20, 0} : new int[] {1, 19, 4});
		tagRef.load(getSideNbt(false));
		return tagRef;
	}
	private void setSideTagRef(SignSideTagReference tagRef) {
		tagRef.save(getSideNbt(true));
	}
	private void modifySideTagRef(Consumer<SignSideTagReference> tagRefConsumer) {
		SignSideTagReference tagRef = getSideTagRef();
		tagRefConsumer.accept(tagRef);
		setSideTagRef(tagRef);
	}
	
	private void setWaxed(boolean waxed) {
		if (!newFeatures)
			throw new IllegalStateException("Incorrect version!");
		localNBT.getOrCreateNBT().putBoolean("is_waxed", waxed);
		checkSave();
	}
	private boolean isWaxed() {
		NbtCompound blockTag = localNBT.getNBT();
		return blockTag != null && blockTag.getBoolean("is_waxed");
	}
	
	private void setGlowing(boolean glowing) {
		modifySideTagRef(tagRef -> tagRef.glowing = glowing);
		checkSave();
	}
	private boolean isGlowing() {
		return getSideTagRef().glowing;
	}
	
	private void setColor(DyeColor color) {
		modifySideTagRef(tagRef -> tagRef.color = color.getName());
		checkSave();
	}
	private DyeColor getColor() {
		return DyeColor.byName(getSideTagRef().color, DyeColor.BLACK);
	}
	
	private void setLines(List<Text> lines) {
		modifySideTagRef(tagRef -> tagRef.text = lines.stream().map(line -> fixClickEvent(line))
				.map(line -> newFeatures ? fixEditable(line) : line).toArray(Text[]::new));
		checkSave();
	}
	private List<Text> getLines() {
		List<Text> output = new ArrayList<>(Arrays.asList(getSideTagRef().text));
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
		if (TextUtil.styleEqualsExact(line.getStyle(), Style.EMPTY) && line.getSiblings().size() == 1 &&
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
		
		int glowingBtnX = 16 + (newFeatures ? 104 * 2 : 0);
		int glowingBtnY = 64;
		AtomicReference<ButtonWidget> glowingBtn = new AtomicReference<>();
		
		ButtonDropdownWidget colors = addSelectableChild(new ButtonDropdownWidget(glowingBtnX, glowingBtnY + 20, 20, 20, null, 20, 20) {
			@Override
			public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
				matrices.push();
				matrices.translate(0.0, 0.0, 2.0);
				super.render(matrices, mouseX, mouseY, delta);
				matrices.pop();
			}
		});
		for (DyeColor color : DyeColor.values()) {
			colors.addButton(TextInst.literal("⬛").styled(style -> style.withColor(getRenderedColor(color))), btn -> {
				setColor(color);
				colors.setOpen(false);
				glowingBtn.get().setMessage(TextInst.translatable("nbteditor.signboard.glowing.enabled")
						.styled(style -> style.withColor(getRenderedColor(getColor()))));
			}, new MVTooltip(TextInst.of(color.getName())));
		}
		colors.build();
		
		glowingBtn.set(addDrawableChild(MVMisc.newButton(glowingBtnX, glowingBtnY, 100, 20,
				TextInst.translatable("nbteditor.signboard.glowing." + (isGlowing() ? "enabled" : "disabled"))
				.styled(style -> style.withColor(getRenderedColor(getColor()))), btn -> {
			boolean prevGlowing = isGlowing();
			if (prevGlowing && hasShiftDown()) {
				colors.setOpen(true);
				return;
			}
			setGlowing(!prevGlowing);
			btn.setMessage(TextInst.translatable("nbteditor.signboard.glowing." + (prevGlowing ? "disabled" : "enabled"))
					.styled(style -> style.withColor(getRenderedColor(getColor()))));
			if (!prevGlowing)
				colors.setOpen(true);
		}, new MVTooltip("nbteditor.signboard.glowing.desc"))));
		
		lines = addDrawableChild(FormattedTextFieldWidget.create(lines, 16, 64 + 24, width - 32, height - 80 - 24,
				getLines(), Style.EMPTY.withColor(Formatting.BLACK), this::setLines));
		lines.setMaxLines(4);
		lines.setBackgroundColor(0);
		lines.setShadow(false);
		
		addDrawable(colors); // Render on top of FormattedTextFieldWidget highlights
	}
	
	@Override
	protected void preRenderEditor(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		MVDrawableHelper.drawTexture(matrices, texture, 16, 64 + 24 * 2, 0, 0, width - 32, height - 80 - 24 * 2);
	}
	
}
