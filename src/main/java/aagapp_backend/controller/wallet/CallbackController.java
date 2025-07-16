package aagapp_backend.controller.wallet;

import aagapp_backend.entity.withdrawrequest.CustomerWithdrawalRequest;
import aagapp_backend.enums.WithdrawalStatus;
import aagapp_backend.repository.withdrawrequest.CustomerWithdrawalRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/payment")
public class CallbackController {

    @Autowired
    private CustomerWithdrawalRequestRepository customerWithdrawalRequestRepository;

    @PostMapping("/payout-callback")
    public ResponseEntity<String> handleKwickPayCallback(@RequestParam Map<String, String> params) {

        String status = params.get("status");
        String clientTxnId = params.get("clientid");
        String gatewayTxnId = params.get("txnid");
        String payId = params.get("payId");
        String amount = params.get("amount");


        // 1. Fetch withdrawal by clientTxnId
        CustomerWithdrawalRequest withdrawal = customerWithdrawalRequestRepository.findByGatewayTxnId(clientTxnId);

        if (withdrawal == null) {
            return ResponseEntity.badRequest().body("Transaction not found");
        }

        // 2. Already paid?
        if (withdrawal.getStatus() == WithdrawalStatus.PAID) {
            return ResponseEntity.ok("Already processed");
        }

        // 3. Update status
        switch (status.toLowerCase()) {
            case "success":
                withdrawal.setStatus(WithdrawalStatus.PAID);
                System.out.println("Gateway Txn ID: " + gatewayTxnId);
                System.out.println("Success................");
                break;
            case "failed":
                withdrawal.setStatus(WithdrawalStatus.FAILED);
                // Optional: refund to wallet here
                System.out.println("failed................");
                break;
            default:
                withdrawal.setStatus(WithdrawalStatus.PENDING);
                System.out.println("Pending................");
                break;
        }

        withdrawal.setGatewayMessage("Webhook update");
        customerWithdrawalRequestRepository.save(withdrawal);

        return ResponseEntity.ok("Callback processed");
    }
}
