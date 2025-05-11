package com.librarySpring.librarySpring;

import com.librarySpring.librarySpring.Entities.Person.PersonErrorMessages;
import com.librarySpring.librarySpring.Entities.Person.interfaces.PersonRepository;
import com.librarySpring.librarySpring.Entities.Person.model.Person;
import com.librarySpring.librarySpring.Entities.Person.model.PersonDTO;
import com.librarySpring.librarySpring.Entities.Person.services.GetPersonService;
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
public class GetPersonServiceTests {

    @Mock
    private PersonRepository personRepository;

    @InjectMocks
    private GetPersonService getPersonService;

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
    void shouldReturnPersonWhenFound() {
        // Given
        when(personRepository.findByUsername("testUser")).thenReturn(Optional.of(testPerson));

        // When
        ResponseEntity<PersonDTO> response = getPersonService.execute("testUser");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        PersonDTO personDTO = response.getBody();
        assertNotNull(personDTO);
        assertEquals("testUser", personDTO.getUsername());
        assertEquals("password123", personDTO.getPassword());
        assertEquals("ADMIN", personDTO.getRole());

        verify(personRepository).findByUsername("testUser");
    }

    @Test
    void shouldThrowExceptionWhenPersonNotFound() {
        // Given
        when(personRepository.findByUsername("nonExistentUser")).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> getPersonService.execute("nonExistentUser")
        );

        assertEquals(PersonErrorMessages.PERSON_NOT_FOUND.getMessage(), exception.getMessage());
        verify(personRepository).findByUsername("nonExistentUser");
    }
}