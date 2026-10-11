package com.app.service;

import com.app.dto.LoanTypeRequestDto;
import com.app.dto.LoanTypeResponseDto;
import com.app.entity.LoanType;
import com.app.exception.BadRequestException;
import com.app.exception.ResourceNotFoundException;
import com.app.repo.LoanTypeRepository;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LoanTypeServiceImpl implements LoanTypeService {

    private final LoanTypeRepository loanTypeRepository;
    private final ModelMapper modelMapper;

    public LoanTypeServiceImpl(
            LoanTypeRepository loanTypeRepository,
            ModelMapper modelMapper) {

        this.loanTypeRepository = loanTypeRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public LoanTypeResponseDto createLoanType(
            LoanTypeRequestDto dto) {

        validateAmountRange(dto);

        String name = dto.getName().trim();

        if (loanTypeRepository.findByName(name).isPresent()) {
            throw new BadRequestException(
                    "Loan type already exists"
            );
        }

        LoanType loanType =
                modelMapper.map(dto, LoanType.class);

        loanType.setName(name);

        LoanType savedLoanType =
                loanTypeRepository.save(loanType);

        return modelMapper.map(
                savedLoanType,
                LoanTypeResponseDto.class
        );
    }

    @Override
    public List<LoanTypeResponseDto> getAllLoanTypes() {

        return loanTypeRepository.findAll()
                .stream()
                .map(loanType ->
                        modelMapper.map(
                                loanType,
                                LoanTypeResponseDto.class
                        )
                )
                .toList();
    }

    @Override
    public LoanTypeResponseDto getLoanTypeById(Long id) {

        LoanType loanType = findLoanType(id);

        return modelMapper.map(
                loanType,
                LoanTypeResponseDto.class
        );
    }

    // Update a loan product's details
    @Transactional
    @Override
    public LoanTypeResponseDto updateLoanType(
            Long id,
            LoanTypeRequestDto dto) {

        validateAmountRange(dto);

        LoanType loanType = findLoanType(id);
        String name = dto.getName().trim();

        // Prevent duplicate names, except the current product's name
        loanTypeRepository.findByName(name)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BadRequestException(
                            "Loan type already exists"
                    );
                });

        // Update editable fields
        loanType.setName(name);
        loanType.setDescription(dto.getDescription().trim());
        loanType.setInterestRate(dto.getInterestRate());
        loanType.setMinAmount(dto.getMinAmount());
        loanType.setMaxAmount(dto.getMaxAmount());
        loanType.setMaxTenureMonths(dto.getMaxTenureMonths());

        // Preserve the existing active status
        LoanType updatedLoanType =
                loanTypeRepository.save(loanType);

        return modelMapper.map(
                updatedLoanType,
                LoanTypeResponseDto.class
        );
    }

    // Activate or deactivate a loan product
    @Transactional
    @Override
    public LoanTypeResponseDto setLoanTypeActive(
            Long id,
            boolean active) {

        LoanType loanType = findLoanType(id);

        loanType.setActive(active);

        LoanType updatedLoanType =
                loanTypeRepository.save(loanType);

        return modelMapper.map(
                updatedLoanType,
                LoanTypeResponseDto.class
        );
    }

    private LoanType findLoanType(Long id) {

        return loanTypeRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Loan type not found with id: " + id
                        )
                );
    }

    private void validateAmountRange(
            LoanTypeRequestDto dto) {

        if (dto.getMinAmount().compareTo(
                dto.getMaxAmount()) >= 0) {

            throw new BadRequestException(
                    "Minimum amount must be less than maximum amount"
            );
        }
    }
}