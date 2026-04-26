package com.company.birthday.service.impl;

import com.company.birthday.dto.gemini.GeminiDto;
import com.company.birthday.service.GeminiClientService;
import com.company.birthday.service.exception.GeminiTimeoutException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.SocketTimeoutException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class GeminiClientServiceImpl implements GeminiClientService {

    private static final DateTimeFormatter BIRTHDAY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final RestClient restClient;
    private final String apiUrl;
    private final String apiKey;
    private final int maxAttempts;
    private final long backoffMs;

    public GeminiClientServiceImpl(RestClient.Builder restClientBuilder,
                                   @Value("${gemini.api.url}") String apiUrl,
                                   @Value("${gemini.api.key:}") String apiKey,
                                   @Value("${gemini.api.connect-timeout-ms:5000}") int connectTimeoutMs,
                                   @Value("${gemini.api.read-timeout-ms:5000}") int readTimeoutMs,
                                   @Value("${birthday.gemini.retry.max-attempts:3}") int maxAttempts,
                                   @Value("${birthday.gemini.retry.backoff-ms:2000}") long backoffMs) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeoutMs);
        requestFactory.setReadTimeout(readTimeoutMs);

        this.restClient = restClientBuilder
                .requestFactory(requestFactory)
                .build();
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.maxAttempts = Math.max(1, maxAttempts);
        this.backoffMs = Math.max(0L, backoffMs);
    }

    @Override
    public String generateBirthdayMessage(String fullName, LocalDate dateOfBirth, String jobTitle, String fallbackMessage) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY is missing.");
        }

        GeminiDto.GenerateContentRequest requestBody = new GeminiDto.GenerateContentRequest(
                List.of(new GeminiDto.Content(List.of(new GeminiDto.Part(buildBirthdayPrompt(fullName, dateOfBirth, jobTitle, fallbackMessage)))))
        );

        GeminiTimeoutException lastTimeout = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return doGenerate(requestBody);
            } catch (GeminiTimeoutException ex) {
                lastTimeout = ex;
                if (attempt == maxAttempts) {
                    throw ex;
                }
                sleepBeforeRetry();
            }
        }

        throw lastTimeout == null ? new GeminiTimeoutException("Gemini timeout", null) : lastTimeout;
    }

    private void sleepBeforeRetry() {
        if (backoffMs <= 0) {
            return;
        }
        try {
            Thread.sleep(backoffMs);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new GeminiTimeoutException("Gemini retry interrupted", ex);
        }
    }

    private String doGenerate(GeminiDto.GenerateContentRequest requestBody) {
        try {
            GeminiDto.GenerateContentResponse response = restClient.post()
                    .uri(apiUrl + "?key={key}", apiKey)
                    .body(requestBody)
                    .retrieve()
                    .body(GeminiDto.GenerateContentResponse.class);

            if (response == null || response.candidates() == null || response.candidates().isEmpty()) {
                throw new IllegalStateException("Gemini response is empty.");
            }

            GeminiDto.Candidate firstCandidate = response.candidates().get(0);
            if (firstCandidate.content() == null || firstCandidate.content().parts() == null || firstCandidate.content().parts().isEmpty()) {
                throw new IllegalStateException("Gemini response has no text content.");
            }

            String content = firstCandidate.content().parts().get(0).text();
            if (content == null || content.isBlank()) {
                throw new IllegalStateException("Gemini generated blank message.");
            }

            return content.trim();
        } catch (ResourceAccessException ex) {
            if (isTimeout(ex)) {
                throw new GeminiTimeoutException("Gemini timeout", ex);
            }
            throw ex;
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 408 || ex.getStatusCode().value() == 504) {
                throw new GeminiTimeoutException("Gemini timeout", ex);
            }
            throw ex;
        } catch (org.springframework.web.client.RestClientException ex) {
            // Catches cases where SocketTimeoutException occurs during response reading
            // (e.g., while reading headers/body), which Spring wraps in a plain RestClientException
            // instead of ResourceAccessException.
            if (isTimeout(ex)) {
                throw new GeminiTimeoutException("Gemini timeout (read)", ex);
            }
            throw ex;
        }
    }

    private boolean isTimeout(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof SocketTimeoutException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private String buildBirthdayPrompt(String fullName, LocalDate dateOfBirth, String jobTitle, String fallbackMessage) {
        String safeFullName = fullName == null || fullName.isBlank() ? "bạn" : fullName.trim();
        String safeDateOfBirth = dateOfBirth == null ? "chưa cung cấp" : dateOfBirth.format(BIRTHDAY_FORMATTER);
        String safeJobTitle = jobTitle == null || jobTitle.isBlank() ? "nhân viên" : jobTitle.trim();
        String safeFallbackMessage = fallbackMessage == null || fallbackMessage.isBlank()
                ? "Chúc bạn một ngày sinh nhật thật vui và nhiều năng lượng tích cực cùng team."
                : fallbackMessage.trim();

        return """
       Hãy đóng vai một chuyên gia phân tích nhân số học hài hước để viết lời chúc sinh nhật cho đồng nghiệp của tôi.
       * Thông tin nhân vật:
           * Tên: %s
           * Ngày sinh: %s
           * Vị trí làm việc: %s
       * Yêu cầu nội dung:
           1. Đoán tính cách: Suy đoán nhẹ nhàng, hợp lý dựa trên ngày sinh và vị trí công việc, tránh nói quá đà.
           2. Hài văn phòng: Lồng ghép 1–2 tình huống rất đời thường tại công ty (deadline, họp, bug, khách hàng, OT...) liên quan đến vị trí %s.
           3. Humor style:
                - Không kể joke lộ liễu
                - Không chơi chữ gượng ép
                - Ưu tiên kiểu "nhận xét đúng quá nên buồn cười"
           4. Kết cấu:
                - 1 mở đầu nhẹ nhàng
                - 1 đoạn giữa có tình huống hài
                - 1 câu kết có “punchline” tinh tế
           5. Giọng văn: Tự nhiên như người thật viết, hài hước nhưng vẫn thể hiện sự trân trọng.
           6. Giới hạn: tối đa 700 ký tự
       * Tránh:
            - Hài lố, cà khịa quá đà
            - Sáo rỗng kiểu “chúc bạn sức khỏe, thành công”
            - Văn phong giống AI
       * Chỉ trả về nội dung lời chúc hoàn chỉnh, không thêm giải thích.
       """.formatted(safeFullName, safeDateOfBirth, safeJobTitle, safeJobTitle);
    }
}

