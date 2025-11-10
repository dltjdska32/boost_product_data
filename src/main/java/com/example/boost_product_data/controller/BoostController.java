//package com.example.boost_product_data.controller;
//
//import com.example.boost_product_data.service.BoostDataService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequiredArgsConstructor
//public class BoostController {
//
//    private final BoostDataService boostDataService;
//
//    @PostMapping("/boost/data")
//    public ResponseEntity<String> boostData() {
//
//        boostDataService.createProduct();
//
//        return ResponseEntity.ok("Boost Data");
//    }
//}
