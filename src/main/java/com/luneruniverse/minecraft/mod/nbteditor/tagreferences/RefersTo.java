package com.luneruniverse.minecraft.mod.nbteditor.tagreferences;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(RefersTo.RefersToList.class)
public @interface RefersTo {
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface RefersToList {
		public RefersTo[] value();
	}
	
	public String min() default "";
	public String max() default "";
	public String path();
}
