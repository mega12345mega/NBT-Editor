package com.luneruniverse.minecraft.mod.nbteditor.tagreferences;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

public class SignSideTagReference extends TagReference {
	
	@RefersTo(min = "1.20.0", path = "has_glowing_text")
	@RefersTo(max = "1.19.4", path = "GlowingText")
	public boolean glowing;
	
	@RefersTo(min = "1.20.0", path = "color")
	@RefersTo(max = "1.19.4", path = "Color")
	public String color;
	
	@RefersTo(min = "1.20.0", path = "messages")
	private Text[] messages;
	
	@RefersTo(max = "1.19.4", path = "Text1")
	private Text text1;
	@RefersTo(max = "1.19.4", path = "Text2")
	private Text text2;
	@RefersTo(max = "1.19.4", path = "Text3")
	private Text text3;
	@RefersTo(max = "1.19.4", path = "Text4")
	private Text text4;
	
	@RefersToProxy(min = "1.20.0", value = "messages")
	@RefersToProxy(max = "1.19.4", value = "[Text1, Text2, Text3, Text4]")
	public Text[] text;
	
	public SignSideTagReference(int[] version) {
		super(version);
	}
	public SignSideTagReference() {
		super();
	}
	
	@Override
	public void load(NbtCompound nbt) {
		super.load(nbt);
		text = Version.<Text[]>newSwitch(version)
				.range("1.20.0", null, () -> messages)
				.range(null, "1.19.4", () -> new Text[] {text1, text2, text3, text4})
				.get();
	}
	
	@Override
	public void save(NbtCompound nbt) {
		Version.newSwitch(version)
				.range("1.20.0", null, () -> messages = text)
				.range(null, "1.19.4", () -> {
					text1 = text[0];
					text2 = text[1];
					text3 = text[2];
					text4 = text[3];
				})
				.run();
		super.save(nbt);
	}
	
}
