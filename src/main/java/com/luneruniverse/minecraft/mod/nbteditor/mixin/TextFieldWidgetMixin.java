package com.luneruniverse.minecraft.mod.nbteditor.mixin;

import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.IdentifierInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Reflection;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.screens.Tickable;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.NamedTextFieldWidget;

import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.util.Identifier;

@Mixin(TextFieldWidget.class)
public abstract class TextFieldWidgetMixin implements Tickable {
	private static final Identifier TEXT_FIELD_INVALID = IdentifierInst.of("nbteditor", "widget/text_field_invalid");
	@ModifyArg(method = "renderWidget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V", ordinal = 0), index = 0)
	@Group(name = "renderButton", min = 1)
	private Identifier drawGuiTexture(Identifier texture) {
		TextFieldWidget source = (TextFieldWidget) (Object) this;
		if (source instanceof NamedTextFieldWidget named && !named.isValid())
			return TEXT_FIELD_INVALID;
		return texture;
	}
	@ModifyArg(method = "method_48579", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_332;method_25294(IIIII)V", ordinal = 0), index = 4, remap = false)
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
			return MVMisc.startVertex(buffer, x, y, z);
		return NamedTextFieldWidget.matrix.applyToVertex(buffer, (float) x, (float) y, (float) z);
	}
	
	private static final Supplier<Reflection.FieldReference> TextFieldWidget_focusedTicks =
			Reflection.getOptionalField(TextFieldWidget.class, "field_2107", "I");
	@Override
	public void tick() {
		TextFieldWidget source = (TextFieldWidget) (Object) this;
		Version.newSwitch()
				.range("1.20.2", null, () -> {})
				.range(null, "1.20.1", () -> TextFieldWidget_focusedTicks.get().set(source, (int) TextFieldWidget_focusedTicks.get().get(source) + 1))
				.run();
	}
}
