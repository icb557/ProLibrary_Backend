package com.librarySpring.librarySpring;

import com.librarySpring.librarySpring.Entities.Person.PersonErrorMessages;
import com.librarySpring.librarySpring.Entities.Person.interfaces.PersonRepository;
import com.librarySpring.librarySpring.Entities.Person.model.Person;
import com.librarySpring.librarySpring.Entities.Person.services.DeletePersonService;
import com.librarySpring.librarySpring.Exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeletePersonServiceTests {

    @Mock
    private PersonRepository personRepository;

    @InjectMocks
    private DeletePersonService deletePersonService;

    private Person testPerson;

    @BeforeEach
    void setUp() {
        testPerson = new Person();
        testPerson.setId(1);
        testPerson.setUsername("testUser");
        testPerson.setPassword("password123");
        testPerson.setRole("ADMIN"); // Valid role from PersonRoles enum
    }

    @Test
    void shouldDeletePersonSuccessfully() {
        // Given
        when(personRepository.findByUsername("testUser")).thenReturn(Optional.of(testPerson));
        doNothing().when(personRepository).deleteById(1);

        // When
        ResponseEntity<Void> response = deletePersonService.execute("testUser");

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(personRepository).findByUsername("testUser");
        verify(personRepository).deleteById(1);
    }

    @Test
    void shouldThrowExceptionWhenPersonNotFound() {
        // Given
        when(personRepository.findByUsername("nonExistentUser")).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> deletePersonService.execute("nonExistentUser")
        );

        assertEquals(PersonErrorMessages.PERSON_NOT_FOUND.getMessage(), exception.getMessage());
        verify(personRepository).findByUsername("nonExistentUser");
        verify(personRepository, never()).deleteById(anyInt());
    }
}