package com.librarySpring.librarySpring;

import com.librarySpring.librarySpring.Entities.Person.PersonErrorMessages;
import com.librarySpring.librarySpring.Entities.Person.interfaces.PersonRepository;
import com.librarySpring.librarySpring.Entities.Person.model.Person;
import com.librarySpring.librarySpring.Entities.Person.model.PersonDTO;
import com.librarySpring.librarySpring.Entities.Person.model.UpdatePersonCommand;
import com.librarySpring.librarySpring.Entities.Person.services.UpdatePersonService;
import com.librarySpring.librarySpring.Exceptions.AttributeNotValidException;
import com.librarySpring.librarySpring.Exceptions.ResourceAlreadyExistsException;
import com.librarySpring.librarySpring.Exceptions.ResourceNotFoundException;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UpdatePersonServiceTests {

    @Mock
    private PersonRepository personRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UpdatePersonService updatePersonService;

    private Person existingPerson;
    private Person updatedPerson;
    private UpdatePersonCommand updateCommand;

    @BeforeEach
    void setUp() {
        existingPerson = new Person();
        existingPerson.setId(1);
        existingPerson.setUsername("oldUsername");
        existingPerson.setPassword("oldPassword");
        existingPerson.setRole("ADMIN");

        updatedPerson = new Person();
        updatedPerson.setUsername("newUsername");
        updatedPerson.setPassword("newPassword123"); // Ensuring password meets length requirement
        updatedPerson.setRole("EMPLOYEE"); // Valid role from PersonRoles enum

        updateCommand = new UpdatePersonCommand("oldUsername", updatedPerson);
    }

    @Test
    void shouldUpdatePersonSuccessfully() {
        // Given
        when(personRepository.findByUsername("oldUsername")).thenReturn(Optional.of(existingPerson));
        when(personRepository.findByUsername("newUsername")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");
        doNothing().when(personRepository).updatePerson(anyInt(), anyString(), anyString(), anyString());

        // When
        ResponseEntity<PersonDTO> response = updatePersonService.execute(updateCommand);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        PersonDTO personDTO = response.getBody();
        assertNotNull(personDTO);
        assertEquals("newUsername", personDTO.getUsername());
        assertEquals("newPassword123", personDTO.getPassword());
        assertEquals("EMPLOYEE", personDTO.getRole());

        verify(personRepository).findByUsername("oldUsername");
        verify(personRepository).findByUsername("newUsername");
        verify(passwordEncoder).encode("newPassword123");
        verify(personRepository).updatePerson(1, "newUsername", "encodedNewPassword", "EMPLOYEE");
    }

    @Test
    void shouldThrowExceptionWhenPersonNotFound() {
        // Given
        when(personRepository.findByUsername("oldUsername")).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> updatePersonService.execute(updateCommand)
        );

        assertEquals(PersonErrorMessages.PERSON_NOT_FOUND.getMessage(), exception.getMessage());
        verify(personRepository).findByUsername("oldUsername");
        verify(personRepository, never()).findByUsername("newUsername");
        verify(passwordEncoder, never()).encode(anyString());
        verify(personRepository, never()).updatePerson(anyInt(), anyString(), anyString(), anyString());
    }

    @Test
    void shouldThrowExceptionWhenUpdatedUsernameAlreadyExists() {
        // Given
        when(personRepository.findByUsername("oldUsername")).thenReturn(Optional.of(existingPerson));
        when(personRepository.findByUsername("newUsername")).thenReturn(Optional.of(new Person()));

        // When & Then
        ResourceAlreadyExistsException exception = assertThrows(
                ResourceAlreadyExistsException.class,
                () -> updatePersonService.execute(updateCommand)
        );

        assertEquals(PersonErrorMessages.PERSON_ALREADY_EXISTS.getMessage(), exception.getMessage());
        verify(personRepository).findByUsername("oldUsername");
        verify(personRepository).findByUsername("newUsername");
        verify(passwordEncoder, never()).encode(anyString());
        verify(personRepository, never()).updatePerson(anyInt(), anyString(), anyString(), anyString());
    }

    @Test
    void shouldThrowExceptionWhenUpdatedUsernameIsBlank() {
        // Given
        Person personWithBlankUsername = new Person();
        personWithBlankUsername.setUsername("");
        personWithBlankUsername.setPassword("newPassword123");
        personWithBlankUsername.setRole("EMPLOYEE");

        UpdatePersonCommand commandWithBlankUsername = new UpdatePersonCommand("oldUsername", personWithBlankUsername);

        when(personRepository.findByUsername("oldUsername")).thenReturn(Optional.of(existingPerson));

        // When & Then
        AttributeNotValidException exception = assertThrows(
                AttributeNotValidException.class,
                () -> updatePersonService.execute(commandWithBlankUsername)
        );

        assertEquals(PersonErrorMessages.NAME_REQUIRED.getMessage(), exception.getMessage());
        verify(personRepository).findByUsername("oldUsername");
        verify(passwordEncoder, never()).encode(anyString());
        verify(personRepository, never()).updatePerson(anyInt(), anyString(), anyString(), anyString());
    }

    @Test
    void shouldThrowExceptionWhenUpdatedPasswordIsTooShort() {
        // Given
        Person personWithShortPassword = new Person();
        personWithShortPassword.setUsername("newUsername");
        personWithShortPassword.setPassword("short");
        personWithShortPassword.setRole("EMPLOYEE");

        UpdatePersonCommand commandWithShortPassword = new UpdatePersonCommand("oldUsername", personWithShortPassword);

        when(personRepository.findByUsername("oldUsername")).thenReturn(Optional.of(existingPerson));

        // When & Then
        AttributeNotValidException exception = assertThrows(
                AttributeNotValidException.class,
                () -> updatePersonService.execute(commandWithShortPassword)
        );

        assertEquals(PersonErrorMessages.PASSWORD_NOT_VALID.getMessage(), exception.getMessage());
        verify(personRepository).findByUsername("oldUsername");
        verify(passwordEncoder, never()).encode(anyString());
        verify(personRepository, never()).updatePerson(anyInt(), anyString(), anyString(), anyString());
    }

    @Test
    void shouldThrowExceptionWhenUpdatedRoleIsInvalid() {
        // Given
        Person personWithInvalidRole = new Person();
        personWithInvalidRole.setUsername("newUsername");
        personWithInvalidRole.setPassword("newPassword123");
        personWithInvalidRole.setRole("USER");

        UpdatePersonCommand commandWithInvalidRole = new UpdatePersonCommand("oldUsername", personWithInvalidRole);

        when(personRepository.findByUsername("oldUsername")).thenReturn(Optional.of(existingPerson));

        // When & Then
        AttributeNotValidException exception = assertThrows(
                AttributeNotValidException.class,
                () -> updatePersonService.execute(commandWithInvalidRole)
        );

        assertEquals(PersonErrorMessages.ROLE_NOT_VALID.getMessage(), exception.getMessage());
        verify(personRepository).findByUsername("oldUsername");
        verify(passwordEncoder, never()).encode(anyString());
        verify(personRepository, never()).updatePerson(anyInt(), anyString(), anyString(), anyString());
    }
}