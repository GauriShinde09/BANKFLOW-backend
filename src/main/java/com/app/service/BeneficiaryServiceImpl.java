package com.app.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.dto.AddBeneficiaryDto;
import com.app.dto.BeneficiaryResponseDto;
import com.app.entity.BankAccount;
import com.app.entity.Bank_User;
import com.app.entity.Beneficiary;
import com.app.exception.AccountNotFoundException;
import com.app.exception.DuplicateBeneficiaryException;
import com.app.exception.SameAccountTransferException;
import com.app.repo.BankAccountRepo;
import com.app.repo.Bank_UserRepo;
import com.app.repo.BeneficiaryRepo;

@Service
public class BeneficiaryServiceImpl implements BeneficiaryService {

    @Autowired
    private BeneficiaryRepo beneficiaryRepo;

    @Autowired
    private Bank_UserRepo userRepo;
    @Autowired
    BankAccountRepo bankAccountRepo;

    @Override
    public BeneficiaryResponseDto addBeneficiary(
            String username,
            AddBeneficiaryDto dto) {

        // Find logged-in user's account
        BankAccount myAccount = bankAccountRepo
                .findByUserUsername(username)
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Your bank account not found"));
        Optional<Beneficiary> existingBeneficiary =
                beneficiaryRepo.findByUserUsernameAndAccountNumber(
                        username,
                        dto.getAccountNumber()
                );

        if (existingBeneficiary.isPresent()) {
        	throw new DuplicateBeneficiaryException(
        	        "Beneficiary already exists"
        	);
        }
        // Find receiver's account
        BankAccount receiverAccount = bankAccountRepo
                .findByAccountNumber(dto.getAccountNumber())
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Receiver account not found"));

        // Prevent saving own account
        if (myAccount.getId().equals(receiverAccount.getId())) {
            throw new SameAccountTransferException(
                    "You cannot add your own account as beneficiary");
        }

        // Create beneficiary
        Beneficiary beneficiary = new Beneficiary();

        beneficiary.setBeneficiaryName(dto.getBeneficiaryName());
        beneficiary.setAccountNumber(
                receiverAccount.getAccountNumber());
        beneficiary.setUser(myAccount.getUser());

        Beneficiary savedBeneficiary =
                beneficiaryRepo.save(beneficiary);

        // Convert Entity → DTO
        BeneficiaryResponseDto response =
                new BeneficiaryResponseDto();

        response.setId(savedBeneficiary.getId());
        response.setBeneficiaryName(
                savedBeneficiary.getBeneficiaryName());
        response.setAccountNumber(
                savedBeneficiary.getAccountNumber());

        return response;
    }

    @Override
    public List<BeneficiaryResponseDto> getMyBeneficiaries(String username) {

    	List<Beneficiary> beneficiaries =
                beneficiaryRepo.findByUserUsername(username);

        return beneficiaries.stream()
                .map(beneficiary -> {

                    BeneficiaryResponseDto response =
                            new BeneficiaryResponseDto();

                    response.setId(beneficiary.getId());
                    response.setBeneficiaryName(
                            beneficiary.getBeneficiaryName());
                    response.setAccountNumber(
                            beneficiary.getAccountNumber());

                    return response;
                })
                .toList();
    }
}