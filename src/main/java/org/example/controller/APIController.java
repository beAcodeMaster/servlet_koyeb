package org.example.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.model.APIParam;
import org.example.model.ModelType;
import org.example.service.APIService;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

@WebServlet(name = "APIServlet", value = "/api")
public class APIController extends HttpServlet {
    // 싱글턴 APIService 인스턴스
    final APIService apiService = APIService.getInstance();
    final Logger logger = Logger.getLogger(APIController.class.getName());
    final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void init() throws ServletException {
        logger.info("APIController init...");
    }

    // POST 요청 처리
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            // POST 요청의 JSON 바디를 한 번만 파싱
            JsonNode requestBody = objectMapper.readTree(req.getReader());

            // 프롬프트 구성
            StringBuilder promptBuilder = new StringBuilder();

            // 필수 파라미터: 인원 수
            String peopleParam = requestBody.path("people").asText("");
            promptBuilder.append("인원: ").append(peopleParam).append("\n");

            // 선택적 파라미터들 처리
            if (!requestBody.path("foodType").isMissingNode() && !requestBody.path("foodType").asText().isEmpty()) {
                promptBuilder.append("음식 종류: ").append(requestBody.path("foodType").asText()).append("\n");
            }

            if (!requestBody.path("main").isMissingNode() && !requestBody.path("main").asText().isEmpty()) {
                promptBuilder.append("메인: ").append(requestBody.path("main").asText()).append("\n");
            }

            if (!requestBody.path("soup").isMissingNode() && !requestBody.path("soup").asText().isEmpty()) {
                promptBuilder.append("국물 여부: ").append(requestBody.path("soup").asText()).append("\n");
            }

            if (!requestBody.path("spicyLevel").isMissingNode() && !requestBody.path("spicyLevel").asText().isEmpty()) {
                promptBuilder.append("맵기 정도: ").append(requestBody.path("spicyLevel").asText()).append("\n");
            }

            // notEat 처리 (배열일 수 있음)
            if (requestBody.path("notEat").isArray() && requestBody.path("notEat").size() > 0) {
                promptBuilder.append("못 먹는 음식: ");
                for (int i = 0; i < requestBody.path("notEat").size(); i++) {
                    promptBuilder.append(requestBody.path("notEat").get(i).asText());
                    if (i < requestBody.path("notEat").size() - 1) {
                        promptBuilder.append(", ");
                    }
                }
                promptBuilder.append("\n");
            }

            if (!requestBody.path("diet").isMissingNode() && !requestBody.path("diet").asText().isEmpty()) {
                promptBuilder.append("다이어트 여부: ").append(requestBody.path("diet").asText()).append("\n");
            }

            if (!requestBody.path("specialNotes").isMissingNode() && !requestBody.path("specialNotes").asText().isEmpty()) {
                promptBuilder.append("특이사항: ").append(requestBody.path("specialNotes").asText()).append("\n");
            }

            // 랜덤 추천 여부
            if (!requestBody.path("random").isMissingNode() && requestBody.path("random").asBoolean()) {
                promptBuilder.append("랜덤 추천 요청\n");
            }

            String prompt = promptBuilder.toString();

            // 모델 선택
            String modelParam = requestBody.path("model").asText("GROQ");
            ModelType modelType;
            if ("GROQ".equalsIgnoreCase(modelParam)) {
                modelType = ModelType.GROQ_LLAMA;
            } else if ("TOGETHER".equalsIgnoreCase(modelParam)) {
                modelType = ModelType.TOGETHER_LLAMA;
            } else {
                throw new IllegalArgumentException("지원하지 않는 모델입니다: " + modelParam);
            }

            APIParam apiParam = new APIParam(prompt, modelType);
            String result = apiService.callAPI(apiParam);
            out.println(result);

        } catch (Exception e) {
            logger.severe("API 호출 중 오류 발생: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    // GET 요청 처리
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            // 프롬프트 구성
            StringBuilder promptBuilder = new StringBuilder();

            // 필수 파라미터: 인원 수
            String peopleParam = req.getParameter("people");
            promptBuilder.append("인원: ").append(peopleParam).append("\n");

            // 선택적 파라미터들 처리
            String foodType = req.getParameter("foodType");
            if (foodType != null && !foodType.isEmpty()) {
                promptBuilder.append("음식 종류: ").append(foodType).append("\n");
            }

            String main = req.getParameter("main");
            if (main != null && !main.isEmpty()) {
                promptBuilder.append("메인: ").append(main).append("\n");
            }

            String soup = req.getParameter("soup");
            if (soup != null && !soup.isEmpty()) {
                promptBuilder.append("국물 여부: ").append(soup).append("\n");
            }

            String spicyLevel = req.getParameter("spicyLevel");
            if (spicyLevel != null && !spicyLevel.isEmpty()) {
                promptBuilder.append("맵기 정도: ").append(spicyLevel).append("\n");
            }

            String notEat = req.getParameter("notEat");
            if (notEat != null && !notEat.isEmpty()) {
                promptBuilder.append("못 먹는 음식: ").append(notEat).append("\n");
            }

            String diet = req.getParameter("diet");
            if (diet != null && !diet.isEmpty()) {
                promptBuilder.append("다이어트 여부: ").append(diet).append("\n");
            }

            String specialNotes = req.getParameter("specialNotes");
            if (specialNotes != null && !specialNotes.isEmpty()) {
                promptBuilder.append("특이사항: ").append(specialNotes).append("\n");
            }

            String random = req.getParameter("random");
            if ("true".equalsIgnoreCase(random)) {
                promptBuilder.append("랜덤 추천 요청\n");
            }

            String prompt = promptBuilder.toString();

            // 모델 선택
            String modelParam = req.getParameter("model");
            ModelType modelType;
            if ("GROQ".equalsIgnoreCase(modelParam)) {
                modelType = ModelType.GROQ_LLAMA;
            } else if ("TOGETHER".equalsIgnoreCase(modelParam)) {
                modelType = ModelType.TOGETHER_LLAMA;
            } else {
                // 기본값 설정
                modelType = ModelType.GROQ_LLAMA;
            }

            APIParam apiParam = new APIParam(prompt, modelType);
            String result = apiService.callAPI(apiParam);
            out.println(result);

        } catch (Exception e) {
            logger.severe("API 호출 중 오류 발생: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}