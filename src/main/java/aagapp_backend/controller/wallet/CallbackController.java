package aagapp_backend.controller.wallet;

import aagapp_backend.entity.withdrawrequest.CustomerWithdrawalRequest;
import aagapp_backend.enums.WithdrawalStatus;
import aagapp_backend.repository.withdrawrequest.CustomerWithdrawalRequestRepository;
import aagapp_backend.services.wallet.WalletService;
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

    @Autowired
    private WalletService walletService;

    @PostMapping("/payout-callback")
    public ResponseEntity<String> handleKwickPayCallback(@RequestParam Map<String, String> params) {

        String status = params.get("status");
        String clientTxnId = params.get("clientid");
        String gatewayTxnId = params.get("txnid");
        String payId = params.get("payId");
        String amount = params.get("orderAmount");

        CustomerWithdrawalRequest withdrawal = customerWithdrawalRequestRepository.findByClientId(clientTxnId);

        if (withdrawal == null) {
            return ResponseEntity.badRequest().body("Transaction not found");
        }

        if (withdrawal.getStatus() == WithdrawalStatus.PAID) {
            return ResponseEntity.ok("Already processed");
        }

        switch (status.toLowerCase()) {
            case "success":
                withdrawal.setStatus(WithdrawalStatus.PAID);
                withdrawal.setPayId(payId);
                withdrawal.setTxnId(gatewayTxnId);
                withdrawal.setGatewayMessage("Payment successful");
                break;
            case "failed":
                withdrawal.setStatus(WithdrawalStatus.FAILED);
                withdrawal.setTxnId(gatewayTxnId);
                withdrawal.setPayId(payId);
                walletService.refundAmountToWallet(clientTxnId, Float.parseFloat(amount));
                withdrawal.setGatewayMessage("Payment failed");

                break;
            default:
                withdrawal.setStatus(WithdrawalStatus.PENDING);
                withdrawal.setTxnId(gatewayTxnId);
                withdrawal.setPayId(payId);
                withdrawal.setGatewayMessage("Payment pending");
                break;
        }
        customerWithdrawalRequestRepository.save(withdrawal);

        return ResponseEntity.ok("Callback processed");
    }
}
