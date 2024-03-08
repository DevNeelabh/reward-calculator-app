package lakhmani.neelabh.rewardcalculator.model;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Transaction {
    private Long id;
    private Long customerId;
    private String month;
    private double amount;
}
