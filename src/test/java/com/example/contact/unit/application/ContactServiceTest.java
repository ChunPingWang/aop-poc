package com.example.contact.unit.application;

import com.example.contact.application.port.in.CreateContactCommand;
import com.example.contact.application.port.out.ContactRepository;
import com.example.contact.application.port.out.DomainEventPublisher;
import com.example.contact.application.service.ContactService;
import com.example.contact.domain.event.ContactCreatedEvent;
import com.example.contact.domain.model.Contact;
import com.example.contact.domain.model.ContactId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContactService Tests")
class ContactServiceTest {

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private DomainEventPublisher eventPublisher;

    private ContactService contactService;

    @BeforeEach
    void setUp() {
        contactService = new ContactService(contactRepository, eventPublisher);
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

        @Test
        @DisplayName("should publish ContactCreatedEvent after creation")
        void shouldPublishContactCreatedEvent() {
            // Given
            CreateContactCommand command = new CreateContactCommand("王五", "0911222333", "新北市");
            Contact savedContact = Contact.create("王五", "0911222333", "新北市")
                    .withId(new ContactId(3L));

            when(contactRepository.save(any(Contact.class))).thenReturn(savedContact);

            // When
            contactService.createContact(command);

            // Then
            ArgumentCaptor<ContactCreatedEvent> eventCaptor = ArgumentCaptor.forClass(ContactCreatedEvent.class);
            verify(eventPublisher).publish(eventCaptor.capture());

            ContactCreatedEvent event = eventCaptor.getValue();
            assertThat(event.getContactId()).isEqualTo(3L);
            assertThat(event.getSnapshot()).containsEntry("name", "王五");
        }
    }
}
