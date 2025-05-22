package com.price.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.price.entity.DailyPrice;
import com.price.entity.StockResponse;

@Service
public class StockService {

	@Value("${alpha.vantage.api.key}")
	private String apiKey;

	@Value("${alpha.vantage.api.url}")
	private String apiUrl;

	private final RestTemplate restTemplate;
	private final ObjectMapper objectMapper;

	public StockService(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
		this.objectMapper = new ObjectMapper();
	}

	public StockResponse fetchStockData(String symbol) throws Exception {
		String url = apiUrl + "?function=TIME_SERIES_DAILY_ADJUSTED" + "&symbol=" + symbol + "&outputsize=full"
				+ "&apikey=" + apiKey;

		String response = restTemplate.getForObject(url, String.class);
		return parseStockData(response, symbol);
	}

	private StockResponse parseStockData(String json, String symbol) throws Exception {
		JsonNode root = objectMapper.readTree(json);

		if (root.has("Error Message")) {
			throw new IllegalArgumentException("Invalid stock symbol or API call limit reached.");
		}

		JsonNode timeSeries = root.get("Time Series (Daily)");
		if (timeSeries == null) {
			throw new IllegalArgumentException("No Time Series data found for symbol: " + symbol);
		}

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDate threeYearsAgo = LocalDate.now().minusYears(3);

		List<DailyPrice> prices = new ArrayList<>();

		Iterator<String> dates = timeSeries.fieldNames();
		while (dates.hasNext()) {
			String dateStr = dates.next();
			LocalDate date = LocalDate.parse(dateStr, formatter);

			if (date.isBefore(threeYearsAgo)) {
				// Ignore older than 3 years
				continue;
			}

			JsonNode dailyData = timeSeries.get(dateStr);

			double open = dailyData.get("1. open").asDouble();
			double high = dailyData.get("2. high").asDouble();
			double low = dailyData.get("3. low").asDouble();
			double close = dailyData.get("4. close").asDouble();
			long volume = dailyData.get("6. volume").asLong();

			prices.add(new DailyPrice(dateStr, open, high, low, close, volume));
		}

		// Sort prices by date ascending (oldest first)
		prices.sort(Comparator.comparing(DailyPrice::getDate));

		return new StockResponse(symbol, prices);
	}
}
