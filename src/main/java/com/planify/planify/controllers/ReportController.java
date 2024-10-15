package com.planify.planify.controllers;

import com.planify.planify.repositories.TransactionRepository;
import com.planify.planify.services.ReportService;
import com.planify.planify.services.UserService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;

@Controller
@RequestMapping("v1/report")
public class ReportController {
    private final ReportService reportService;
    private final UserService userService;
    private final TransactionRepository transactionRepository;

    public ReportController(ReportService reportService, UserService userService, TransactionRepository transactionRepository) {
        this.reportService = reportService;
        this.userService = userService;
        this.transactionRepository = transactionRepository;
    }

    @GetMapping("/export-pdf")
    public ResponseEntity<ByteArrayResource> exportPdf(Principal principal) throws IOException {
        var user = userService.findByEmail(principal.getName()).orElseThrow();
        var resource = reportService.generateReport(transactionRepository.findByUserOrderByDate(user));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=document.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(resource.contentLength())
                .body(resource);
    }

    @GetMapping("/export-csv")
    public ResponseEntity<ByteArrayResource> exportCsv(Principal principal,
                                                       @RequestParam(required = false) LocalDate startDate,
                                                       @RequestParam(required = false) LocalDate endDate) {
        ByteArrayResource resource = reportService.exportCsv(principal, startDate, endDate);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=transactions.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(resource.contentLength())
                .body(resource);
    }
}
