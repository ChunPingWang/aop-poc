package com.example.contact.integration;

import com.example.contact.CucumberSpringConfiguration;
import com.example.contact.infrastructure.adapter.in.web.dto.ContactResponse;
import com.example.contact.infrastructure.adapter.in.web.dto.CreateContactRequest;
import com.example.contact.infrastructure.adapter.in.web.dto.UpdateContactRequest;
import com.example.contact.infrastructure.adapter.in.web.dto.ErrorResponse;
import com.example.contact.infrastructure.adapter.out.persistence.ContactJpaRepository;
import com.example.contact.infrastructure.adapter.out.persistence.entity.ContactJpaEntity;
import io.cucumber.java.Before;
import io.cucumber.java.zh_tw.假設;
import io.cucumber.java.zh_tw.那麼;
import io.cucumber.java.zh_tw.當;
import io.cucumber.java.zh_tw.而且;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ContactStepDefinitions extends CucumberSpringConfiguration {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ContactJpaRepository contactRepository;

    private ResponseEntity<ContactResponse> contactResponse;
    private ResponseEntity<ErrorResponse> errorResponse;
    private ResponseEntity<List<ContactResponse>> contactListResponse;
    private Long savedContactId;

    @Before
    public void setup() {
        contactRepository.deleteAll();
    }

    // === 假設 (Given) ===

    @假設("系統正常運作且無任何聯絡人")
    public void systemIsRunningWithNoContacts() {
        contactRepository.deleteAll();
        assertThat(contactRepository.count()).isZero();
    }

    @假設("系統正常運作")
    public void systemIsRunning() {
        // System is already running via SpringBootTest
    }

    // === 當 (When) ===

    @當("使用者提供姓名「{word}」、電話「{word}」、地址「{word}」發送新增請求")
    public void userCreatesContactWithFullInfo(String name, String phone, String address) {
        CreateContactRequest request = new CreateContactRequest(name, phone, address);
        contactResponse = restTemplate.postForEntity(
            getBaseUrl() + "/api/contacts",
            request,
            ContactResponse.class
        );
    }

    @當("使用者提供姓名「{word}」、電話「{word}」但不提供地址發送新增請求")
    public void userCreatesContactWithoutAddress(String name, String phone) {
        CreateContactRequest request = new CreateContactRequest(name, phone, null);
        contactResponse = restTemplate.postForEntity(
            getBaseUrl() + "/api/contacts",
            request,
            ContactResponse.class
        );
    }

    @當("使用者提供空白姓名發送新增請求")
    public void userCreatesContactWithBlankName() {
        CreateContactRequest request = new CreateContactRequest("", "0912345678", null);
        errorResponse = restTemplate.postForEntity(
            getBaseUrl() + "/api/contacts",
            request,
            ErrorResponse.class
        );
    }

    @當("使用者未提供電話發送新增請求")
    public void userCreatesContactWithoutPhone() {
        CreateContactRequest request = new CreateContactRequest("張三", "", null);
        errorResponse = restTemplate.postForEntity(
            getBaseUrl() + "/api/contacts",
            request,
            ErrorResponse.class
        );
    }

    // === 那麼 (Then) ===

    @那麼("系統回傳包含系統產生 ID 的完整聯絡人資訊")
    public void systemReturnsContactWithId() {
        assertThat(contactResponse.getBody()).isNotNull();
        assertThat(contactResponse.getBody().id()).isNotNull();
        assertThat(contactResponse.getBody().id()).isGreaterThan(0);
    }

    @那麼("系統回傳包含系統產生 ID 的聯絡人資訊")
    public void systemReturnsContactInfo() {
        assertThat(contactResponse.getBody()).isNotNull();
        assertThat(contactResponse.getBody().id()).isNotNull();
    }

    @那麼("系統回傳驗證錯誤訊息")
    public void systemReturnsValidationError() {
        assertThat(errorResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // === 而且 (And) ===

    @而且("回應狀態碼為 {int}")
    public void responseStatusCodeIs(int statusCode) {
        assertThat(contactResponse.getStatusCode().value()).isEqualTo(statusCode);
    }

    @而且("地址欄位為空")
    public void addressFieldIsNull() {
        assertThat(contactResponse.getBody().address()).isNull();
    }

    @而且("錯誤訊息說明姓名為必填欄位")
    public void errorMessageForName() {
        assertThat(errorResponse.getBody().message()).contains("姓名");
    }

    @而且("錯誤訊息說明電話為必填欄位")
    public void errorMessageForPhone() {
        assertThat(errorResponse.getBody().message()).contains("電話");
    }

    // ============ User Story 2: Query Contact ============

    @假設("系統中已存在聯絡人「{word}」")
    public void contactExists(String name) {
        ContactJpaEntity entity = new ContactJpaEntity(
            null, name, "0912345678", "台北市",
            LocalDateTime.now(), LocalDateTime.now()
        );
        ContactJpaEntity saved = contactRepository.save(entity);
        savedContactId = saved.getId();
    }

    @假設("系統中已存在多筆聯絡人記錄")
    public void multipleContactsExist() {
        contactRepository.save(new ContactJpaEntity(
            null, "張三", "0912345678", "台北市",
            LocalDateTime.now(), LocalDateTime.now()
        ));
        contactRepository.save(new ContactJpaEntity(
            null, "李四", "0987654321", "新北市",
            LocalDateTime.now(), LocalDateTime.now()
        ));
    }

    @假設("系統中無任何聯絡人記錄")
    public void noContactsExist() {
        contactRepository.deleteAll();
        assertThat(contactRepository.count()).isZero();
    }

    @假設("系統中不存在 ID 為 {int} 的聯絡人")
    public void contactNotExist(int id) {
        contactRepository.deleteById((long) id);
        assertThat(contactRepository.existsById((long) id)).isFalse();
    }

    @當("使用者以該聯絡人 ID 發送查詢請求")
    public void queryContactById() {
        contactResponse = restTemplate.getForEntity(
            getBaseUrl() + "/api/contacts/" + savedContactId,
            ContactResponse.class
        );
    }

    @當("使用者發送查詢所有聯絡人請求")
    public void queryAllContacts() {
        contactListResponse = restTemplate.exchange(
            getBaseUrl() + "/api/contacts",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<ContactResponse>>() {}
        );
    }

    @當("使用者以 ID 為 {int} 發送查詢請求")
    public void queryContactBySpecificId(int id) {
        errorResponse = restTemplate.getForEntity(
            getBaseUrl() + "/api/contacts/" + id,
            ErrorResponse.class
        );
    }

    @那麼("系統回傳該聯絡人的完整資訊")
    public void returnContactInfo() {
        assertThat(contactResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(contactResponse.getBody()).isNotNull();
        assertThat(contactResponse.getBody().id()).isEqualTo(savedContactId);
    }

    @那麼("系統回傳所有聯絡人的列表")
    public void returnAllContacts() {
        assertThat(contactListResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(contactListResponse.getBody()).isNotNull();
        assertThat(contactListResponse.getBody().size()).isGreaterThanOrEqualTo(2);
    }

    @那麼("系統回傳空列表")
    public void returnEmptyList() {
        assertThat(contactListResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(contactListResponse.getBody()).isNotNull();
        assertThat(contactListResponse.getBody()).isEmpty();
    }

    @那麼("系統回傳查無資料的適當訊息")
    public void returnNotFoundMessage() {
        assertThat(errorResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(errorResponse.getBody().message()).contains("不存在");
    }

    // ============ User Story 3: Update Contact ============

    @當("使用者修改該聯絡人姓名為「{word}」、電話為「{word}」、地址為「{word}」")
    public void updateContactFullInfo(String name, String phone, String address) {
        UpdateContactRequest request = new UpdateContactRequest(name, phone, address);
        contactResponse = restTemplate.exchange(
            getBaseUrl() + "/api/contacts/" + savedContactId,
            HttpMethod.PUT,
            new HttpEntity<>(request),
            ContactResponse.class
        );
    }

    @當("使用者僅修改該聯絡人電話為「{word}」")
    public void updateContactPhone(String phone) {
        // Get current contact info first
        ContactJpaEntity existing = contactRepository.findById(savedContactId).orElseThrow();
        UpdateContactRequest request = new UpdateContactRequest(existing.getName(), phone, existing.getAddress());
        contactResponse = restTemplate.exchange(
            getBaseUrl() + "/api/contacts/" + savedContactId,
            HttpMethod.PUT,
            new HttpEntity<>(request),
            ContactResponse.class
        );
    }

    @當("使用者嘗試修改 ID 為 {int} 的不存在聯絡人")
    public void updateNonExistentContact(int id) {
        UpdateContactRequest request = new UpdateContactRequest("測試", "0912345678", "台北市");
        errorResponse = restTemplate.exchange(
            getBaseUrl() + "/api/contacts/" + id,
            HttpMethod.PUT,
            new HttpEntity<>(request),
            ErrorResponse.class
        );
    }

    @當("使用者將該聯絡人姓名修改為空白")
    public void updateContactWithBlankName() {
        UpdateContactRequest request = new UpdateContactRequest("", "0912345678", "台北市");
        errorResponse = restTemplate.exchange(
            getBaseUrl() + "/api/contacts/" + savedContactId,
            HttpMethod.PUT,
            new HttpEntity<>(request),
            ErrorResponse.class
        );
    }

    @當("使用者將該聯絡人電話修改為空白")
    public void updateContactWithBlankPhone() {
        UpdateContactRequest request = new UpdateContactRequest("王小明", "", "台北市");
        errorResponse = restTemplate.exchange(
            getBaseUrl() + "/api/contacts/" + savedContactId,
            HttpMethod.PUT,
            new HttpEntity<>(request),
            ErrorResponse.class
        );
    }

    @那麼("系統回傳更新後的聯絡人資訊")
    public void returnUpdatedContactInfo() {
        assertThat(contactResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(contactResponse.getBody()).isNotNull();
    }

    @而且("聯絡人姓名為「{word}」")
    public void verifyContactName(String name) {
        assertThat(contactResponse.getBody().name()).isEqualTo(name);
    }

    @而且("聯絡人電話為「{word}」")
    public void verifyContactPhone(String phone) {
        assertThat(contactResponse.getBody().phone()).isEqualTo(phone);
    }

    @而且("聯絡人地址為「{word}」")
    public void verifyContactAddress(String address) {
        assertThat(contactResponse.getBody().address()).isEqualTo(address);
    }

    // ============ User Story 4: Delete Contact ============

    private ResponseEntity<Void> deleteResponse;

    @當("使用者以該聯絡人 ID 發送刪除請求")
    public void deleteContactById() {
        deleteResponse = restTemplate.exchange(
            getBaseUrl() + "/api/contacts/" + savedContactId,
            HttpMethod.DELETE,
            null,
            Void.class
        );
    }

    @當("使用者以 ID 為 {int} 發送刪除請求")
    public void deleteContactBySpecificId(int id) {
        errorResponse = restTemplate.exchange(
            getBaseUrl() + "/api/contacts/" + id,
            HttpMethod.DELETE,
            null,
            ErrorResponse.class
        );
    }

    @那麼("系統回傳刪除成功訊息")
    public void returnDeleteSuccessMessage() {
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @而且("該聯絡人已從系統中移除")
    public void verifyContactDeleted() {
        assertThat(contactRepository.existsById(savedContactId)).isFalse();
    }
}
