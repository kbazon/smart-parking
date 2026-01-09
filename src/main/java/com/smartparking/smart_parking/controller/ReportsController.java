package com.smartparking.smart_parking.controller;

import com.smartparking.smart_parking.model.Report;
import com.smartparking.smart_parking.service.ReportService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;

@RestController
@RequestMapping("/reports")
public class ReportsController {

    private final ReportService reportService;

    public ReportsController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/daily")
    public Report generateDaily(@RequestParam(required = false) String date) {
        LocalDate d = date != null ? LocalDate.parse(date) : LocalDate.now();
        return reportService.generateDailyReport(d);
    }

    @PostMapping("/monthly")
    public Report generateMonthly(@RequestParam(required = false) String month) {
        YearMonth ym = month != null ? YearMonth.parse(month) : YearMonth.now();
        return reportService.generateMonthlyReport(ym);
    }
}
