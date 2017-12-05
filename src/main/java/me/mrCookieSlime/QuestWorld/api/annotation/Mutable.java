package me.mrCookieSlime.QuestWorld.api.annotation;

import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This annotation marks an entity that supports modification beyond
 * the scope of a function.
 * 
 * <p> When used on a parameter, it indicates the function may write
 * data into that parameter.
 * 
 * <p> When used on a function, it indicates modification of the returned
 * value may cause side-effects. If implementing a @Mutable function, a copy
 * may be returned, although a reference is recommended.
 * 
 * <p> This will not be included during compilation, it is purely for
 * documentation.
 */
@Retention(SOURCE)
@Target({ElementType.METHOD, ElementType.PARAMETER})
public @interface Mutable {
	// Reason for mutability, if it helps documentation
	String value() default "Can be modified";
}
