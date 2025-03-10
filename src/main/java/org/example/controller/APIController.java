package org.example.controller;

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
        processRequest(req, resp, false);
    }

    // GET 요청 처리
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp, true);
    }

    // 공통 요청 처리 메서드: isGet이 true면 GET 방식, false면 POST 방식
    private void processRequest(HttpServletRequest req, HttpServletResponse resp, boolean isGet) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            // 프롬프트 구성
            StringBuilder promptBuilder = new StringBuilder();
            String peopleParam = isGet ? req.getParameter("people") :
                    objectMapper.readTree(req.getReader()).path("people").asText();
            promptBuilder.append("인원: ").append(peopleParam).append("\n");

            // GET과 POST 모두에 대해 공통 파라미터 처리
            String foodType = isGet ? req.getParameter("foodType") :
                    objectMapper.readTree(req.getReader()).path("foodType").asText();
            if (foodType != null && !foodType.isEmpty()) {
                promptBuilder.append("음식 종류: ").append(foodType).append("\n");
            }

            String main = isGet ? req.getParameter("main") :
                    objectMapper.readTree(req.getReader()).path("main").asText();
            if (main != null && !main.isEmpty()) {
                promptBuilder.append("메인: ").append(main).append("\n");
            }

            String soup = isGet ? req.getParameter("soup") :
                    objectMapper.readTree(req.getReader()).path("soup").asText();
            if (soup != null && !soup.isEmpty()) {
                promptBuilder.append("국물 여부: ").append(soup).append("\n");
            }

            String spicyLevel = isGet ? req.getParameter("spicyLevel") :
                    objectMapper.readTree(req.getReader()).path("spicyLevel").asText();
            if (spicyLevel != null && !spicyLevel.isEmpty()) {
                promptBuilder.append("맵기 정도: ").append(spicyLevel).append("\n");
            }

            String notEat = isGet ? req.getParameter("notEat") :
                    objectMapper.readTree(req.getReader()).path("notEat").asText();
            if (notEat != null && !notEat.isEmpty()) {
                promptBuilder.append("못 먹는 음식: ").append(notEat).append("\n");
            }

            String diet = isGet ? req.getParameter("diet") :
                    objectMapper.readTree(req.getReader()).path("diet").asText();
            if (diet != null && !diet.isEmpty()) {
                promptBuilder.append("다이어트 여부: ").append(diet).append("\n");
            }

            String specialNotes = isGet ? req.getParameter("specialNotes") :
                    objectMapper.readTree(req.getReader()).path("specialNotes").asText();
            if (specialNotes != null && !specialNotes.isEmpty()) {
                promptBuilder.append("특이사항: ").append(specialNotes).append("\n");
            }

            String random = isGet ? req.getParameter("random") :
                    objectMapper.readTree(req.getReader()).path("random").asText();
            if ("true".equalsIgnoreCase(random)) {
                promptBuilder.append("랜덤 추천 요청\n");
            }

            String prompt = promptBuilder.toString();

            // 모델 선택: GET과 POST 모두 model 파라미터를 사용
            String modelParam = isGet ? req.getParameter("model") :
                    objectMapper.readTree(req.getReader()).path("model").asText();
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
}
