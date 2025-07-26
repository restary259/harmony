package com.harmonymod.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for SLF4J Logger providing structured logging for Harmony.
 */
public class HarmonyLogger {
    private final Logger logger;

    public HarmonyLogger(String modid) {
        this.logger = LoggerFactory.getLogger(modid);
    }

    public void info(String message, Object... args) {
        logger.info(message, args);
    }

    public void debug(String message, Object... args) {
        logger.debug(message, args);
    }

    public void warn(String message, Object... args) {
        logger.warn(message, args);
    }

    public void error(String message, Object... args) {
        logger.error(message, args);
    }
}
