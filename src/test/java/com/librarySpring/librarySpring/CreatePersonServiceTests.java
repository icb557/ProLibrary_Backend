package com.librarySpring.librarySpring;

import com.librarySpring.librarySpring.Entities.Person.PersonErrorMessages;
import com.librarySpring.librarySpring.Entities.Person.interfaces.PersonRepository;
import com.librarySpring.librarySpring.Entities.Person.model.Person;
import com.librarySpring.librarySpring.Entities.Person.model.PersonDTO;
import com.librarySpring.librarySpring.Entities.Person.services.CreatePersonService;
import com.librarySpring.librarySpring.Exceptions.AttributeNotValidException;
import com.librarySpring.librarySpring.Exceptions.ResourceAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CreatePersonServiceTests {

    @Mock
    private PersonRepository personRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CreatePersonService createPersonService;

    private Person testPerson;

    @BeforeEach
    void setUp() {
        testPerson = new Person();
        testPerson.setId(1);
        testPerson.setUsername("testUser");
        testPerson.setPassword("password123");
        testPerson.setRole("ADMIN"); // Using valid role from PersonRoles enum
    }

    @Test
    void shouldCreatePersonSuccessfully() {
        // Given
        when(personRepository.findByUsername("testUser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(personRepository.save(any(Person.class))).thenReturn(testPerson);

        // When
        ResponseEntity<PersonDTO> response = createPersonService.execute(testPerson);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("testUser", response.getBody().getUsername());
        assertEquals("password123", response.getBody().getPassword());
        assertEquals("ADMIN", response.getBody().getRole());

        verify(personRepository).findByUsername("testUser");
        verify(passwordEncoder).encode("password123");
        verify(personRepository).save(any(Person.class));
    }

    @Test
    void shouldThrowExceptionWhenPersonAlreadyExists() {
        // Given
        when(personRepository.findByUsername("testUser")).thenReturn(Optional.of(testPerson));

        // When & Then
        ResourceAlreadyExistsException exception = assertThrows(
                ResourceAlreadyExistsException.class,
                () -> createPersonService.execute(testPerson)
        );

        assertEquals(PersonErrorMessages.PERSON_ALREADY_EXISTS.getMessage(), exception.getMessage());
        verify(personRepository).findByUsername("testUser");
        verify(passwordEncoder, never()).encode(anyString());
        verify(personRepository, never()).save(any(Person.class));
    }

    @Test
    void shouldThrowExceptionWhenUsernameIsBlank() {
        // Given
        Person personWithBlankUsername = new Person();
        personWithBlankUsername.setUsername("");
        personWithBlankUsername.setPassword("password123");
        personWithBlankUsername.setRole("ADMIN");

        when(personRepository.findByUsername("")).thenReturn(Optional.empty());

        // When & Then
        AttributeNotValidException exception = assertThrows(
                AttributeNotValidException.class,
                () -> createPersonService.execute(personWithBlankUsername)
        );

        assertEquals(PersonErrorMessages.NAME_REQUIRED.getMessage(), exception.getMessage());
        verify(personRepository).findByUsername("");
        verify(passwordEncoder, never()).encode(anyString());
        verify(personRepository, never()).save(any(Person.class));
    }

    @Test
    void shouldThrowExceptionWhenPasswordIsTooShort() {
        // Given
        Person personWithShortPassword = new Person();
        personWithShortPassword.setUsername("testUser");
        personWithShortPassword.setPassword("short");
        personWithShortPassword.setRole("ADMIN");

        when(personRepository.findByUsername("testUser")).thenReturn(Optional.empty());

        // When & Then
        AttributeNotValidException exception = assertThrows(
                AttributeNotValidException.class,
                () -> createPersonService.execute(personWithShortPassword)
        );

        assertEquals(PersonErrorMessages.PASSWORD_NOT_VALID.getMessage(), exception.getMessage());
        verify(personRepository).findByUsername("testUser");
        verify(passwordEncoder, never()).encode(anyString());
        verify(personRepository, never()).save(any(Person.class));
    }

    @Test
    void shouldThrowExceptionWhenRoleIsInvalid() {
        // Given
        Person personWithInvalidRole = new Person();
        personWithInvalidRole.setUsername("testUser");
        personWithInvalidRole.setPassword("password123");
        personWithInvalidRole.setRole("USER"); // Not in PersonRoles enum

        when(personRepository.findByUsername("testUser")).thenReturn(Optional.empty());

        // When & Then
        AttributeNotValidException exception = assertThrows(
                AttributeNotValidException.class,
                () -> createPersonService.execute(personWithInvalidRole)
        );

        assertEquals(PersonErrorMessages.ROLE_NOT_VALID.getMessage(), exception.getMessage());
        verify(personRepository).findByUsername("testUser");
        verify(passwordEncoder, never()).encode(anyString());
        verify(personRepository, never()).save(any(Person.class));
    }
}