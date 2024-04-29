package com.luneruniverse.minecraft.mod.nbteditor.tagreferences;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
@Repeatable(RefersToProxy.RefersToProxyList.class)
public @interface RefersToProxy {
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.CLASS)
	public @interface RefersToProxyList {
		public RefersToProxy[] value();
	}
	
	public String min() default "";
	public String max() default "";
	public String value();
}
