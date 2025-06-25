package com.luneruniverse.minecraft.mod.nbteditor.screens.factories;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalItem;
import com.luneruniverse.minecraft.mod.nbteditor.localnbt.LocalNBT;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.IdentifierInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVTooltip;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.nbt.NBTManagers;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.BlockReference;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.NBTReference;
import com.luneruniverse.minecraft.mod.nbteditor.nbtreferences.itemreferences.ItemReference;
import com.luneruniverse.minecraft.mod.nbteditor.screens.LocalEditorScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.ButtonDropdownWidget;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.FormattedTextFieldWidget;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.ItemTagReferences;
import com.luneruniverse.minecraft.mod.nbteditor.tagreferences.SignSideTagReferences;
import com.luneruniverse.minecraft.mod.nbteditor.util.StyleUtil;

import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.Block;
import net.minecraft.block.HangingSignBlock;
import net.minecraft.block.WallHangingSignBlock;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.HangingSignItem;
import net.minecraft.item.SignItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class SignboardScreen<L extends LocalNBT> extends LocalEditorScreen<L> {
	
	// Double sided & waxable
	private static boolean NEW_FEATURES = Version.<Boolean>newSwitch()
			.range("1.20.0", null, true)
			.range(null, "1.19.4", false)
			.get();
	
	private static int getRenderedColor(DyeColor dye) {
		if (dye == DyeColor.BLACK)
			return 0xFFF0EBCC;
		return MVMisc.scaleRgb(dye.getSignColor(), 0.4);
	}
	
	private final Identifier texture;
	private boolean back;
	private FormattedTextFieldWidget lines;
	
	public SignboardScreen(NBTReference<L> ref) {
		super(TextInst.of("Signboard"), ref);
		
		String woodType;
		boolean hanging;
		if (NEW_FEATURES) {
			Block block = null;
			if (ref instanceof ItemReference itemRef)
				block = ((SignItem) itemRef.getItem().getItem()).getBlock();
			else if (ref instanceof BlockReference blockRef)
				block = blockRef.getBlock();
			woodType = AbstractSignBlock.getWoodType(block).name();
			hanging = block instanceof HangingSignBlock || block instanceof WallHangingSignBlock;
		} else {
			String id = ref.getId().getPath();
			woodType = id.replaceAll("(_wall)?(_hanging)?_sign$", "");
			hanging = id.matches("^[a-z_]+_hanging_sign$");
		}
		String textureName;
		if (hanging) {
			textureName = switch (woodType) {
				case "crimson" -> "stripped_crimson_stem";
				case "warped" -> "stripped_warped_stem";
				case "bamboo" -> "bamboo_planks";
				default -> "stripped_" + woodType + "_log";
			};
		} else
			textureName = woodType + "_planks";
		texture = IdentifierInst.of("minecraft", "textures/block/" + textureName + ".png");
		
		if (NBTManagers.COMPONENTS_EXIST) {
			if (localNBT instanceof LocalItem localItem) {
				NbtCompound nbt = ItemTagReferences.BLOCK_ENTITY_DATA.get(localItem.getEditableItem());
				nbt.putString("id",
						localItem.getItemType() instanceof HangingSignItem ? "minecraft:hanging_sign" : "minecraft:sign");
				ItemTagReferences.BLOCK_ENTITY_DATA.set(localItem.getEditableItem(), nbt);
			}
		}
	}
	
	private NbtCompound getSideNbt() {
		NbtCompound nbt;
		if (localNBT instanceof LocalItem localItem)
			nbt = ItemTagReferences.BLOCK_ENTITY_DATA.get(localItem.getEditableItem());
		else {
			nbt = localNBT.getNBT();
			if (nbt == null)
				return new NbtCompound();
		}
		
		if (NEW_FEATURES)
			return nbt.getCompound(back ? "back_text" : "front_text");
		return nbt;
	}
	private void setSideNbt(NbtCompound sideNbt) {
		if (!NEW_FEATURES) {
			if (localNBT instanceof LocalItem localItem)
				ItemTagReferences.BLOCK_ENTITY_DATA.set(localItem.getEditableItem(), sideNbt);
			else
				localNBT.setNBT(sideNbt);
			return;
		}
		
		if (localNBT instanceof LocalItem localItem) {
			NbtCompound nbt = ItemTagReferences.BLOCK_ENTITY_DATA.get(localItem.getEditableItem());
			nbt.put(back ? "back_text" : "front_text", sideNbt);
			ItemTagReferences.BLOCK_ENTITY_DATA.set(localItem.getEditableItem(), nbt);
		} else {
			NbtCompound nbt = localNBT.getNBT();
			nbt.put(back ? "back_text" : "front_text", sideNbt);
			localNBT.setNBT(nbt);
		}
	}
	private void modifySideNbt(Consumer<NbtCompound> modifier) {
		NbtCompound sideNbt = getSideNbt();
		modifier.accept(sideNbt);
		setSideNbt(sideNbt);
	}
	
	private void setWaxed(boolean waxed) {
		if (!NEW_FEATURES)
			throw new IllegalStateException("Incorrect version!");
		
		if (localNBT instanceof LocalItem localItem) {
			NbtCompound nbt = ItemTagReferences.BLOCK_ENTITY_DATA.get(localItem.getEditableItem());
			nbt.putBoolean("is_waxed", waxed);
			ItemTagReferences.BLOCK_ENTITY_DATA.set(localItem.getEditableItem(), nbt);
		} else {
			NbtCompound nbt = localNBT.getNBT();
			nbt.putBoolean("is_waxed", waxed);
			localNBT.setNBT(nbt);
		}
		checkSave();
	}
	private boolean isWaxed() {
		NbtCompound nbt;
		if (localNBT instanceof LocalItem localItem)
			nbt = ItemTagReferences.BLOCK_ENTITY_DATA.get(localItem.getEditableItem());
		else
			nbt = localNBT.getNBT();
		return nbt != null && nbt.getBoolean("is_waxed");
	}
	
	private void setGlowing(boolean glowing) {
		modifySideNbt(nbt -> SignSideTagReferences.GLOWING.set(nbt, glowing));
		checkSave();
	}
	private boolean isGlowing() {
		return SignSideTagReferences.GLOWING.get(getSideNbt());
	}
	
	private void setColor(DyeColor color) {
		modifySideNbt(nbt -> SignSideTagReferences.COLOR.set(nbt, color.getName()));
		checkSave();
	}
	private DyeColor getColor() {
		return DyeColor.byName(SignSideTagReferences.COLOR.get(getSideNbt()), DyeColor.BLACK);
	}
	
	private void setLines(List<Text> lines) {
		modifySideNbt(nbt -> SignSideTagReferences.TEXT.set(nbt, lines.stream()
				.map(this::fixClickEvent).map(line -> NEW_FEATURES ? fixEditable(line) : line).toList()));
		checkSave();
	}
	private List<Text> getLines() {
		List<Text> output = SignSideTagReferences.TEXT.get(getSideNbt());
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
		if (StyleUtil.identical(line.getStyle(), Style.EMPTY) && line.getSiblings().size() == 1 &&
				line.getSiblings().get(0).getSiblings().isEmpty()) {
			return line.getSiblings().get(0);
		}
		return line;
	}
	
	@Override
	protected void initEditor() {
		if (NEW_FEATURES) {
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
		
		int glowingBtnX = 16 + (NEW_FEATURES ? 104 * 2 : 0);
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
