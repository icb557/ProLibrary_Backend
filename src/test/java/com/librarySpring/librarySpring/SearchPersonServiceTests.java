package com.librarySpring.librarySpring;

import com.librarySpring.librarySpring.Entities.Person.interfaces.PersonRepository;
import com.librarySpring.librarySpring.Entities.Person.model.Person;
import com.librarySpring.librarySpring.Entities.Person.model.PersonDTO;
import com.librarySpring.librarySpring.Entities.Person.services.SearchPersonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SearchPersonServiceTests {

    @Mock
    private PersonRepository personRepository;

    @InjectMocks
    private SearchPersonService searchPersonService;

    private Person testPerson1;
    private Person testPerson2;

    @BeforeEach
    void setUp() {
        testPerson1 = new Person();
        testPerson1.setId(1);
        testPerson1.setUsername("adminUser");
        testPerson1.setPassword("password1");
        testPerson1.setRole("ADMIN"); // Valid role from PersonRoles enum

        testPerson2 = new Person();
        testPerson2.setId(2);
        testPerson2.setUsername("userAdmin");
        testPerson2.setPassword("password2");
        testPerson2.setRole("EMPLOYEE"); // Valid role from PersonRoles enum
    }

    @Test
    void shouldReturnMatchingPeopleWhenSearchTermExists() {
        // Given
        String searchTerm = "admin";
        List<Person> people = Arrays.asList(testPerson1, testPerson2);
        when(personRepository.findByUsernameContainingIgnoreCase(searchTerm)).thenReturn(people);

        // When
        ResponseEntity<List<PersonDTO>> response = searchPersonService.execute(searchTerm);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<PersonDTO> personDTOs = response.getBody();
        assertNotNull(personDTOs);
        assertEquals(2, personDTOs.size());

        assertEquals("adminUser", personDTOs.get(0).getUsername());
        assertEquals("password1", personDTOs.get(0).getPassword());
        assertEquals("ADMIN", personDTOs.get(0).getRole());

        assertEquals("userAdmin", personDTOs.get(1).getUsername());
        assertEquals("password2", personDTOs.get(1).getPassword());
        assertEquals("EMPLOYEE", personDTOs.get(1).getRole());

        verify(personRepository).findByUsernameContainingIgnoreCase(searchTerm);
    }

    @Test
    void shouldReturnEmptyListWhenNoMatchesFound() {
        // Given
        String searchTerm = "nonexistent";
        when(personRepository.findByUsernameContainingIgnoreCase(searchTerm)).thenReturn(Collections.emptyList());

        // When
        ResponseEntity<List<PersonDTO>> response = searchPersonService.execute(searchTerm);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<PersonDTO> personDTOs = response.getBody();
        assertNotNull(personDTOs);
        assertTrue(personDTOs.isEmpty());

        verify(personRepository).findByUsernameContainingIgnoreCase(searchTerm);
    }
}