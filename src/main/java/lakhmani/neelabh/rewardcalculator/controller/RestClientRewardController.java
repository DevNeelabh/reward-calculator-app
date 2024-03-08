package lakhmani.neelabh.rewardcalculator.controller;

import lakhmani.neelabh.rewardcalculator.dto.MonthlyRewardPoints;
import lakhmani.neelabh.rewardcalculator.dto.RewardSummary;
import lakhmani.neelabh.rewardcalculator.model.Transaction;
import lakhmani.neelabh.rewardcalculator.service.RewardCalculatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rewardRestClient")
public class RestClientRewardController {

    @Autowired
    private RewardCalculatorService rewardCalculatorService;
     private final RestClient restClient;

    public RestClientRewardController(){
        restClient = RestClient.builder().baseUrl("http://localhost:3001")
                .build();
    }

    @GetMapping("/calculate")
    public ResponseEntity<List<RewardSummary>> calculateReward(){
        //call to transactions api
        List<Transaction> response = restClient.get()
                .uri("/transactions")
                .retrieve()
                .body(new ParameterizedTypeReference<List<Transaction>>() {
                });
        Map<Long, Map<String, Integer>> rewardSummary = new HashMap<>();

        assert response != null;
        for (Transaction transaction : response) {
            long customerId = transaction.getCustomerId();
            String month = transaction.getMonth();
            int points = rewardCalculatorService.calculateRewardPoints(transaction.getAmount());

            // Initialize the customer entry if not present
            if (!rewardSummary.containsKey(customerId)) {
                rewardSummary.put(customerId, new HashMap<>());
            }

            // Update monthly points
            Map<String, Integer> monthlyPoints = rewardSummary.get(customerId);
            if (!monthlyPoints.containsKey(month)) {
                monthlyPoints.put(month, 0);
            }
            monthlyPoints.put(month, monthlyPoints.get(month) + points);
        }

        // Convert the nested map to a list of RewardSummary objects
        List<RewardSummary> rewardSummaries = new ArrayList<>();
        for (Map.Entry<Long, Map<String, Integer>> entry : rewardSummary.entrySet()) {
            long customerId = entry.getKey();
            int totalPoints = 0;
            List<MonthlyRewardPoints> monthlyRewardPointsList = new ArrayList<>();
            for (Map.Entry<String, Integer> monthlyEntry : entry.getValue().entrySet()) {
                String month = monthlyEntry.getKey();
                int points = monthlyEntry.getValue();
                totalPoints += points;
                monthlyRewardPointsList.add(new MonthlyRewardPoints(month, points));
            }
            rewardSummaries.add(new RewardSummary(customerId, totalPoints, monthlyRewardPointsList));
        }

        return ResponseEntity.ok(rewardSummaries);

    }
}
