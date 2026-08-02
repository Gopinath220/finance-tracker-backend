package com.tcsproject.finance_tracker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.tcsproject.finance_tracker.entity.Transaction;
import com.tcsproject.finance_tracker.entity.User;
import com.tcsproject.finance_tracker.repository.TransactionRepository;
import com.tcsproject.finance_tracker.repository.UserRepository;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = {"http://localhost:5173", "https://gopinath220.github.io"})
public class TransactionController {

    @Autowired 
    private TransactionRepository transactionRepository;
    
    @Autowired 
    private UserRepository userRepository;

    @GetMapping
    public List<Transaction> getAllTransactions(Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Authenticated user context not found"));
        return transactionRepository.findByUserId(user.getId());
    }
     
    @PostMapping
    public Transaction createTransaction(@RequestBody Transaction transaction, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Authenticated user context not found"));
        transaction.setUser(user);
        return transactionRepository.save(transaction);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTransaction(@PathVariable Long id, Principal principal) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction record not found"));
                
        if (!transaction.getUser().getUsername().equals(principal.getName())) {
            return ResponseEntity.status(403).body("Access Denied: You do not own this record");
        }
        
        transactionRepository.delete(transaction);
        return ResponseEntity.ok("Transaction successfully deleted");
    }
    
    @GetMapping("/export")
    public ResponseEntity<Resource> exportToCSV(
            @RequestParam("month") Integer month,
            @RequestParam("year") Integer year,
            Principal principal) {
        
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Authenticated user context not found"));
                
        List<Transaction> transactions = transactionRepository.findByUserId(user.getId());

        List<Transaction> monthlyTransactions = transactions.stream()
            .filter(t -> t.getDate() != null && 
                         t.getDate().getMonthValue() == month && 
                         t.getDate().getYear() == year)
            .collect(Collectors.toList());

        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append("ID,Description,Amount,Type,Category,Date\n");
        for (Transaction t : monthlyTransactions) {
            csvBuilder.append(String.format("%d,%s,%.2f,%s,%s,%s\n", 
                t.getId(), 
                t.getDescription().replace(",", " "),
                t.getAmount(), 
                t.getType(), 
                t.getCategory(), 
                t.getDate()
            ));
        }

        byte[] csvBytes = csvBuilder.toString().getBytes();
        ByteArrayResource resource = new ByteArrayResource(csvBytes);

        String filename = String.format("expenses_%d_%02d.csv", year, month);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(csvBytes.length)
                .body(resource);
    }
}
