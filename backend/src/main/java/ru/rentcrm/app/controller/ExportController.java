package ru.rentcrm.app.controller;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.rentcrm.app.security.CurrentUser;
import ru.rentcrm.app.service.ExportService;

@RestController
@RequestMapping("/api/export")
public class ExportController {
    private final CurrentUser current;
    private final ExportService exportService;

    public ExportController(CurrentUser current, ExportService exportService) {
        this.current = current;
        this.exportService = exportService;
    }

    @GetMapping("/workbook")
    public ResponseEntity<byte[]> workbook() {
        byte[] bytes = exportService.accountWorkbook(current.accountId());
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                .filename("rentcrm-export.xlsx")
                .build()
                .toString())
            .body(bytes);
    }
}
