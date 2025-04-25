package com.SC2002.bto.service.impl;

import com.SC2002.bto.service.IValidationService;
import com.SC2002.bto.utils.InputValidator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Implementation of the validation service.
 * Follows the Single Responsibility Principle by focusing only on validation operations.
 */
public class ValidationService implements IValidationService {
    
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("d/M/yy");
    
    @Override
    public boolean validateNric(String nric) {
        return InputValidator.validateNric(nric);
    }
    
    @Override
    public boolean validatePassword(String password) {
        return InputValidator.validatePassword(password);
    }
    
    @Override
    public boolean validateMaritalStatus(String maritalStatus) {
        return InputValidator.validateMaritalStatus(maritalStatus);
    }
    
    @Override
    public boolean validateSingleApplicantAge(int age, String maritalStatus) {
        return InputValidator.validateSingleApplicantAge(age, maritalStatus);
    }
    
    @Override
    public boolean validateMarriedApplicantAge(int age, String maritalStatus) {
        return InputValidator.validateMarriedApplicantAge(age, maritalStatus);
    }
    
    @Override
    public boolean validateFlatTypeEligibility(String flatType, String maritalStatus) {
        return InputValidator.validateFlatTypeEligibility(flatType, maritalStatus);
    }
    
    @Override
    public boolean validateApplicantEligibility(int age, String maritalStatus, String flatType) {
        return InputValidator.validateApplicantEligibility(age, maritalStatus, flatType);
    }
    
    @Override
    public boolean validateApplicationPeriod(String openingDate, String closingDate) {
        try {
            // Parse dates
            LocalDate opening = LocalDate.parse(openingDate, DATE_FMT);
            LocalDate closing = LocalDate.parse(closingDate, DATE_FMT);
            
            // Check if opening date is before closing date
            return !opening.isAfter(closing);
        } catch (DateTimeParseException e) {
            return false;
        }
    }
    
    @Override
    public boolean validateFlatTypes(String type1, String type2) {
        // Check if flat types are valid
        return ("2-Room".equalsIgnoreCase(type1) && "3-Room".equalsIgnoreCase(type2));
    }
    
    @Override
    public boolean validateFlatUnits(int units) {
        // Check if units is a positive number
        return units >= 0;
    }
    
    @Override
    public boolean validateFlatPrice(int price) {
        // Check if price is a positive number
        return price > 0;
    }
}
