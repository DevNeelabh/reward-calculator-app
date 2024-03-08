package lakhmani.neelabh.rewardcalculator.dto;

import java.util.List;

public record RewardSummary(Long customerId,
                            int totalRewardPoints,
                            List<MonthlyRewardPoints> monthlyRewardPoints) {}


