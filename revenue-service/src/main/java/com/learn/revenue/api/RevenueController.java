package com.learn.revenue.api;

import com.learn.common.api.ApiResponse;
import com.learn.common.api.RevenueSummary;
import com.learn.revenue.service.RevenueService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/revenues")
public class RevenueController {

    private final RevenueService revenueService;

    public RevenueController(RevenueService revenueService) {
        this.revenueService = revenueService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RevenueResponse>> create(@Valid @RequestBody CreateRevenueRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(revenueService.create(request)));
    }

    @GetMapping
    public ApiResponse<List<RevenueResponse>> list(
            @RequestParam(value = "from", required = false) LocalDate from,
            @RequestParam(value = "to", required = false) LocalDate to) {
        return ApiResponse.ok(revenueService.list(from, to));
    }

    @GetMapping("/summary")
    public ApiResponse<RevenueSummary> summary(
            @RequestParam("from") LocalDate from,
            @RequestParam("to") LocalDate to) {
        return ApiResponse.ok(revenueService.summarize(from, to));
    }

    @GetMapping("/{id}")
    public ApiResponse<RevenueResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(revenueService.get(id));
    }
}
