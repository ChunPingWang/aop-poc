package com.example.contact.unit.domain;

import com.example.contact.domain.exception.ValidationException;
import com.example.contact.domain.model.Contact;
import com.example.contact.domain.model.ContactId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Contact Domain Entity Tests")
class ContactTest {

    @Nested
    @DisplayName("create() - Contact creation")
    class CreateTests {

        @Test
        @DisplayName("should create contact with valid data")
        void shouldCreateContactWithValidData() {
            // When
            Contact contact = Contact.create("張三", "0912345678", "台北市中正區");

            // Then
            assertThat(contact.getId()).isNull();
            assertThat(contact.getName()).isEqualTo("張三");
            assertThat(contact.getPhone()).isEqualTo("0912345678");
            assertThat(contact.getAddress()).isEqualTo("台北市中正區");
            assertThat(contact.getCreatedAt()).isNotNull();
            assertThat(contact.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("should create contact without address")
        void shouldCreateContactWithoutAddress() {
            // When
            Contact contact = Contact.create("李四", "0987654321", null);

            // Then
            assertThat(contact.getName()).isEqualTo("李四");
            assertThat(contact.getAddress()).isNull();
        }

        @Test
        @DisplayName("should trim whitespace from fields")
        void shouldTrimWhitespace() {
            // When
            Contact contact = Contact.create("  張三  ", "  0912345678  ", "  台北市  ");

            // Then
            assertThat(contact.getName()).isEqualTo("張三");
            assertThat(contact.getPhone()).isEqualTo("0912345678");
            assertThat(contact.getAddress()).isEqualTo("台北市");
        }

        @Test
        @DisplayName("should throw ValidationException when name is null")
        void shouldThrowWhenNameIsNull() {
            assertThatThrownBy(() -> Contact.create(null, "0912345678", null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("姓名為必填欄位");
        }

        @Test
        @DisplayName("should throw ValidationException when name is empty")
        void shouldThrowWhenNameIsEmpty() {
            assertThatThrownBy(() -> Contact.create("", "0912345678", null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("姓名為必填欄位");
        }

        @Test
        @DisplayName("should throw ValidationException when name is only whitespace")
        void shouldThrowWhenNameIsWhitespace() {
            assertThatThrownBy(() -> Contact.create("   ", "0912345678", null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("姓名為必填欄位");
        }

        @Test
        @DisplayName("should throw ValidationException when name exceeds 50 characters")
        void shouldThrowWhenNameTooLong() {
            String longName = "a".repeat(51);
            assertThatThrownBy(() -> Contact.create(longName, "0912345678", null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("姓名長度不可超過 50 字元");
        }

        @Test
        @DisplayName("should throw ValidationException when phone is null")
        void shouldThrowWhenPhoneIsNull() {
            assertThatThrownBy(() -> Contact.create("張三", null, null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("電話為必填欄位");
        }

        @Test
        @DisplayName("should throw ValidationException when phone is empty")
        void shouldThrowWhenPhoneIsEmpty() {
            assertThatThrownBy(() -> Contact.create("張三", "", null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("電話為必填欄位");
        }

        @Test
        @DisplayName("should throw ValidationException when phone exceeds 20 characters")
        void shouldThrowWhenPhoneTooLong() {
            String longPhone = "0".repeat(21);
            assertThatThrownBy(() -> Contact.create("張三", longPhone, null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("電話長度不可超過 20 字元");
        }

        @Test
        @DisplayName("should throw ValidationException when address exceeds 200 characters")
        void shouldThrowWhenAddressTooLong() {
            String longAddress = "a".repeat(201);
            assertThatThrownBy(() -> Contact.create("張三", "0912345678", longAddress))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("地址長度不可超過 200 字元");
        }
    }

    @Nested
    @DisplayName("updateInfo() - Contact update")
    class UpdateTests {

        @Test
        @DisplayName("should update contact info")
        void shouldUpdateContactInfo() {
            // Given
            Contact contact = Contact.create("張三", "0912345678", "台北市");

            // When
            contact.updateInfo("張三豐", "0987654321", "新北市");

            // Then
            assertThat(contact.getName()).isEqualTo("張三豐");
            assertThat(contact.getPhone()).isEqualTo("0987654321");
            assertThat(contact.getAddress()).isEqualTo("新北市");
        }

        @Test
        @DisplayName("should update updatedAt timestamp")
        void shouldUpdateTimestamp() throws InterruptedException {
            // Given
            Contact contact = Contact.create("張三", "0912345678", null);
            var originalUpdatedAt = contact.getUpdatedAt();

            Thread.sleep(10);

            // When
            contact.updateInfo("李四", "0987654321", null);

            // Then
            assertThat(contact.getUpdatedAt()).isAfter(originalUpdatedAt);
        }
    }

    @Nested
    @DisplayName("withId() - Assign ID")
    class WithIdTests {

        @Test
        @DisplayName("should create new contact with assigned ID")
        void shouldAssignId() {
            // Given
            Contact contact = Contact.create("張三", "0912345678", "台北市");
            ContactId id = new ContactId(1L);

            // When
            Contact withId = contact.withId(id);

            // Then
            assertThat(withId.getId()).isEqualTo(id);
            assertThat(withId.getName()).isEqualTo("張三");
            assertThat(withId.getPhone()).isEqualTo("0912345678");
        }
    }
}
