package me.mrCookieSlime.QuestWorld.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Why is there not a standard @Nullable? It's just frustrating. This means the
 * annotated entity could be assigned null as part of expected usage.
 * 
 * <p> This annotation will not be included during compilation, it is purely
 * for documentation
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.PARAMETER, ElementType.METHOD})
public @interface Nullable {
	String value() default "Can be null";
}
