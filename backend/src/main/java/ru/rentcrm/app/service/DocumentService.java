package ru.rentcrm.app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.rentcrm.app.exception.ApiException;
import ru.rentcrm.app.model.AppUser;
import ru.rentcrm.app.model.DocumentItem;
import ru.rentcrm.app.model.DocumentType;
import ru.rentcrm.app.repository.DocumentRepository;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class DocumentService {
    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);
    private static final long MAX_FILE_SIZE = 30L * 1024L * 1024L;
    private static final Map<String, Set<String>> ALLOWED_TYPES = Map.ofEntries(
        Map.entry("pdf", Set.of("application/pdf")),
        Map.entry("doc", Set.of("application/msword", "application/octet-stream")),
        Map.entry("docx", Set.of("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/zip", "application/octet-stream")),
        Map.entry("xls", Set.of("application/vnd.ms-excel", "application/octet-stream")),
        Map.entry("xlsx", Set.of("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/zip", "application/octet-stream")),
        Map.entry("ppt", Set.of("application/vnd.ms-powerpoint", "application/octet-stream")),
        Map.entry("pptx", Set.of("application/vnd.openxmlformats-officedocument.presentationml.presentation", "application/zip", "application/octet-stream")),
        Map.entry("odt", Set.of("application/vnd.oasis.opendocument.text", "application/zip", "application/octet-stream")),
        Map.entry("ods", Set.of("application/vnd.oasis.opendocument.spreadsheet", "application/zip", "application/octet-stream")),
        Map.entry("odp", Set.of("application/vnd.oasis.opendocument.presentation", "application/zip", "application/octet-stream")),
        Map.entry("rtf", Set.of("application/rtf", "text/rtf", "application/octet-stream")),
        Map.entry("txt", Set.of("text/plain", "application/octet-stream")),
        Map.entry("csv", Set.of("text/csv", "application/vnd.ms-excel", "text/plain", "application/octet-stream")),
        Map.entry("jpg", Set.of("image/jpeg")),
        Map.entry("jpeg", Set.of("image/jpeg")),
        Map.entry("png", Set.of("image/png")),
        Map.entry("heic", Set.of("image/heic", "image/heif", "application/octet-stream"))
    );

    private final DocumentRepository documents;
    private final OwnershipService ownership;
    private final AuditService audit;
    private final Path storageRoot;

    public DocumentService(
        DocumentRepository documents,
        OwnershipService ownership,
        AuditService audit,
        @Value("${app.documents.storage-dir:/app/uploads/documents}") String storageDir
    ) {
        this.documents = documents;
        this.ownership = ownership;
        this.audit = audit;
        this.storageRoot = Path.of(storageDir).toAbsolutePath().normalize();
    }

    public List<DocumentItem> list(UUID accountId) {
        log.debug("Listing documents started accountId={}", accountId);
        List<DocumentItem> result = documents.findByAccountIdOrderByCreatedAtDesc(accountId);
        log.debug("Listing documents finished accountId={} count={}", accountId, result.size());
        return result;
    }

    public DocumentItem create(AppUser actor, DocumentItem input) {
        log.debug("Creating document started accountId={}", actor.accountId);
        input.id = UUID.randomUUID();
        input.accountId = actor.accountId;
        input.createdAt = Instant.now();
        if (input.fileUrl == null || input.fileUrl.isBlank()) {
            input.fileUrl = "#";
        }
        DocumentItem saved = documents.save(input);
        audit.record(actor, "CREATE", "DOCUMENT", saved.id, saved.name);
        log.debug("Creating document finished accountId={} documentId={}", actor.accountId, saved.id);
        return saved;
    }

    public DocumentItem upload(AppUser actor, UUID objectId, UUID tenantId, UUID contractId, String name, DocumentType type, LocalDate expiresAt, MultipartFile file) {
        log.debug("Uploading document started accountId={} fileName={} size={}", actor.accountId, file.getOriginalFilename(), file.getSize());
        validateUpload(file);
        DocumentItem item = new DocumentItem();
        item.id = UUID.randomUUID();
        item.accountId = actor.accountId;
        item.objectId = objectId;
        item.tenantId = tenantId;
        item.contractId = contractId;
        item.name = name == null || name.isBlank() ? safeOriginalName(file) : name.trim();
        item.type = type == null ? DocumentType.OTHER : type;
        item.expiresAt = expiresAt;
        item.createdAt = Instant.now();
        item.originalFileName = safeOriginalName(file);
        item.contentType = normalizeContentType(file.getContentType());
        item.sizeBytes = file.getSize();
        item.storageKey = actor.accountId + "/" + item.id + "-" + UUID.randomUUID() + "." + extension(item.originalFileName);
        item.fileUrl = "/api/documents/" + item.id + "/file";
        storeFile(item.storageKey, file);
        DocumentItem saved = documents.save(item);
        audit.record(actor, "CREATE", "DOCUMENT", saved.id, saved.name);
        log.debug("Uploading document finished accountId={} documentId={} storageKey={}", actor.accountId, saved.id, saved.storageKey);
        return saved;
    }

    public DocumentItem update(AppUser actor, UUID id, DocumentItem input) {
        log.debug("Updating document started accountId={} documentId={}", actor.accountId, id);
        DocumentItem item = ownership.requireOwnedDocument(documents.findById(id), actor.accountId);
        input.id = item.id;
        input.accountId = item.accountId;
        input.createdAt = item.createdAt;
        input.storageKey = item.storageKey;
        input.originalFileName = item.originalFileName;
        input.contentType = item.contentType;
        input.sizeBytes = item.sizeBytes;
        input.scanStatus = item.scanStatus;
        if ((input.fileUrl == null || input.fileUrl.isBlank()) && item.storageKey != null) {
            input.fileUrl = item.fileUrl;
        }
        DocumentItem saved = documents.save(input);
        audit.record(actor, "UPDATE", "DOCUMENT", saved.id, saved.name);
        log.debug("Updating document finished accountId={} documentId={}", actor.accountId, saved.id);
        return saved;
    }

    public StoredDocument download(AppUser actor, UUID id) {
        log.debug("Downloading document started accountId={} documentId={}", actor.accountId, id);
        DocumentItem item = ownership.requireOwnedDocument(documents.findById(id), actor.accountId);
        if (item.storageKey == null || item.storageKey.isBlank()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "У документа нет сохраненного файла");
        }
        Path path = resolveStorageKey(item.storageKey);
        if (!Files.exists(path)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Файл документа не найден в хранилище");
        }
        try {
            Resource resource = new UrlResource(path.toUri());
            log.debug("Downloading document finished accountId={} documentId={}", actor.accountId, id);
            return new StoredDocument(item, resource);
        } catch (MalformedURLException ex) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Не удалось открыть файл документа");
        }
    }

    public void delete(AppUser actor, UUID id) {
        log.debug("Deleting document started accountId={} documentId={}", actor.accountId, id);
        DocumentItem item = ownership.requireOwnedDocument(documents.findById(id), actor.accountId);
        documents.delete(item);
        deleteStoredFile(item.storageKey);
        audit.record(actor, "DELETE", "DOCUMENT", id, item.name);
        log.debug("Deleting document finished accountId={} documentId={}", actor.accountId, id);
    }

    private void validateUpload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Выберите файл документа");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Файл больше 30 МБ");
        }
        String fileName = safeOriginalName(file);
        String ext = extension(fileName);
        if (!ALLOWED_TYPES.containsKey(ext)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Недопустимый формат файла");
        }
        String contentType = normalizeContentType(file.getContentType());
        if (!contentType.isBlank() && !ALLOWED_TYPES.get(ext).contains(contentType)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Тип файла не соответствует расширению");
        }
    }

    private void storeFile(String storageKey, MultipartFile file) {
        Path target = resolveStorageKey(storageKey);
        try {
            Files.createDirectories(target.getParent());
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Не удалось сохранить файл документа");
        }
    }

    private void deleteStoredFile(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) return;
        try {
            Files.deleteIfExists(resolveStorageKey(storageKey));
        } catch (IOException ex) {
            log.warn("Failed to delete stored document file storageKey={}", storageKey, ex);
        }
    }

    private Path resolveStorageKey(String storageKey) {
        Path path = storageRoot.resolve(storageKey).normalize();
        if (!path.startsWith(storageRoot)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Некорректный ключ файла");
        }
        return path;
    }

    private String safeOriginalName(MultipartFile file) {
        String original = file.getOriginalFilename();
        if (original == null || original.isBlank()) return "document";
        return Path.of(original).getFileName().toString();
    }

    private String extension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "У файла должно быть расширение");
        }
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private String normalizeContentType(String contentType) {
        return contentType == null ? "" : contentType.toLowerCase(Locale.ROOT).split(";")[0].trim();
    }

    public record StoredDocument(DocumentItem item, Resource resource) {}
}
