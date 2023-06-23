package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MultiVersionMisc;
import com.luneruniverse.minecraft.mod.nbteditor.screens.Tickable;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.NamedTextFieldWidget;

import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexConsumer;

@Mixin(TextFieldWidget.class)
public abstract class TextFieldWidgetMixin implements Tickable {
	@ModifyArg(method = "renderButton", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;fill(IIIII)V", ordinal = 0), index = 4, remap = false)
	@Group(name = "renderButton", min = 1)
	private int fillDrawContext(int color) {
		TextFieldWidget source = (TextFieldWidget) (Object) this;
		if (source instanceof NamedTextFieldWidget named && !named.isValid())
			return 0xFFDF4949;
		return color;
	}
	@ModifyArg(method = {"method_48579", "method_25359"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/class_342;method_25294(Lnet/minecraft/class_4587;IIIII)V", ordinal = 0), index = 5, remap = false)
	@Group(name = "renderButton", min = 1)
	private int fillMatrixStack(int color) {
		TextFieldWidget source = (TextFieldWidget) (Object) this;
		if (source instanceof NamedTextFieldWidget named && !named.isValid())
			return 0xFFDF4949;
		return color;
	}
	
	@Redirect(method = "method_1886(IIII)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_287;method_22912(DDD)Lnet/minecraft/class_4588;"), remap = false, require = 0)
	@SuppressWarnings("target")
	private VertexConsumer vertex(BufferBuilder buffer, double x, double y, double z) {
		if (NamedTextFieldWidget.matrix == null)
			return buffer.vertex(x, y, z);
		return MultiVersionMisc.vertex(buffer, NamedTextFieldWidget.matrix, (float) x, (float) y, (float) z);
	}
	
	@Shadow
	private int focusedTicks;
	@Override
	public void tick() {
		focusedTicks++;
	}
}
