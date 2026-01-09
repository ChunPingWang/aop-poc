package com.example.contact.unit.application;

import com.example.contact.application.port.in.CreateContactCommand;
import com.example.contact.application.port.out.ContactRepository;
import com.example.contact.application.service.ContactService;
import com.example.contact.domain.model.Contact;
import com.example.contact.domain.model.ContactId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContactService Tests")
class ContactServiceTest {

    @Mock
    private ContactRepository contactRepository;

    private ContactService contactService;

    @BeforeEach
    void setUp() {
        contactService = new ContactService(contactRepository);
    }

    @Nested
    @DisplayName("createContact()")
    class CreateContactTests {

        @Test
        @DisplayName("should create and save contact successfully")
        void shouldCreateAndSaveContact() {
            // Given
            CreateContactCommand command = new CreateContactCommand("張三", "0912345678", "台北市中正區");
            Contact savedContact = Contact.create("張三", "0912345678", "台北市中正區")
                    .withId(new ContactId(1L));

            when(contactRepository.save(any(Contact.class))).thenReturn(savedContact);

            // When
            Contact result = contactService.createContact(command);

            // Then
            assertThat(result.getId()).isNotNull();
            assertThat(result.getId().value()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("張三");
            assertThat(result.getPhone()).isEqualTo("0912345678");
            assertThat(result.getAddress()).isEqualTo("台北市中正區");

            verify(contactRepository, times(1)).save(any(Contact.class));
        }

        @Test
        @DisplayName("should create contact without address")
        void shouldCreateContactWithoutAddress() {
            // Given
            CreateContactCommand command = new CreateContactCommand("李四", "0987654321", null);
            Contact savedContact = Contact.create("李四", "0987654321", null)
                    .withId(new ContactId(2L));

            when(contactRepository.save(any(Contact.class))).thenReturn(savedContact);

            // When
            Contact result = contactService.createContact(command);

            // Then
            assertThat(result.getName()).isEqualTo("李四");
            assertThat(result.getAddress()).isNull();
        }
    }
}
