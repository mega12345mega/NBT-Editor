package com.luneruniverse.minecraft.mod.nbteditor.multiversion.shaders;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayer.MultiPhaseParameters;

public class MVShader3 extends MVShader {
	
	public static final List<RenderPipeline> RENDER_PIPELINES = new ArrayList<>();
	
	private final RenderLayer layer;
	
	public MVShader3(MVShader.Builder builder) {
		RenderPipeline.Builder pipelineBuilder = RenderPipeline.builder(builder.getSnippets().stream()
				.map(snippet -> (RenderPipeline.Snippet) snippet).toArray(RenderPipeline.Snippet[]::new))
				.withLocation("pipeline/" + builder.getLayerName())
				.withVertexShader("core/" + builder.getShaderName())
				.withFragmentShader("core/" + builder.getShaderName())
				.withVertexFormat((VertexFormat) builder.getVertexFormat().getInternalValue(),
						(VertexFormat.DrawMode) builder.getDrawMode().getInternalValue());
		
		if (builder.isTranslucentBlendFunc())
			pipelineBuilder.withBlend(BlendFunction.TRANSLUCENT);
		
		RenderPipeline pipeline = RenderPipelines.register(pipelineBuilder.build());
		
		layer = RenderLayer.of(
				builder.getLayerName(),
				builder.getExpectedBufferSize(),
				pipeline,
				MultiPhaseParameters.builder().build(builder.isAffectsOutline()));
		
		RENDER_PIPELINES.add(pipeline);
	}
	
	@Override
	public RenderLayer getLayer() {
		return layer;
	}
	
}
