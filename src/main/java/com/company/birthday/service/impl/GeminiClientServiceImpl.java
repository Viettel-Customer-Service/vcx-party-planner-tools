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
Hãy nhập vai một "Bậc thầy Nhân số học hệ... cơm văn phòng", người có khả năng nhìn thấu số phận qua ngày sinh và cả những ticket trên Jira. Viết một lời chúc sinh nhật vừa "tâm linh" vừa thực tế cho đồng nghiệp của tôi.

* Dữ liệu đầu vào:
    - Tên: %s
    - Ngày sinh: %s
    - Vị trí: %s

* Yêu cầu "vibe" và nội dung:
    1. Phân tích nhân số học kiểu "vui vẻ không quạu": Kết nối các con số trong ngày sinh với thói quen làm việc của một %s (Ví dụ: Số 7 nhưng lại hay bị nghiệp deadline quật, hoặc số 1 nhưng luôn là người cuối cùng rời văn phòng).
    2. Tình huống thực tế: Lồng ghép 1 chi tiết cực kỳ đặc trưng của dân văn phòng (như: họp vô tri, chiếc máy pha cà phê hỏng, những lúc "seen" tin nhắn sếp nhưng chưa biết trả lời sao, hoặc niềm hạnh phúc khi không bị réo tên lúc 5h chiều).
    3. Gu hài hước: Hài ngầm, kiểu "Observation Comedy" (nhìn đâu trúng đó). Tuyệt đối không dùng những từ sáo rỗng như "tuổi mới thành công, hạnh phúc". 
    4. Cấu trúc: 
        - Mở đầu: Một lời phán đầy tính "chiêm tinh" về sự xuất hiện của nhân vật này trong team.
        - Giữa: Sự giao thoa giữa số mệnh và cái nghiệp làm %s.
        - Kết: Một lời chúc mang tính "giải nghiệp" hoặc kỳ vọng thực tế (ví dụ: chúc mọi task đều Green, chúc client không đổi ý vào phút chót).
    5. Giới hạn: Dưới 700 ký tự. 

* Lưu ý đặc biệt: Viết tự nhiên như một người bạn thân đang nhắn tin trong group chat công ty, có chút lém lỉnh nhưng vẫn đầy sự trân trọng.

Chỉ trả về nội dung lời chúc, không giải thích gì thêm.
""".formatted(safeFullName, safeDateOfBirth, safeJobTitle, safeJobTitle);
    }
}

