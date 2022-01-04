package com.monash.analytics.audio.service.features;

/**
 * Allow the implementor to be saved
 */
@FunctionalInterface
public interface Archivable {
    /**
     * a function to save
     */
    void save();
}
