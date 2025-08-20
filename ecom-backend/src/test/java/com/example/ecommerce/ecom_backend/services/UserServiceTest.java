package com.example.ecommerce.ecom_backend.services;

import com.example.ecommerce.ecom_backend.auth.dto.UserRegistrationRequest;
import com.example.ecommerce.ecom_backend.common.exception.DuplicateEmailException;
import com.example.ecommerce.ecom_backend.common.exception.ResourceNotFoundException;
import com.example.ecommerce.ecom_backend.user.dto.UserResponseDTO;
import com.example.ecommerce.ecom_backend.user.dto.UserUpdateDTO;
import com.example.ecommerce.ecom_backend.user.model.Role;
import com.example.ecommerce.ecom_backend.user.model.User;
import com.example.ecommerce.ecom_backend.user.repository.RoleRepository;
import com.example.ecommerce.ecom_backend.user.repository.UserRepository;
import com.example.ecommerce.ecom_backend.user.service.UserService;
import com.example.ecommerce.ecom_backend.user.service.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUser_shouldSaveUser_whenCalled() {
        UserRegistrationRequest registrationRequest = new UserRegistrationRequest().builder()
                .email("L2l4A@example.com")
                .password("password")
                .firstName("John")
                .lastName("Doe")
                .build();
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(new Role()));
        when(passwordEncoder.encode(registrationRequest.getPassword())).thenReturn("encodedPassword");

        //ACTION
        userService.registerUser(registrationRequest);

        //ASSERT
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertNotNull(savedUser);
        assertEquals("L2l4A@example.com", savedUser.getEmail());
        assertEquals("John", savedUser.getFirstName());
        assertEquals("Doe", savedUser.getLastName());
        assertEquals("encodedPassword", savedUser.getPassword());
        assertNotNull(savedUser.getPassword());


    }

    @Test
    void  registerUser_shouldThrowDuplicateEmailException_whenEmailAlreadyExists() {
        UserRegistrationRequest registrationRequest = new UserRegistrationRequest().builder()
                .email("L2l4A@example.com")
                .password("password")
                .firstName("John")
                .lastName("Doe")
                .build();
        when(userRepository.findByEmail(registrationRequest.getEmail())).thenReturn(Optional.of(new User()));

        assertThrows(DuplicateEmailException.class, () -> userService.registerUser(registrationRequest));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void  getuserById_shouldReturnUser_whenUserExists() {
        Long userId = 1L;
        User user = new User().builder()
                .id(userId)
                .email("L2l4A@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        //Act
        UserResponseDTO userResponseDTO = userService.getUserById(userId);

        //Assert
        assertNotNull(userResponseDTO);
        assertEquals(userId, userResponseDTO.getId());
        assertEquals("L2l4A@example.com", userResponseDTO.getEmail());
        assertEquals("John", userResponseDTO.getFirstName());
        assertEquals("Doe", userResponseDTO.getLastName());

    }
    @Test
    void  getuserById_shouldThrowResourceNotFoundException_whenUserDoesNotExist() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        //Act
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(userId));

        verifyNoMoreInteractions(userRepository);
    }
@ Test
    void updateUser_shouldUpdateUser_whenUserExists() {
      Long userId = 1L;
      UserUpdateDTO userUpdateDTO = new UserUpdateDTO().builder()
            .email("manimau@example.com")
            .firstName("John")
            .lastName("Doe")
            .build();

      User user = new User().builder()
            .id(userId)
            .email("L2l4A@example.com")
            .firstName("Original Name")
            .lastName("Original Last Name")
            .build();
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    userService.updateUser(userId, userUpdateDTO);

    verify(userRepository).save(userCaptor.capture());

    User updatedUser = userCaptor.getValue();

    assertNotNull(updatedUser);
    assertEquals(userId, updatedUser.getId());
    assertEquals("manimau@example.com", updatedUser.getEmail());
    assertEquals("John", updatedUser.getFirstName());
    assertEquals("Doe", updatedUser.getLastName());
    assertNotNull("Original Name", updatedUser.getFirstName());

   }
   @Test
    void updateUser_shouldThrowResourceNotFoundException_whenUserDoesNotExist() {
        Long userId = 1L;
        UserUpdateDTO userUpdateDTO = new UserUpdateDTO().builder()
                .email("manimau@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(userId, userUpdateDTO));

        verifyNoMoreInteractions(userRepository);
    }
}
