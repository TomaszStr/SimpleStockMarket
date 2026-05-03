package io.tomaszstr.simplestockmarket.bank;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/stocks")
@RequiredArgsConstructor
public class BankController {

    private final BankService bankService;

    @GetMapping
    public ResponseEntity<BankStateResponse> getBankState() {
        log.info("Received GET request for bank state");
        return ResponseEntity.ok(bankService.getBankState());
    }

    @PostMapping
    public ResponseEntity<Void> setBankState(@Valid @RequestBody BankStateRequest request) {
        log.info("Received POST request to update bank state");
        bankService.setBankState(request);
        return ResponseEntity.ok().build();
    }
}