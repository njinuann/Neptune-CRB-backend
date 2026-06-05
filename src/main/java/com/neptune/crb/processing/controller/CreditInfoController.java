//package com.neptune.crb.processing.controller;
//
//import com.neptune.crb.processing.model.creditInfo.CRBCreditInfoResponse;
//import com.neptune.crb.processing.model.creditInfo.CompanyCreditRequest;
//import com.neptune.crb.processing.model.creditInfo.IndividualCreditRequest;
//import com.neptune.crb.processing.model.creditInfo.IprsBeginRequest;
//import com.neptune.crb.processing.service.creditInfo.CreditInfoService;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/credit-info")
//@RequiredArgsConstructor
//@Slf4j
//public class CreditInfoController {
//
//    private final CreditInfoService service;
//
//    @PostMapping("/individual")
//    public ResponseEntity<CRBCreditInfoResponse<?>> individual(
//            @Valid @RequestBody IndividualCreditRequest request,
//            HttpServletRequest httpServletRequest) {
//
//        try {
//            log.info("Individual request received from IP: {}", httpServletRequest.getRemoteAddr());
//
//            var response = service.beginIndividualCreditReport(request);
//
//            return ResponseEntity.ok(
//                    new CRBCreditInfoResponse<>(
//                            "00",
//                            "Success",
//                            response
//                    )
//            );
//
//        } catch (Exception e) {
//            log.error("Individual credit error", e);
//
//            return ResponseEntity.ok(
//                    new CRBCreditInfoResponse<>(
//                            "CRB_099",
//                            "System error: " + e.getMessage(),
//                            null
//                    )
//            );
//        }
//    }
//
//    @PostMapping("/company")
//    public ResponseEntity<CRBCreditInfoResponse<?>> company(
//            @Valid @RequestBody CompanyCreditRequest request,
//            HttpServletRequest httpServletRequest) {
//
//        try {
//            var response = service.beginCompanyCreditReport(request);
//
//            return ResponseEntity.ok(
//                    new CRBCreditInfoResponse<>(
//                            "00",
//                            "Success",
//                            response
//                    )
//            );
//
//        } catch (Exception e) {
//            log.error("Company credit error", e);
//
//            return ResponseEntity.ok(
//                    new CRBCreditInfoResponse<>(
//                            "CRB_099",
//                            e.getMessage(),
//                            null
//                    )
//            );
//        }
//    }
//
//    @PostMapping("/iprs/begin")
//    public ResponseEntity<CRBCreditInfoResponse<?>> beginIPRS(
//            @Valid @RequestBody IprsBeginRequest request) {
//
//        try {
//            var response = service.beginIprsQuery(request);
//
//            return ResponseEntity.ok(
//                    new CRBCreditInfoResponse<>(
//                            "00",
//                            "Success",
//                            response
//                    )
//            );
//
//        } catch (Exception e) {
//            return ResponseEntity.ok(
//                    new CRBCreditInfoResponse<>(
//                            "CRB_099",
//                            e.getMessage(),
//                            null
//                    )
//            );
//        }
//    }
//
//    @GetMapping("/iprs/end")
//    public ResponseEntity<CRBCreditInfoResponse<?>> endIprs(@RequestParam String token) {
//
//        var response = service.endQuery(token);
//
//        return ResponseEntity.ok(
//                new CRBCreditInfoResponse<>(
//                        "00",
//                        "Success",
//                        response
//                )
//        );
//    }
//
//    @GetMapping("/iprs/end/pdf")
//    public ResponseEntity<byte[]> endIprsPdf(@RequestParam String token) {
//
//        byte[] pdf = service.endQueryPdf(token);
//
//        return ResponseEntity.ok()
//                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=iprs.pdf")
//                .contentType(MediaType.APPLICATION_PDF)
//                .body(pdf);
//    }
//}