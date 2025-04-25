package com.SC2002.bto.repository;

import java.util.List;
import java.util.Optional;

/**
 * Generic repository interface for CRUD operations.
 * Follows the Repository Pattern for data access.
 * 
 * @param <T> The entity type
 * @param <ID> The ID type
 */
public interface IRepository<T, ID> {
    
    /**
     * Finds all entities.
     * 
     * @return A list of all entities
     */
    List<T> findAll();
    
    /**
     * Finds an entity by its ID.
     * 
     * @param id The ID to search for
     * @return An Optional containing the entity if found, empty otherwise
     */
    Optional<T> findById(ID id);
    
    /**
     * Saves an entity.
     * 
     * @param entity The entity to save
     * @return The saved entity
     */
    T save(T entity);
    
    /**
     * Saves multiple entities.
     * 
     * @param entities The entities to save
     * @return The saved entities
     */
    List<T> saveAll(List<T> entities);
    
    /**
     * Deletes an entity.
     * 
     * @param entity The entity to delete
     */
    void delete(T entity);
    
    /**
     * Checks if an entity exists by its ID.
     * 
     * @param id The ID to check
     * @return true if the entity exists, false otherwise
     */
    boolean existsById(ID id);
}
