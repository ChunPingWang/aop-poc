package com.example.contact.application.port.in;

/**
 * Command object for creating a new contact.
 */
public record CreateContactCommand(
    String name,
    String phone,
    String address
) {
}
