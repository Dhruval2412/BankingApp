package com.example.bankapp.controller;

import com.example.bankapp.model.Account;
import com.example.bankapp.model.Transaction;
import com.example.bankapp.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Controller
public class BankController {
    @Autowired
    private AccountService accountService;

    @GetMapping("/dashboard")
    public String dashboard(Model model){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Account account = accountService.findAccountByUsername(username);
        model.addAttribute("account",account);
        return "dashboard";
    }

    @GetMapping("/register")
    public String showRegistrationFrom(){
        return "register";
    }

    @PostMapping("/register")
    public String registerAccount(@RequestParam String username ,@RequestParam String password, Model model ){
        try{
            accountService.registerAccount(username,password);
            return "redirect:/login";
        }catch (RuntimeException e){
            model.addAttribute("Error",e.getMessage());
            return "register";
        }
    }
    @GetMapping("/login")
    public String login(){
        return "login";
    }

    @PostMapping("/deposit")
    public String deposit(@RequestParam BigDecimal amount){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Account account = accountService.findAccountByUsername(username);
        accountService.deposit(account,amount);
        return "redirect:/dashboard";
    }
    @PostMapping("/withdraw")
    public String withdraw(@RequestParam BigDecimal amount,Model model){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Account account = accountService.findAccountByUsername(username);

        try{
            accountService.withdraw(account,amount);
        }catch (RuntimeException e){
            model.addAttribute("Error",e.getMessage());
            model.addAttribute("account",account);
            return "dashboard";
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/transactions")
    public String transactionHistory(Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Account account = accountService.findAccountByUsername(username);

        if (account == null) {
            model.addAttribute("Error", "Account not found.");
            return "dashboard"; // or redirect to an error page
        }

        model.addAttribute("transactions", accountService.getTransactionHistory(account));
        return "transactions";
    }

    @PostMapping("/transfer")
    public String transferAmount(@RequestParam String toUsername,@RequestParam BigDecimal amount,Model model){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Account fromAccount = accountService.findAccountByUsername(username);

        try{
            accountService.transferAmount(fromAccount,toUsername,amount);

        }catch (RuntimeException e){
            model.addAttribute("Error",e.getMessage());
            model.addAttribute("account",fromAccount);
            return "dashboard";
        }
        return "redirect:/dashboard";
    }
    @GetMapping("/profile")
    public String showProfile(Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Account account = accountService.findAccountByUsername(username);
        model.addAttribute("account", account);
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@RequestParam String username,
                                @RequestParam(required = false) String newPassword,
                                @RequestParam(required = false) String confirmPassword,
                                Model model) {
        try {
            // Check if newPassword and confirmPassword match
            if (newPassword != null && !newPassword.isEmpty()) {
                if (!newPassword.equals(confirmPassword)) {
                    model.addAttribute("error", "Passwords do not match.");
                    return "profile"; // Return to the profile page with an error message
                }
                Account account = accountService.findAccountByUsername(username);
                accountService.updatePassword(account, newPassword); // Call the updatePassword method
                model.addAttribute("success", "Password updated successfully!");
            }
            return "redirect:/profile"; // Redirect back to the profile page
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "profile"; // Return to the profile page with an error message
        }
    }

    @GetMapping("/searchtransactions")
    public String searchTransactionHistory(@RequestParam(required = false) String type,
                                           @RequestParam(required = false) String startDate,
                                           @RequestParam(required = false) String endDate,
                                           Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Account account = accountService.findAccountByUsername(username);

        List<Transaction> transactions;

        // Parse dates and fetch transactions based on the provided filters
        if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate start = LocalDate.parse(startDate, formatter);
            LocalDate end = LocalDate.parse(endDate, formatter);
            transactions = accountService.searchTransactions(account, type, start, end);
        } else {
            transactions = accountService.searchTransactions(account, type, null, null);
        }

        // Add attributes to the model for the view
        model.addAttribute("transactions", transactions);
        model.addAttribute("transactionTypes", Arrays.asList("Deposit", "Withdrawal", "Transfer")); // For dropdown

        return "searchtransaction"; // Return the view name for search transactions
    }
}
