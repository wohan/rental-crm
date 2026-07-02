package ru.rentcrm.app.controller;

import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.rentcrm.app.model.DocumentItem;
import ru.rentcrm.app.model.DocumentType;
import ru.rentcrm.app.security.CurrentUser;
import ru.rentcrm.app.service.DocumentService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {
    private final CurrentUser current;
    private final DocumentService service;

    public DocumentController(CurrentUser current, DocumentService service) {
        this.current = current;
        this.service = service;
    }

    @GetMapping
    public List<DocumentItem> list() {
        return service.list(current.accountId());
    }

    @PostMapping
    public DocumentItem create(@RequestBody DocumentItem item) {
        return service.create(current.get(), item);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DocumentItem upload(
        @RequestParam(required = false) UUID objectId,
        @RequestParam(required = false) UUID tenantId,
        @RequestParam(required = false) UUID contractId,
        @RequestParam String name,
        @RequestParam DocumentType type,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiresAt,
        @RequestPart("file") MultipartFile file
    ) {
        return service.upload(current.get(), objectId, tenantId, contractId, name, type, expiresAt, file);
    }

    @PutMapping("/{id}")
    public DocumentItem update(@PathVariable UUID id, @RequestBody DocumentItem item) {
        return service.update(current.get(), id, item);
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<Resource> download(@PathVariable UUID id) {
        DocumentService.StoredDocument stored = service.download(current.get(), id);
        String fileName = stored.item().originalFileName == null ? stored.item().name : stored.item().originalFileName;
        String contentType = stored.item().contentType == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : stored.item().contentType;
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(fileName).build().toString())
            .body(stored.resource());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        service.delete(current.get(), id);
    }
}
