package me.mrCookieSlime.QuestWorld.api.annotation;

import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * The annotated entity could be assigned <tt>null</tt> as part of expected
 * usage.
 * 
 * <p> When used on a parameter, it indicates <tt>null</tt> is an acceptable
 * argument and the function will not blindly use that value without checking
 * against <tt>null</tt>.
 * 
 * <p> When used on a function, it indicates <tt>null</tt> may be returned and
 * the user should check before using the value. If implementing a
 * <tt>@Nullable</tt> function, you may return <tt>null</tt>.
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
