package me.mrCookieSlime.QuestWorld.api.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Methods with this annotation expect to be overridden to provide custom
 * behavior. Overriding class methods that do not have this annotation is not
 * recommended, and may cause unexpected behavior.
 * 
 * <p> Abstract methods will not have this annotation, as they must be
 * implemented for the class to function, whereas Control methods may or may not
 * be implemented at all.
 * 
 * <p> This will not be included during compilation, it is purely for
 * documentation.
 */
@Retention(SOURCE)
@Target(METHOD)
public @interface Control {
	String value() default "Control point";
}
