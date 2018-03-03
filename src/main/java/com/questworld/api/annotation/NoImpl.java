package com.questworld.api.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * The tagged type is should NOT be implemented in any extension. It is the
 * public face of an internal object that must be constructed by QuestWorld
 * 
 * <p> This will not be included during compilation, it is purely for
 * documentation.
 */
@Retention(SOURCE)
@Target(TYPE)
public @interface NoImpl {
	String value() default "";
}
