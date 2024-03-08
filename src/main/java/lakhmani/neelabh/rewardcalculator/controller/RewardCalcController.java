    package lakhmani.neelabh.rewardcalculator.controller;

    import com.fasterxml.jackson.core.type.TypeReference;
    import com.fasterxml.jackson.databind.ObjectMapper;
    import lakhmani.neelabh.rewardcalculator.dto.MonthlyRewardPoints;
    import lakhmani.neelabh.rewardcalculator.dto.RewardSummary;
    import lakhmani.neelabh.rewardcalculator.model.Transaction;
    import lakhmani.neelabh.rewardcalculator.service.RewardCalculatorService;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.http.HttpStatus;
    import org.springframework.stereotype.Controller;
    import org.springframework.ui.Model;
    import org.springframework.web.bind.annotation.GetMapping;
    import org.springframework.web.bind.annotation.RequestMapping;

    import java.io.IOException;
    import java.net.URI;
    import java.net.URISyntaxException;
    import java.net.http.HttpClient;
    import java.net.http.HttpRequest;
    import java.net.http.HttpResponse;
    import java.util.ArrayList;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;
    import java.util.concurrent.CompletableFuture;

    @Controller
    @RequestMapping("/rewards")
    public class RewardCalcController {

        @Autowired
        private RewardCalculatorService rewardCalculatorService;

        @GetMapping("/calculate")
        public String calculateReward(Model model) {
            try {
                // Call json server to fetch transactions
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder(
                        new URI("http://localhost:3001/transactions")).GET().build();

                CompletableFuture<HttpResponse<String>> future = client.sendAsync(
                        request, HttpResponse.BodyHandlers.ofString());

                // Wait for the response to complete
                HttpResponse<String> response = future.join();
                int statusCode = response.statusCode();

                if (statusCode != HttpStatus.OK.value()) {
                    return "error";
                }

                // Parse JSON response
                ObjectMapper objectMapper = new ObjectMapper();
                List<Transaction> transactions = objectMapper.readValue(
                        response.body(), new TypeReference<>() {});

                Map<Long, Map<String, Integer>> rewardSummary = new HashMap<>();

                transactions.forEach(transaction -> {
                    long customerId = transaction.getCustomerId();
                    String month = transaction.getMonth();
                    int points = rewardCalculatorService.calculateRewardPoints(transaction.getAmount());

                    // Initialize the customer entry if not present
                    rewardSummary.putIfAbsent(customerId, new HashMap<>());

                    // Update monthly points
                    rewardSummary.get(customerId).merge(month, points, Integer::sum);
                });

                // Convert the nested map to a list of RewardSummary objects
                List<RewardSummary> rewardSummaries = new ArrayList<>();
                rewardSummary.forEach((customerId, monthlyPoints) -> {
                    int totalPoints = monthlyPoints.values().stream().mapToInt(Integer::intValue).sum();
                    List<MonthlyRewardPoints> monthlyRewardPointsList = new ArrayList<>();
                    monthlyPoints.forEach((month, points) -> monthlyRewardPointsList.add(new MonthlyRewardPoints(month, points)));
                    rewardSummaries.add(new RewardSummary(customerId, totalPoints, monthlyRewardPointsList));
                });


                // Add reward summaries to model
                model.addAttribute("rewardSummaries", rewardSummaries);

                // Return the view
                return "reward";
            } catch (URISyntaxException | IOException e) {
                return "error";
            }
        }
    }



