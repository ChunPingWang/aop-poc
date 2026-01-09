package com.example.contact.application.service;

import com.example.contact.application.port.in.CreateContactCommand;
import com.example.contact.application.port.in.CreateContactUseCase;
import com.example.contact.application.port.in.DeleteContactUseCase;
import com.example.contact.application.port.in.GetContactUseCase;
import com.example.contact.application.port.in.UpdateContactCommand;
import com.example.contact.application.port.in.UpdateContactUseCase;
import com.example.contact.application.port.out.ContactRepository;
import com.example.contact.application.port.out.DomainEventPublisher;
import com.example.contact.domain.event.ContactCreatedEvent;
import com.example.contact.domain.event.ContactDeletedEvent;
import com.example.contact.domain.event.ContactUpdatedEvent;
import com.example.contact.domain.exception.ContactNotFoundException;
import com.example.contact.domain.model.Contact;
import com.example.contact.domain.model.ContactId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Application service for Contact operations.
 *
 * <p>This service publishes domain events for each operation,
 * allowing other components (like audit logging) to react
 * without coupling.</p>
 */
@Service
@Transactional
public class ContactService implements CreateContactUseCase, GetContactUseCase,
                                       UpdateContactUseCase, DeleteContactUseCase {

    private final ContactRepository contactRepository;
    private final DomainEventPublisher eventPublisher;

    public ContactService(ContactRepository contactRepository,
                          DomainEventPublisher eventPublisher) {
        this.contactRepository = contactRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Contact createContact(CreateContactCommand command) {
        Contact contact = Contact.create(
            command.name(),
            command.phone(),
            command.address()
        );
        Contact savedContact = contactRepository.save(contact);

        // Publish domain event
        eventPublisher.publish(new ContactCreatedEvent(savedContact));

        return savedContact;
    }

    @Override
    @Transactional(readOnly = true)
    public Contact getContactById(ContactId id) {
        return contactRepository.findById(id)
                .orElseThrow(() -> new ContactNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Contact> getAllContacts() {
        return contactRepository.findAll();
    }

    @Override
    public Contact updateContact(UpdateContactCommand command) {
        Contact existing = contactRepository.findById(command.id())
                .orElseThrow(() -> new ContactNotFoundException(command.id()));

        // Capture before state
        Map<String, Object> beforeSnapshot = createSnapshot(existing);

        existing.updateInfo(command.name(), command.phone(), command.address());
        Contact updatedContact = contactRepository.save(existing);

        // Publish domain event with before/after state
        eventPublisher.publish(new ContactUpdatedEvent(updatedContact, beforeSnapshot));

        return updatedContact;
    }

    @Override
    public void deleteContact(ContactId id) {
        Contact existing = contactRepository.findById(id)
                .orElseThrow(() -> new ContactNotFoundException(id));

        // Capture state before deletion
        Map<String, Object> snapshot = createSnapshot(existing);
        Long contactId = existing.getId().value();

        contactRepository.deleteById(id);

        // Publish domain event
        eventPublisher.publish(new ContactDeletedEvent(contactId, snapshot));
    }

    private Map<String, Object> createSnapshot(Contact contact) {
        return Map.of(
            "id", contact.getId().value(),
            "name", contact.getName(),
            "phone", contact.getPhone(),
            "address", contact.getAddress() != null ? contact.getAddress() : ""
        );
    }
}
