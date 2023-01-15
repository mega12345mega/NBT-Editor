package com.luneruniverse.minecraft.mod.nbteditor.mixin.source;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MultiVersionMisc;
import com.luneruniverse.minecraft.mod.nbteditor.screens.NamedTextFieldWidget;

import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexConsumer;

@Mixin(TextFieldWidget.class)
public class TextFieldWidgetMixin {
	@ModifyArg(method = "renderButton", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/TextFieldWidget;fill(Lnet/minecraft/client/util/math/MatrixStack;IIIII)V", ordinal = 0), index = 5)
	private int fill(int color) {
		TextFieldWidget source = (TextFieldWidget) (Object) this;
		if (source instanceof NamedTextFieldWidget named && !named.isValid())
			return 0xFFDF4949;
		return color;
	}
	@Redirect(method = "drawSelectionHighlight", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/BufferBuilder;vertex(DDD)Lnet/minecraft/client/render/VertexConsumer;"))
	private VertexConsumer vertex(BufferBuilder buffer, double x, double y, double z) {
		if (NamedTextFieldWidget.matrix == null)
			return buffer.vertex(x, y, z);
		return MultiVersionMisc.vertex(buffer, NamedTextFieldWidget.matrix, (float) x, (float) y, (float) z);
	}
}
