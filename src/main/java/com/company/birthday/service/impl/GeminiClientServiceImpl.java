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
           1. Đoán tính cách: Dựa trên ngày sinh và vị trí công việc, hãy phán một cách "duyên dáng" về tính cách của họ.
           2. Yếu tố hài hước: Lồng ghép các tình huống đặc trưng của vị trí %s.
           3. Giọng văn: Hài hước, thông minh, không dùng từ ngữ quá sáo rỗng và thể hiện sự trân trọng đóng góp của họ trong team.
           4. Định dạng: Viết thành một đoạn văn ngắn gọn, súc tích, phù hợp để gửi tặng đồng nghiệp. Giới hạn 700 ký tự.
       * Mẫu phong cách tham khảo (không sao chép nguyên văn): %s
       * Chỉ trả về nội dung lời chúc hoàn chỉnh, không thêm giải thích.
       """.formatted(safeFullName, safeDateOfBirth, safeJobTitle, safeJobTitle, safeFallbackMessage);
    }
}



