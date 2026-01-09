package com.example.contact.infrastructure.adapter.in.web;

import com.example.contact.application.port.in.CreateContactCommand;
import com.example.contact.application.port.in.CreateContactUseCase;
import com.example.contact.application.port.in.DeleteContactUseCase;
import com.example.contact.application.port.in.GetContactUseCase;
import com.example.contact.application.port.in.UpdateContactCommand;
import com.example.contact.application.port.in.UpdateContactUseCase;
import com.example.contact.domain.model.Contact;
import com.example.contact.domain.model.ContactId;
import com.example.contact.infrastructure.adapter.in.web.dto.ContactResponse;
import com.example.contact.infrastructure.adapter.in.web.dto.CreateContactRequest;
import com.example.contact.infrastructure.adapter.in.web.dto.ErrorResponse;
import com.example.contact.infrastructure.adapter.in.web.dto.UpdateContactRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/contacts")
@Tag(name = "聯絡人管理", description = "聯絡人 CRUD 操作 API")
public class ContactController {

    private final CreateContactUseCase createContactUseCase;
    private final GetContactUseCase getContactUseCase;
    private final UpdateContactUseCase updateContactUseCase;
    private final DeleteContactUseCase deleteContactUseCase;

    public ContactController(CreateContactUseCase createContactUseCase,
                           GetContactUseCase getContactUseCase,
                           UpdateContactUseCase updateContactUseCase,
                           DeleteContactUseCase deleteContactUseCase) {
        this.createContactUseCase = createContactUseCase;
        this.getContactUseCase = getContactUseCase;
        this.updateContactUseCase = updateContactUseCase;
        this.deleteContactUseCase = deleteContactUseCase;
    }

    @Operation(
        summary = "新增聯絡人",
        description = "建立新的聯絡人記錄，包含姓名、電話和選填的地址"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "聯絡人建立成功",
            content = @Content(schema = @Schema(implementation = ContactResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "請求參數驗證失敗",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @PostMapping
    public ResponseEntity<ContactResponse> createContact(
            @Valid @RequestBody CreateContactRequest request) {
        CreateContactCommand command = new CreateContactCommand(
            request.name(),
            request.phone(),
            request.address()
        );
        Contact contact = createContactUseCase.createContact(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(ContactResponse.from(contact));
    }

    @Operation(
        summary = "查詢所有聯絡人",
        description = "取得系統中所有聯絡人的列表"
    )
    @ApiResponse(
        responseCode = "200",
        description = "成功取得聯絡人列表"
    )
    @GetMapping
    public ResponseEntity<List<ContactResponse>> getAllContacts() {
        List<Contact> contacts = getContactUseCase.getAllContacts();
        List<ContactResponse> responses = contacts.stream()
                .map(ContactResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @Operation(
        summary = "查詢單一聯絡人",
        description = "根據 ID 查詢特定聯絡人的詳細資訊"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "成功取得聯絡人資訊",
            content = @Content(schema = @Schema(implementation = ContactResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "聯絡人不存在",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ContactResponse> getContactById(
            @Parameter(description = "聯絡人 ID", required = true, example = "1")
            @PathVariable Long id) {
        Contact contact = getContactUseCase.getContactById(new ContactId(id));
        return ResponseEntity.ok(ContactResponse.from(contact));
    }

    @Operation(
        summary = "修改聯絡人",
        description = "更新現有聯絡人的資訊"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "聯絡人更新成功",
            content = @Content(schema = @Schema(implementation = ContactResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "請求參數驗證失敗",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "聯絡人不存在",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @PutMapping("/{id}")
    public ResponseEntity<ContactResponse> updateContact(
            @Parameter(description = "聯絡人 ID", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody UpdateContactRequest request) {
        UpdateContactCommand command = new UpdateContactCommand(
            new ContactId(id),
            request.name(),
            request.phone(),
            request.address()
        );
        Contact contact = updateContactUseCase.updateContact(command);
        return ResponseEntity.ok(ContactResponse.from(contact));
    }

    @Operation(
        summary = "刪除聯絡人",
        description = "從系統中移除指定的聯絡人"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "聯絡人刪除成功"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "聯絡人不存在",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(
            @Parameter(description = "聯絡人 ID", required = true, example = "1")
            @PathVariable Long id) {
        deleteContactUseCase.deleteContact(new ContactId(id));
        return ResponseEntity.noContent().build();
    }
}
