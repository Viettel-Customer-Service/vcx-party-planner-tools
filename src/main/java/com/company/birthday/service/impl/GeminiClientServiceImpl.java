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
       "Soi quẻ" nhân số học và viết một lời chúc sinh nhật thật "mặn" cho đồng nghiệp của mình là %s, sinh ngày %s, hiện đang "chinh chiến" ở vị trí %s.

        Nhờ bạn trổ tài phân tích các con số để "phán" một cách duyên dáng về tính cách của người này, sao cho vừa hài hước vừa đúng chất của một %s thực thụ. Hãy lồng ghép khéo léo những tình huống "dở khóc dở cười" đặc trưng của nghề nghiệp vào lời chúc, tránh dùng văn mẫu sáo rỗng. Đừng quên gửi gắm sự trân trọng vì những đóng góp của họ cho %s trong thời gian qua.

        Yêu cầu:

        Văn phong: Thông minh, hóm hỉnh, hành văn tự nhiên như người thật nói chuyện.

        Độ dài: Ngắn gọn, súc tích (dưới 700 ký tự), phù hợp để gửi tin nhắn hoặc đăng Facebook.

        Tham khảo phong cách: %s.

        Kết quả: Chỉ trả về đúng nội dung lời chúc, không cần dẫn nhập hay giải thích thêm.
       """.formatted(safeFullName, safeDateOfBirth, safeJobTitle, safeJobTitle, "Trung tâm Công nghệ & Kỹ thuật", safeFallbackMessage);
    }
}

