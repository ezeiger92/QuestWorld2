package me.mrCookieSlime.QuestWorld.api.annotation;

import java.lang.annotation.ElementType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation marks an entity that supports modification beyond
 * the scope of a function.
 * 
 * <p> When used on a parameter, it indicates the function may write
 * data into that parameter.
 * 
 * <p> When used on a function, it indicates modification of the returned
 * value may cause side-effects.
 * 
 * <p> This will not be included during compilation, it is purely for
 * documentation.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD, ElementType.PARAMETER})
public @interface Mutable {
	// Reason for mutability, if it helps documentation
	String value() default "Can be modified";
}
