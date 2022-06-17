package com.luneruniverse.minecraft.mod.nbteditor.screens;

import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;

public class HideFlagsScreen extends Screen {
	
	private enum Flag {
		ENCHANTMENTS(Text.translatable("nbteditor.flag.enchantments"), 1, -215, -25),
		ATTRIBUTE_MODIFIERS(Text.translatable("nbteditor.flag.attribute_modifiers"), 2, -105, -25),
		UNBREAKABLE(Text.translatable("nbteditor.flag.unbreakable"), 4, 5, -25),
		CAN_DESTORY(Text.translatable("nbteditor.flag.can_destroy"), 8, 115, -25),
		CAN_PLACE_ON(Text.translatable("nbteditor.flag.can_place_on"), 16, -215, 5),
		MISC(Text.translatable("nbteditor.flag.misc"), 32, -105, 5),
		DYED_COLOR(Text.translatable("nbteditor.flag.dyed_color"), 64, 5, 5),
		ALL(Text.translatable("nbteditor.flag.all"), 127, 115, 5);
		
		private final Text text;
		private final int code;
		private final int offsetX;
		private final int offsetY;
		
		private Flag(Text text, int code, int offsetX, int offsetY) {
			this.text = text;
			this.code = code;
			this.offsetX = offsetX;
			this.offsetY = offsetY;
		}
		
		public int toggle(int code) {
			if (this.code == 127)
				return isEnabled(code) ? 0 : this.code;
			return (code & ~this.code) | (~code & this.code);
		}
		public boolean isEnabled(int code) {
			return (code & this.code) != 0;
		}
		
		public Text getText(int code) {
			return this.text.copy().formatted(isEnabled(code) ? Formatting.RED : Formatting.GREEN); // Not enabled means it isn't hidden
		}
	}
	
	
	
	private final ItemStack item;
	private final Hand hand;
	private int code;
	
	private ButtonWidget allBtn;
	
	public HideFlagsScreen(ItemStack item, Hand hand) {
		super(Text.of("Hide Flags"));
		
		this.item = item;
		this.hand = hand;
		this.code = item.getOrCreateNbt().getInt("HideFlags");
	}
	
	@Override
	protected void init() {
		this.clearChildren();
		
		for (Flag flag : Flag.values()) {
			this.addDrawableChild(allBtn = new ButtonWidget(width / 2 + flag.offsetX, height / 2 + flag.offsetY, 100, 20, flag.getText(code), btn -> {
				item.getOrCreateNbt().putInt("HideFlags", code = flag.toggle(code));
				MainUtil.saveItem(hand, item);
				if (flag == Flag.ALL)
					init();
				else {
					btn.setMessage(flag.getText(code));
					allBtn.setMessage(Flag.ALL.getText(code));
				}
			}));
		}
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		super.renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
		MainUtil.renderLogo(matrices);
	}
	
}
