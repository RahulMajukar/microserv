package com.learn.report.api;

import com.learn.common.api.ApiResponse;
import com.learn.report.service.ManagementReportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ManagementReportController {

    private final ManagementReportService reportService;

    public ManagementReportController(ManagementReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/management")
    public ResponseEntity<ApiResponse<ManagementReportResponse>> generate(
            @Valid @RequestBody GenerateReportRequest request) {
        ManagementReportResponse report = reportService.generate(request.from(), request.to());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(report));
    }

    @GetMapping
    public ApiResponse<List<ManagementReportResponse>> list() {
        return ApiResponse.ok(reportService.list());
    }

    @GetMapping("/{id}")
    public ApiResponse<ManagementReportResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(reportService.get(id));
    }
}
