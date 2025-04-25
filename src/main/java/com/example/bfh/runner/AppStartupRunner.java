package com.example.bfh.runner;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

// import com.example.bfh.service.SolverService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
// import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

@Component
public class AppStartupRunner implements ApplicationRunner {
    private static final String INIT_URL = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook";

    // Replace with your details
    private static final String NAME = "John Doe";
    private static final String REG_NO = "REG12347";
    private static final String EMAIL = "john@example.com";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 1. Call /generateWebhook
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", NAME);
        requestBody.put("regNo", REG_NO);
        requestBody.put("email", EMAIL);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(INIT_URL, entity, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            System.err.println("Failed to get webhook info: " + response.getStatusCode());
            return;
        }

        JsonNode root = objectMapper.readTree(response.getBody());
        String webhookUrl = root.get("webhook").asText();
        String accessToken = root.get("accessToken").asText();
        JsonNode data = root.get("data");

        // 2. Decide which question to solve
        int regNumLastTwo = Integer.parseInt(REG_NO.substring(REG_NO.length() - 2));
        boolean isOdd = regNumLastTwo % 2 == 1;

        Map<String, Object> output = new LinkedHashMap<>();
        output.put("regNo", REG_NO);

        if (isOdd) {
            // Question 1: Mutual Followers
            List<List<Integer>> mutuals = findMutualFollowers(data.get("users"));
            output.put("outcome", mutuals);
        } else {
            // Question 2: Nth-Level Followers
            JsonNode usersNode = data.get("users");
            int n = usersNode.get("n").asInt();
            int findId = usersNode.get("findId").asInt();
            JsonNode usersArr = usersNode.get("users");
            List<Integer> nthLevel = findNthLevelFollowers(usersArr, findId, n);
            output.put("outcome", nthLevel);
        }

        // 3. POST result to webhook with retry logic
        boolean success = false;
        int attempts = 0;
        int maxAttempts = 4;
        while (!success && attempts < maxAttempts) {
            try {
                HttpHeaders resultHeaders = new HttpHeaders();
                resultHeaders.setContentType(MediaType.APPLICATION_JSON);
                resultHeaders.set("Authorization", accessToken);
                HttpEntity<String> resultEntity = new HttpEntity<>(objectMapper.writeValueAsString(output), resultHeaders);

                ResponseEntity<String> resultResponse = restTemplate.postForEntity(webhookUrl, resultEntity, String.class);
                if (resultResponse.getStatusCode().is2xxSuccessful()) {
                    System.out.println("Webhook POST succeeded.");
                    success = true;
                } else {
                    System.err.println("Webhook POST failed: " + resultResponse.getStatusCode());
                }
            } catch (Exception ex) {
                System.err.println("Webhook POST exception: " + ex.getMessage());
            }
            attempts++;
            if (!success && attempts < maxAttempts) {
                Thread.sleep(1500);
            }
        }
        if (!success) {
            System.err.println("Failed to POST to webhook after " + maxAttempts + " attempts.");
        }
    }

    // --- Question 1: Mutual Followers ---
    private List<List<Integer>> findMutualFollowers(JsonNode users) {
        if (users == null) {
            System.err.println("Users data is null");
            return new ArrayList<>();
        }

        Map<Integer, Set<Integer>> followsMap = new HashMap<>();
        for (JsonNode user : users) {
            JsonNode idNode = user.get("id");
            JsonNode followsNode = user.get("follows");
            
            if (idNode == null || followsNode == null) {
                continue;
            }
            
            try {
                int id = idNode.asInt();
                Set<Integer> follows = new HashSet<>();
                
                for (JsonNode f : followsNode) {
                    if (f != null) {
                        follows.add(f.asInt());
                    }
                }
                followsMap.put(id, follows);
            } catch (Exception e) {
                System.err.println("Error processing user: " + e.getMessage());
            }
        }

        Set<String> seen = new HashSet<>();
        List<List<Integer>> result = new ArrayList<>();
        
        for (int id : followsMap.keySet()) {
            for (int fid : followsMap.get(id)) {
                if (followsMap.containsKey(fid) && followsMap.get(fid).contains(id)) {
                    int min = Math.min(id, fid);
                    int max = Math.max(id, fid);
                    String key = min + "-" + max;
                    if (!seen.contains(key)) {
                        result.add(Arrays.asList(min, max));
                        seen.add(key);
                    }
                }
            }
        }
        return result;
    }



    
    private List<Integer> findNthLevelFollowers(JsonNode users, int findId, int n) {
        if (users == null) {
            System.err.println("Users data is null");
            return new ArrayList<>();
        }

        Map<Integer, List<Integer>> graph = new HashMap<>();
        for (JsonNode user : users) {
            JsonNode idNode = user.get("id");
            JsonNode followsNode = user.get("follows");
            
            if (idNode == null || followsNode == null) {
                continue;
            }
            
            try {
                int id = idNode.asInt();
                List<Integer> follows = new ArrayList<>();
                for (JsonNode f : followsNode) {
                    if (f != null) {
                        follows.add(f.asInt());
                    }
                }
                graph.put(id, follows);
            } catch (Exception e) {
                System.err.println("Error processing user: " + e.getMessage());
            }
        }

        Set<Integer> visited = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();
        queue.add(findId);
        visited.add(findId);
        int level = 0;
        while (!queue.isEmpty() && level < n) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                int curr = queue.poll();
                for (int next : graph.getOrDefault(curr, Collections.emptyList())) {
                    if (!visited.contains(next)) {
                        queue.add(next);
                        visited.add(next);
                    }
                }
            }
            level++;
        }
        List<Integer> result = new ArrayList<>(queue);
        Collections.sort(result);
        return result;
    }
}
