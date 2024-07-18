package com.luneruniverse.minecraft.mod.nbteditor.screens;

import java.util.Optional;

import org.lwjgl.glfw.GLFW;

import com.luneruniverse.minecraft.mod.nbteditor.NBTEditor;
import com.luneruniverse.minecraft.mod.nbteditor.NBTEditorClient;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.EditableText;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVDrawableHelper;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVTooltip;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.TextInst;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;
import com.luneruniverse.minecraft.mod.nbteditor.screens.containers.ClientChestScreen;
import com.luneruniverse.minecraft.mod.nbteditor.screens.widgets.NamedTextFieldWidget;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ClientChestDataVersionScreen extends TickableSupportingScreen {
	
	private final boolean needsDataVersion;
	private final boolean outdated;
	private final Text msg;
	private NamedTextFieldWidget dataVersion;
	private ButtonWidget updatePageBtn;
	
	public ClientChestDataVersionScreen(boolean needsDataVersion, boolean outdated) {
		super(TextInst.of("Client Chest DataVersion"));
		this.needsDataVersion = needsDataVersion;
		this.outdated = outdated;
		this.msg = TextInst.translatable("nbteditor.client_chest.data_version." + (outdated ? "old" : "new"), ClientChestScreen.PAGE + 1);
	}
	
	@Override
	protected void init() {
		EditableText prevKeybind = TextInst.translatable("nbteditor.keybind.page.down");
		EditableText nextKeybind = TextInst.translatable("nbteditor.keybind.page.up");
		if (ConfigScreen.isInvertedPageKeybinds()) {
			EditableText temp = prevKeybind;
			prevKeybind = nextKeybind;
			nextKeybind = temp;
		}
		
		int dontUpdatePageX = width / 2 + (outdated ? 54 : -50);
		
		this.addDrawableChild(MVMisc.newButton(dontUpdatePageX + 56, height / 2, 20, 20, TextInst.of("<"), btn -> prevPage(),
				ConfigScreen.isKeybindsHidden() ? null : new MVTooltip(TextInst.literal("")
						.append(prevKeybind).append(TextInst.translatable("nbteditor.keybind.page.prev")))))
				.active = ClientChestScreen.PAGE > 0;
		
		this.addDrawableChild(MVMisc.newButton(dontUpdatePageX + 80, height / 2, 20, 20, TextInst.of(">"), btn -> nextPage(),
				ConfigScreen.isKeybindsHidden() ? null : new MVTooltip(TextInst.literal("")
						.append(nextKeybind).append(TextInst.translatable("nbteditor.keybind.page.next")))))
				.active = ClientChestScreen.PAGE < NBTEditorClient.CLIENT_CHEST.getPageCount() - 1;
		
		addDrawableChild(MVMisc.newButton(dontUpdatePageX, height / 2, 52, 20,
				TextInst.translatable("nbteditor.client_chest.data_version.dont_update_page"), btn -> close()));
		
		if (!outdated)
			return;
		
		dataVersion = addDrawableChild(
				new NamedTextFieldWidget(width / 2 - 50, height / 2 - 20, 100, 16, dataVersion)
				.name(TextInst.translatable("nbteditor.nbt.import.data_version")));
		updatePageBtn = addDrawableChild(MVMisc.newButton(width / 2 - 154, height / 2, 100, 20,
				TextInst.translatable("nbteditor.client_chest.data_version.update_page"), btn -> {
					Optional<Integer> dataVersionValue = Version.getDataVersion(dataVersion.getText())
							.filter(value -> value < Version.getDataVersion());
					if (needsDataVersion && dataVersionValue.isEmpty())
						return;
					try {
						NBTEditorClient.CLIENT_CHEST.updatePage(ClientChestScreen.PAGE, dataVersionValue);
						MainUtil.client.player.sendMessage(NBTEditorClient.CLIENT_CHEST.attachShowFolder(
								TextInst.translatable("nbteditor.client_chest.data_version.update_page_success",
										TextInst.literal(ClientChestScreen.PAGE + 1 + "").formatted(Formatting.GREEN))));
						ClientChestScreen.show();
					} catch (Exception e) {
						NBTEditor.LOGGER.error("Error while updating client chest page", e);
						client.player.sendMessage(TextInst.translatable("nbteditor.client_chest.data_version.update_page_error",
								TextInst.literal(ClientChestScreen.PAGE + 1 + "").formatted(Formatting.RED)), false);
					}
				}));
		addDrawableChild(MVMisc.newButton(width / 2 - 50, height / 2, 100, 20,
				TextInst.translatable("nbteditor.client_chest.data_version.update_all_pages"), btn -> {
					Optional<Integer> dataVersionValue = Version.getDataVersion(dataVersion.getText())
							.filter(value -> value < Version.getDataVersion());
					try {
						NBTEditorClient.CLIENT_CHEST.updateAllPages(dataVersionValue);
						MainUtil.client.player.sendMessage(NBTEditorClient.CLIENT_CHEST.attachShowFolder(
								TextInst.translatable("nbteditor.client_chest.data_version.update_all_pages_success")));
						ClientChestScreen.show();
					} catch (Exception e) {
						NBTEditor.LOGGER.error("Error while updating client chest pages", e);
						client.player.sendMessage(TextInst.translatable("nbteditor.client_chest.data_version.update_all_pages_error"), false);
					}
				}, new MVTooltip("nbteditor.client_chest.data_version.update_all_pages.desc")));
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		if (outdated) {
			dataVersion.setValid((!needsDataVersion && dataVersion.getText().isEmpty()) ||
					Version.getDataVersion(dataVersion.getText()).filter(value -> value < Version.getDataVersion()).isPresent());
			if (needsDataVersion)
				updatePageBtn.active = dataVersion.isValid();
		}
		
		super.renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
		MVDrawableHelper.drawCenteredTextWithShadow(matrices, textRenderer, msg, width / 2, height / 2 - 10 - (outdated ? 20 : 0) - textRenderer.fontHeight / 2, -1);
		if (outdated && dataVersion.isHovered())
			new MVTooltip("nbteditor.nbt.import.data_version.desc").render(matrices, mouseX, mouseY);
		MainUtil.renderLogo(matrices);
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_PAGE_UP || keyCode == GLFW.GLFW_KEY_PAGE_DOWN) {
			boolean prev = (keyCode == GLFW.GLFW_KEY_PAGE_DOWN);
			if (ConfigScreen.isInvertedPageKeybinds())
				prev = !prev;
			if (prev)
				prevPage();
			else
				nextPage();
		}
		
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	
	private void prevPage() {
		if (ClientChestScreen.PAGE > 0) {
			ClientChestScreen.PAGE--;
			ClientChestScreen.show();
		}
	}
	private void nextPage() {
		if (ClientChestScreen.PAGE < NBTEditorClient.CLIENT_CHEST.getPageCount() - 1) {
			ClientChestScreen.PAGE++;
			ClientChestScreen.show();
		}
	}
	
}
