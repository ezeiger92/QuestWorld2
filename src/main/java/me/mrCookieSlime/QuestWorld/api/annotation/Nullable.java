package me.mrCookieSlime.QuestWorld.api.annotation;

import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Why is there not a standard @Nullable? It's just frustrating. This means the
 * annotated entity could be assigned null as part of expected usage.
 * 
 * <p> When used on a parameter, it indicates null is an acceptable argument and
 * the function will not blindly use that value without checking against null.
 * 
 * <p> When used on a function, it indicates null may be returned and the user
 * should check before using the value. If implementing a @Nullable function,
 * null may be returned.
 * 
 * <p> This will not be included during compilation, it is purely for
 * documentation.
 */
@Retention(SOURCE)
@Target({ElementType.METHOD, ElementType.PARAMETER})
public @interface Nullable {
	// Reason for nullability, if it helps documentation
	String value() default "Can be null";
}
