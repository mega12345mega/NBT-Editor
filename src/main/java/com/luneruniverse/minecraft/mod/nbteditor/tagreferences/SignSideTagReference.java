package com.luneruniverse.minecraft.mod.nbteditor.tagreferences;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

public class SignSideTagReference extends TagReference {
	
	@RefersTo(max = "1.19.4", path = "GlowingText")
	@RefersTo(min = "1.20.0", path = "has_glowing_text")
	public boolean glowing;
	
	@RefersTo(max = "1.19.4", path = "Color")
	@RefersTo(min = "1.20.0", path = "color")
	public String color;
	
	@RefersTo(max = "1.19.4", path = "Text1")
	private Text text1;
	@RefersTo(max = "1.19.4", path = "Text2")
	private Text text2;
	@RefersTo(max = "1.19.4", path = "Text3")
	private Text text3;
	@RefersTo(max = "1.19.4", path = "Text4")
	private Text text4;
	
	@RefersTo(min = "1.20.0", path = "messages")
	private Text[] messages;
	
	@RefersToProxy(max = "1.19.4", value = "[Text1, Text2, Text3, Text4]")
	@RefersToProxy(min = "1.20.0", value = "messages")
	public Text[] text;
	
	public SignSideTagReference(int[] version) {
		super(version);
	}
	public SignSideTagReference() {
		this(Version.get());
	}
	
	@Override
	public void load(NbtCompound nbt) {
		super.load(nbt);
		text = Version.<Text[]>newSwitch(version)
				.range(null, "1.19.4", () -> new Text[] {text1, text2, text3, text4})
				.range("1.20.0", null, () -> messages)
				.get();
	}
	
	@Override
	public void save(NbtCompound nbt) {
		Version.newSwitch(version)
				.range(null, "1.19.4", () -> {
					text1 = text[0];
					text2 = text[1];
					text3 = text[2];
					text4 = text[3];
				})
				.range("1.20.0", null, () -> messages = text)
				.run();
		super.save(nbt);
	}
	
}
