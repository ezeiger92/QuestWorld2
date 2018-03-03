package com.questworld.util;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Log {
	private static Logger activeLogger = null;
	
	public static void setLogger(Logger logger) {
		activeLogger = logger;
	}
	
	public static void log(Level level, String message) {
		activeLogger.log(level, message);
	}
	
	public static void severe(String message) {
		activeLogger.severe(message);
	}
	
	public static void warning(String message) {
		activeLogger.warning(message);
	}
	
	public static void info(String message) {
		activeLogger.info(message);
	}
	
	public static void config(String message) {
		activeLogger.config(message);
	}
	
	public static void fine(String message) {
		activeLogger.fine(message);
	}
	
	public static void finer(String message) {
		activeLogger.finer(message);
	}
	
	public static void finest(String message) {
		activeLogger.finest(message);
	}
}
