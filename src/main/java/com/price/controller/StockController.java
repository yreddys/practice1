package com.price.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.price.entity.StockResponse;
import com.price.service.StockService;

@RestController
@RequestMapping("/api/stocks")
public class StockController {

	private final StockService stockService;

	public StockController(StockService stockService) {
		this.stockService = stockService;
	}

	@GetMapping("/{symbol}")
	public ResponseEntity<?> getStockData(@PathVariable("symbol") String symbol) {
		try {
			StockResponse response = stockService.fetchStockData(symbol.toUpperCase());
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body("Something went wrong");
		}
	}
}
