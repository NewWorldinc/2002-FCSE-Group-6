package com.SC2002.bto.di;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple service locator implementation for dependency injection.
 * Follows the Dependency Inversion Principle by allowing high-level modules
 * to depend on abstractions rather than concrete implementations.
 */
public class ServiceLocator {
    private static final Map<Class<?>, Object> services = new HashMap<>();
    
    /**
     * Registers a service implementation for a specific interface.
     * 
     * @param <T> The type of the service interface
     * @param serviceInterface The service interface class
     * @param serviceImplementation The service implementation instance
     */
    public static <T> void register(Class<T> serviceInterface, T serviceImplementation) {
        services.put(serviceInterface, serviceImplementation);
    }
    
    /**
     * Gets a service implementation for a specific interface.
     * 
     * @param <T> The type of the service interface
     * @param serviceInterface The service interface class
     * @return The service implementation instance
     * @throws IllegalArgumentException if no implementation is registered for the interface
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> serviceInterface) {
        Object service = services.get(serviceInterface);
        if (service == null) {
            throw new IllegalArgumentException("No implementation registered for " + serviceInterface.getName());
        }
        return (T) service;
    }
    
    /**
     * Checks if a service implementation is registered for a specific interface.
     * 
     * @param serviceInterface The service interface class
     * @return true if an implementation is registered, false otherwise
     */
    public static boolean isRegistered(Class<?> serviceInterface) {
        return services.containsKey(serviceInterface);
    }
    
    /**
     * Unregisters a service implementation for a specific interface.
     * 
     * @param serviceInterface The service interface class
     */
    public static void unregister(Class<?> serviceInterface) {
        services.remove(serviceInterface);
    }
    
    /**
     * Clears all registered service implementations.
     */
    public static void clear() {
        services.clear();
    }
}
