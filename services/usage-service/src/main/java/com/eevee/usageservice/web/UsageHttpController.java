package com.eevee.usageservice.web;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Gateway-routed Usage HTTP API ({@code /api/v1/usage/**}). Extend with dashboard/query endpoints as needed.
 */
@RestController
@RequestMapping("/api/v1/usage")
public class UsageHttpController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(Map.of("status", "ok"));
    }
}
