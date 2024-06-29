package com.luneruniverse.minecraft.mod.nbteditor.tagreferences;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(PrefixRefersTo.PrefixRefersToList.class)
public @interface PrefixRefersTo {
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface PrefixRefersToList {
		public PrefixRefersTo[] value();
	}
	
	public String min() default "";
	public String max() default "";
	/**
	 * @return The prefix before @RefersTo paths, which may include forward slashes to enter compounds
	 */
	public String prefix();
}
