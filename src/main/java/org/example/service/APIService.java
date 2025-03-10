package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.model.APIParam;
import org.example.model.ModelResponse;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class APIService {
    private static final APIService instance = new APIService();
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final String groqToken;
    private final String togetherToken;
    private final String groqGuide;
    private final String togetherGuide;
    private final Logger logger = Logger.getLogger(APIService.class.getName());

    public static APIService getInstance() {
        return instance;
    }

    private APIService() {
        // 시스템 환경 변수에서 직접 값 가져오기
        groqToken = System.getenv("GROQ_KEY");
        if (groqToken == null || groqToken.isEmpty()) {
            logger.warning("GROQ_KEY 환경 변수가 설정되지 않았습니다.");
        }

        togetherToken = System.getenv("TOGETHER_KEY");
        if (togetherToken == null || togetherToken.isEmpty()) {
            logger.warning("TOGETHER_KEY 환경 변수가 설정되지 않았습니다.");
        }

        // 기본 가이드라인 설정 (환경 변수가 없을 경우 사용)
        String defaultGuide = "당신은 음식을 추천해주는 AI 어시스턴트입니다. 사용자의 요청에 맞는 메뉴를 추천해주세요.";

        // 환경 변수가 없을 경우 기본값 사용
        this.groqGuide = (System.getenv("GROQ_GUIDE") != null) ? System.getenv("GROQ_GUIDE") : defaultGuide;
        this.togetherGuide = (System.getenv("TOGETHER_GUIDE") != null) ? System.getenv("TOGETHER_GUIDE") : defaultGuide;

    }

    public String callAPI(APIParam apiParam) throws Exception {
        String url;
        String token;
        String instruction;

        // 플랫폼에 따른 설정 선택
        switch (apiParam.model().platform) {
            case GROQ -> {
                url = "https://api.groq.com/openai/v1/chat/completions";
                token = groqToken;
                if (token == null || token.isEmpty()) {
                    throw new Exception("GROQ API 키가 설정되지 않았습니다.");
                }
                instruction = groqGuide;
            }
            case TOGETHER -> {
                url = "https://api.together.xyz/v1/chat/completions";
                token = togetherToken;
                if (token == null || token.isEmpty()) {
                    throw new Exception("TOGETHER API 키가 설정되지 않았습니다.");
                }
                instruction = togetherGuide;
            }
            default -> throw new Exception("지원하지 않는 플랫폼입니다.");
        }

        // 입력 유효성 검사
        if (apiParam.prompt() == null || apiParam.prompt().isEmpty()) {
            throw new Exception("유효한 프롬프트가 필요합니다.");
        }

        // JSON 이스케이프 처리
        ObjectMapper escapeMapper = new ObjectMapper();
        String escapedPrompt = escapeMapper.writeValueAsString(apiParam.prompt());
        escapedPrompt = escapedPrompt.substring(1, escapedPrompt.length() - 1); // 따옴표 제거

        String escapedInstruction = escapeMapper.writeValueAsString(instruction);
        escapedInstruction = escapedInstruction.substring(1, escapedInstruction.length() - 1); // 따옴표 제거

        // 요청 바디 생성
        String body = """
                {
                     "messages": [
                       {
                         "role": "system",
                         "content": "%s"
                       },
                       {
                         "role": "user",
                         "content": "%s"
                       }
                     ],
                     "model": "%s"
                   }
                """.formatted(escapedInstruction, escapedPrompt, apiParam.model().name);

        // HTTP 요청 생성 및 전송
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // 응답 코드 확인
            if (response.statusCode() >= 400) {
                logger.severe("API 오류 응답: " + response.statusCode() + " - " + response.body());
                throw new Exception("API 응답 오류: " + response.statusCode());
            }

            // 응답 파싱
            String responseBody = response.body();
            logger.fine("API 응답: " + responseBody);

            ObjectMapper objectMapper = new ObjectMapper();
            ModelResponse modelResponse = objectMapper.readValue(responseBody, ModelResponse.class);

            if (modelResponse.choices() == null || modelResponse.choices().isEmpty()) {
                throw new Exception("API 응답에 유효한 결과가 없습니다.");
            }

            String content = modelResponse.choices().get(0).message().content();
            Map<String, String> map = new HashMap<>();
            map.put("content", content);
            return objectMapper.writeValueAsString(map);
        } catch (IOException e) {
            logger.severe("API 호출 중 I/O 오류: " + e.getMessage());
            throw new Exception("API 호출 중 I/O 오류: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.severe("API 호출이 중단되었습니다: " + e.getMessage());
            throw new Exception("API 호출이 중단되었습니다.");
        } catch (Exception e) {
            logger.severe("API 호출 중 예상치 못한 오류: " + e.getMessage());
            throw new Exception("API 호출 중 오류: " + e.getMessage());
        }
    }
}