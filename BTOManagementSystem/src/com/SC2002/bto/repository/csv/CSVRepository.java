package com.SC2002.bto.repository.csv;

import com.SC2002.bto.repository.IRepository;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Base class for CSV-based repositories.
 * Follows the Template Method pattern by providing common CSV operations.
 * 
 * @param <T> The entity type
 * @param <ID> The ID type
 */
public abstract class CSVRepository<T, ID> implements IRepository<T, ID> {
    
    protected final String filePath;
    
    /**
     * Constructs a CSVRepository with the specified file path.
     * 
     * @param filePath The path of the CSV file
     */
    public CSVRepository(String filePath) {
        this.filePath = filePath;
    }
    
    /**
     * Reads all lines from the CSV file.
     * 
     * @return A list of lines from the CSV file
     */
    protected List<String> readAllLines() {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            System.out.println("Error reading CSV file: " + e.getMessage());
        }
        return lines;
    }
    
    /**
     * Writes all lines to the CSV file.
     * 
     * @param lines The lines to write
     * @return true if the write was successful, false otherwise
     */
    protected boolean writeAllLines(List<String> lines) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            System.out.println("Error writing CSV file: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Parses a CSV line into tokens.
     * 
     * @param line The CSV line to parse
     * @return A list of tokens
     */
    protected List<String> parseCSVLine(String line) {
        List<String> tokens = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder currentToken = new StringBuilder();
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '\"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                tokens.add(currentToken.toString());
                currentToken = new StringBuilder();
            } else {
                currentToken.append(c);
            }
        }
        
        // Add the last token
        tokens.add(currentToken.toString());
        
        return tokens;
    }
    
    /**
     * Finds an entity by its ID.
     * 
     * @param id The ID to search for
     * @param idExtractor A function to extract the ID from a line
     * @param lineToEntity A function to convert a line to an entity
     * @return An Optional containing the entity if found, empty otherwise
     */
    protected Optional<T> findById(ID id, Function<String, ID> idExtractor, Function<String, T> lineToEntity) {
        List<String> lines = readAllLines();
        
        // Skip header
        if (!lines.isEmpty()) {
            lines.remove(0);
        }
        
        for (String line : lines) {
            ID lineId = idExtractor.apply(line);
            if (lineId.equals(id)) {
                return Optional.of(lineToEntity.apply(line));
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Finds all entities.
     * 
     * @param lineToEntity A function to convert a line to an entity
     * @return A list of all entities
     */
    protected List<T> findAll(Function<String, T> lineToEntity) {
        List<String> lines = readAllLines();
        List<T> entities = new ArrayList<>();
        
        // Skip header
        if (!lines.isEmpty()) {
            lines.remove(0);
        }
        
        for (String line : lines) {
            entities.add(lineToEntity.apply(line));
        }
        
        return entities;
    }
    
    /**
     * Saves an entity.
     * 
     * @param entity The entity to save
     * @param idExtractor A function to extract the ID from an entity
     * @param entityToLine A function to convert an entity to a line
     * @param headerLine The header line for the CSV file
     * @return The saved entity
     */
    protected T save(T entity, Function<T, ID> idExtractor, Function<T, String> entityToLine, String headerLine) {
        List<String> lines = readAllLines();
        boolean found = false;
        
        // If file is empty, add header
        if (lines.isEmpty()) {
            lines.add(headerLine);
        }
        
        ID id = idExtractor.apply(entity);
        
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            ID lineId = idExtractor.apply(lineToEntity(line));
            
            if (lineId.equals(id)) {
                lines.set(i, entityToLine.apply(entity));
                found = true;
                break;
            }
        }
        
        if (!found) {
            lines.add(entityToLine.apply(entity));
        }
        
        writeAllLines(lines);
        
        return entity;
    }
    
    /**
     * Deletes an entity.
     * 
     * @param entity The entity to delete
     * @param idExtractor A function to extract the ID from an entity
     */
    protected void delete(T entity, Function<T, ID> idExtractor) {
        List<String> lines = readAllLines();
        ID id = idExtractor.apply(entity);
        
        // Skip header
        if (!lines.isEmpty()) {
            String header = lines.remove(0);
            List<String> newLines = new ArrayList<>();
            newLines.add(header);
            
            for (String line : lines) {
                ID lineId = idExtractor.apply(lineToEntity(line));
                if (!lineId.equals(id)) {
                    newLines.add(line);
                }
            }
            
            writeAllLines(newLines);
        }
    }
    
    /**
     * Checks if an entity exists by its ID.
     * 
     * @param id The ID to check
     * @param idExtractor A function to extract the ID from a line
     * @return true if the entity exists, false otherwise
     */
    protected boolean existsById(ID id, Function<String, ID> idExtractor) {
        List<String> lines = readAllLines();
        
        // Skip header
        if (!lines.isEmpty()) {
            lines.remove(0);
        }
        
        for (String line : lines) {
            ID lineId = idExtractor.apply(line);
            if (lineId.equals(id)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Converts a line to an entity.
     * 
     * @param line The line to convert
     * @return The entity
     */
    protected abstract T lineToEntity(String line);
    
    /**
     * Converts an entity to a line.
     * 
     * @param entity The entity to convert
     * @return The line
     */
    protected abstract String entityToLine(T entity);
}
