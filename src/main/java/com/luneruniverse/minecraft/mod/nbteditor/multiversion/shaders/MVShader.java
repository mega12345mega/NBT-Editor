package com.luneruniverse.minecraft.mod.nbteditor.multiversion.shaders;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.luneruniverse.minecraft.mod.nbteditor.multiversion.Version;

import net.minecraft.client.render.RenderLayer;

public abstract class MVShader {
	
	public static class Builder {
		
		private final String shaderName;
		private final String layerName;
		private final MVVertexFormat vertexFormat;
		private final MVDrawMode drawMode;
		private final int expectedBufferSize;
		private final boolean affectsOutline;
		
		private final List<Object> snippets;
		private boolean translucentBlendFunc;
		
		public Builder(String shaderName, String layerName, MVVertexFormat vertexFormat, MVDrawMode drawMode,
				int expectedBufferSize, boolean affectsOutline) {
			this.shaderName = shaderName;
			this.layerName = layerName;
			this.vertexFormat = vertexFormat;
			this.drawMode = drawMode;
			this.expectedBufferSize = expectedBufferSize;
			this.affectsOutline = affectsOutline;
			
			this.snippets = new ArrayList<>();
			this.translucentBlendFunc = false;
		}
		
		public Builder withSnippet(Supplier<Object> snippet) {
			Version.newSwitch()
					.range("1.21.5", null, () -> snippets.add(snippet.get()))
					.range(null, "1.21.4", () -> {})
					.run();
			return this;
		}
		public Builder withTranslucentBlendFunc(boolean translucentBlendFunc) {
			this.translucentBlendFunc = translucentBlendFunc;
			return this;
		}
		
		public String getShaderName() {
			return shaderName;
		}
		public String getLayerName() {
			return layerName;
		}
		public MVVertexFormat getVertexFormat() {
			return vertexFormat;
		}
		public MVDrawMode getDrawMode() {
			return drawMode;
		}
		public int getExpectedBufferSize() {
			return expectedBufferSize;
		}
		public boolean isAffectsOutline() {
			return affectsOutline;
		}
		
		public List<Object> getSnippets() {
			return snippets;
		}
		public boolean isTranslucentBlendFunc() {
			return translucentBlendFunc;
		}
		
		public MVShader build() {
			return Version.<MVShader>newSwitch()
					.range("1.21.5", null, () -> new MVShader3(this))
					.range("1.21.2", "1.21.4", () -> new MVShader2(this))
					.range(null, "1.21.1", () -> new MVShader1(this))
					.get();
		}
		
	}
	
	public abstract RenderLayer getLayer();
	
}
