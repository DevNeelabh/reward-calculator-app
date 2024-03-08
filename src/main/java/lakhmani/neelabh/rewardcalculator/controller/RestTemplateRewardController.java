package lakhmani.neelabh.rewardcalculator.controller;

import lakhmani.neelabh.rewardcalculator.dto.MonthlyRewardPoints;
import lakhmani.neelabh.rewardcalculator.dto.RewardSummary;
import lakhmani.neelabh.rewardcalculator.model.Transaction;
import lakhmani.neelabh.rewardcalculator.service.RewardCalculatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reward/restTemplate")
public class RestTemplateRewardController {
    @Autowired
    private RewardCalculatorService rewardCalculatorService;

    @GetMapping("/calculate")
    public ResponseEntity<List<RewardSummary>> calculateRewards() {
        try {
            // Call API to fetch transactions
            RestTemplate restTemplate = new RestTemplate();
            String apiUrl = "http://localhost:3001/transactions";
            ResponseEntity<List<Transaction>> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Transaction>>() {}
            );
            List<Transaction> transactions = response.getBody();

            // Calculate reward summary
            assert transactions != null;
            List<RewardSummary> rewardSummaries = calculateRewardSummaries(transactions);

            return ResponseEntity.ok(rewardSummaries);
        } catch (RestClientException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private List<RewardSummary> calculateRewardSummaries(List<Transaction> transactions) {
        Map<Long, Map<String, Integer>> rewardSummary = new HashMap<>();
        for (Transaction transaction : transactions) {
            long customerId = transaction.getCustomerId();
            String month = transaction.getMonth();
            int points = rewardCalculatorService.calculateRewardPoints(transaction.getAmount());

            // Initialize the customer entry if not present
            rewardSummary.putIfAbsent(customerId, new HashMap<>());

            // Update monthly points
            Map<String, Integer> monthlyPoints = rewardSummary.get(customerId);
            monthlyPoints.put(month, monthlyPoints.getOrDefault(month, 0) + points);
        }

        // Convert the nested map to a list of RewardSummary objects
        List<RewardSummary> rewardSummaries = new ArrayList<>();
        for (Map.Entry<Long, Map<String, Integer>> entry : rewardSummary.entrySet()) {
            long customerId = entry.getKey();
            int totalPoints = entry.getValue().values().stream().mapToInt(Integer::intValue).sum();
            List<MonthlyRewardPoints> monthlyRewardPointsList = new ArrayList<>();
            entry.getValue().forEach((month, points) -> monthlyRewardPointsList.add(new MonthlyRewardPoints(month, points)));
            rewardSummaries.add(new RewardSummary(customerId, totalPoints, monthlyRewardPointsList));
        }
        return rewardSummaries;
    }
}
