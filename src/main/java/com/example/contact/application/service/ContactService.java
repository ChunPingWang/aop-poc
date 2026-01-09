package com.example.contact.application.service;

import com.example.contact.application.port.in.CreateContactCommand;
import com.example.contact.application.port.in.CreateContactUseCase;
import com.example.contact.application.port.in.GetContactUseCase;
import com.example.contact.application.port.in.DeleteContactUseCase;
import com.example.contact.application.port.in.UpdateContactCommand;
import com.example.contact.application.port.in.UpdateContactUseCase;
import com.example.contact.application.port.out.ContactRepository;
import com.example.contact.domain.exception.ContactNotFoundException;
import com.example.contact.domain.model.Contact;
import com.example.contact.domain.model.ContactId;
import com.example.contact.domain.model.OperationType;
import com.example.contact.infrastructure.audit.Auditable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ContactService implements CreateContactUseCase, GetContactUseCase, UpdateContactUseCase, DeleteContactUseCase {

    private final ContactRepository contactRepository;

    public ContactService(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    @Override
    @Auditable(operation = OperationType.CREATE)
    public Contact createContact(CreateContactCommand command) {
        Contact contact = Contact.create(
            command.name(),
            command.phone(),
            command.address()
        );
        return contactRepository.save(contact);
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
    @Auditable(operation = OperationType.UPDATE)
    public Contact updateContact(UpdateContactCommand command) {
        Contact existing = contactRepository.findById(command.id())
                .orElseThrow(() -> new ContactNotFoundException(command.id()));
        existing.updateInfo(command.name(), command.phone(), command.address());
        return contactRepository.save(existing);
    }

    @Override
    @Auditable(operation = OperationType.DELETE)
    public void deleteContact(ContactId id) {
        if (!contactRepository.existsById(id)) {
            throw new ContactNotFoundException(id);
        }
        contactRepository.deleteById(id);
    }
}
