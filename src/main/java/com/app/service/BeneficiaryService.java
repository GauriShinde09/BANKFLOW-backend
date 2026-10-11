package com.app.service;

import java.util.List;

import com.app.dto.AddBeneficiaryDto;
import com.app.dto.BeneficiaryResponseDto;
import com.app.entity.Beneficiary;

public interface BeneficiaryService {
	BeneficiaryResponseDto addBeneficiary(
	            String username,
	            AddBeneficiaryDto dto
	    );

	    List<BeneficiaryResponseDto> getMyBeneficiaries(String username);
}
