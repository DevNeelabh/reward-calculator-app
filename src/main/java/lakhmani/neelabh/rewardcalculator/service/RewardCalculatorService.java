package lakhmani.neelabh.rewardcalculator.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class RewardCalculatorService {

    public int calculateRewardPoints(double amount) {
        int points = 0;
        if (amount > 100) {
            points += 2 * (amount - 100);
        }else if (amount > 50) {
            points += amount - 50;
        }
        return points;
    }
}
