package com.bookswap.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@Service
public class ExternalApiService {
    
    private static final Logger logger = LoggerFactory.getLogger(ExternalApiService.class);
    
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    @Value("${external-api.books.base-url}")
    private String booksApiBaseUrl;
    
    @Value("${external-api.books.timeout}")
    private int booksApiTimeout;
    
    @Value("${external-api.currency.base-url}")
    private String currencyApiBaseUrl;
    
    @Value("${external-api.currency.timeout}")
    private int currencyApiTimeout;
    
    public ExternalApiService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Поиск информации о книге через Google Books API
     */
    public BookInfo searchBookInfo(String isbn) {
        logger.info("Поиск информации о книге по ISBN: {}", isbn);
        
        try {
            String url = booksApiBaseUrl + "/volumes?q=isbn:" + isbn;
            
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("Accept", "application/json")
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    logger.error("Ошибка при запросе к Books API: {}", response.code());
                    return null;
                }
                
                String responseBody = response.body().string();
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                
                if (jsonNode.has("items") && jsonNode.get("items").size() > 0) {
                    JsonNode bookItem = jsonNode.get("items").get(0);
                    JsonNode volumeInfo = bookItem.get("volumeInfo");
                    
                    BookInfo bookInfo = new BookInfo();
                    bookInfo.setTitle(getStringValue(volumeInfo, "title"));
                    bookInfo.setDescription(getStringValue(volumeInfo, "description"));
                    bookInfo.setPublisher(getStringValue(volumeInfo, "publisher"));
                    bookInfo.setPublishedDate(getStringValue(volumeInfo, "publishedDate"));
                    bookInfo.setPageCount(getIntValue(volumeInfo, "pageCount"));
                    bookInfo.setLanguage(getStringValue(volumeInfo, "language"));
                    
                    // Авторы
                    if (volumeInfo.has("authors")) {
                        JsonNode authors = volumeInfo.get("authors");
                        StringBuilder authorsStr = new StringBuilder();
                        for (int i = 0; i < authors.size(); i++) {
                            if (i > 0) authorsStr.append(", ");
                            authorsStr.append(authors.get(i).asText());
                        }
                        bookInfo.setAuthors(authorsStr.toString());
                    }
                    
                    // Категории (жанры)
                    if (volumeInfo.has("categories")) {
                        JsonNode categories = volumeInfo.get("categories");
                        StringBuilder categoriesStr = new StringBuilder();
                        for (int i = 0; i < categories.size(); i++) {
                            if (i > 0) categoriesStr.append(", ");
                            categoriesStr.append(categories.get(i).asText());
                        }
                        bookInfo.setCategories(categoriesStr.toString());
                    }
                    
                    // Обложка
                    if (volumeInfo.has("imageLinks")) {
                        JsonNode imageLinks = volumeInfo.get("imageLinks");
                        if (imageLinks.has("thumbnail")) {
                            bookInfo.setCoverImageUrl(imageLinks.get("thumbnail").asText());
                        }
                    }
                    
                    logger.info("Информация о книге успешно получена: {}", bookInfo.getTitle());
                    return bookInfo;
                }
                
                logger.warn("Книга с ISBN {} не найдена", isbn);
                return null;
                
            }
        } catch (IOException e) {
            logger.error("Ошибка при обращении к Books API", e);
            return null;
        }
    }
    
    /**
     * Получение курса валют
     */
    public BigDecimal getCurrencyRate(String fromCurrency, String toCurrency) {
        logger.info("Получение курса валют: {} -> {}", fromCurrency, toCurrency);
        
        try {
            String url = currencyApiBaseUrl + "/" + fromCurrency.toUpperCase();
            
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("Accept", "application/json")
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    logger.error("Ошибка при запросе к Currency API: {}", response.code());
                    return BigDecimal.ONE;
                }
                
                String responseBody = response.body().string();
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                
                if (jsonNode.has("rates")) {
                    JsonNode rates = jsonNode.get("rates");
                    if (rates.has(toCurrency.toUpperCase())) {
                        BigDecimal rate = new BigDecimal(rates.get(toCurrency.toUpperCase()).asText());
                        logger.info("Курс валют получен: {} {} = 1 {}", rate, toCurrency, fromCurrency);
                        return rate;
                    }
                }
                
                logger.warn("Курс валют {} -> {} не найден", fromCurrency, toCurrency);
                return BigDecimal.ONE;
                
            }
        } catch (IOException e) {
            logger.error("Ошибка при обращении к Currency API", e);
            return BigDecimal.ONE;
        }
    }
    
    /**
     * Конвертация цены из одной валюты в другую
     */
    public BigDecimal convertPrice(BigDecimal amount, String fromCurrency, String toCurrency) {
        if (fromCurrency.equalsIgnoreCase(toCurrency)) {
            return amount;
        }
        
        BigDecimal rate = getCurrencyRate(fromCurrency, toCurrency);
        return amount.multiply(rate);
    }
    
    // Вспомогательные методы
    
    private String getStringValue(JsonNode node, String fieldName) {
        return node.has(fieldName) ? node.get(fieldName).asText() : null;
    }
    
    private Integer getIntValue(JsonNode node, String fieldName) {
        return node.has(fieldName) ? node.get(fieldName).asInt() : null;
    }
    
    // DTO класс для информации о книге
    public static class BookInfo {
        private String title;
        private String authors;
        private String description;
        private String publisher;
        private String publishedDate;
        private Integer pageCount;
        private String language;
        private String categories;
        private String coverImageUrl;
        
        // Getters and Setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getAuthors() { return authors; }
        public void setAuthors(String authors) { this.authors = authors; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getPublisher() { return publisher; }
        public void setPublisher(String publisher) { this.publisher = publisher; }
        
        public String getPublishedDate() { return publishedDate; }
        public void setPublishedDate(String publishedDate) { this.publishedDate = publishedDate; }
        
        public Integer getPageCount() { return pageCount; }
        public void setPageCount(Integer pageCount) { this.pageCount = pageCount; }
        
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
        
        public String getCategories() { return categories; }
        public void setCategories(String categories) { this.categories = categories; }
        
        public String getCoverImageUrl() { return coverImageUrl; }
        public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }
        
        public Integer getPublicationYear() {
            if (publishedDate != null && publishedDate.length() >= 4) {
                try {
                    return Integer.parseInt(publishedDate.substring(0, 4));
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        }
        
        @Override
        public String toString() {
            return "BookInfo{" +
                    "title='" + title + '\'' +
                    ", authors='" + authors + '\'' +
                    ", publisher='" + publisher + '\'' +
                    ", publishedDate='" + publishedDate + '\'' +
                    '}';
        }
    }
} 