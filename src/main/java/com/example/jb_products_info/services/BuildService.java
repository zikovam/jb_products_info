package com.example.jb_products_info.services;

import com.example.jb_products_info.dto.BuildInfoDTO;
import com.example.jb_products_info.dto.ProductInfoDTO;
import com.example.jb_products_info.entities.Build;
import com.example.jb_products_info.repositories.BuildRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BuildService {
    private final BuildRepository buildRepository;

    public BuildService(BuildRepository buildRepository) {
        this.buildRepository = buildRepository;
    }

    public BuildInfoDTO convertToBuildInfoDTO(Build build) {
        String buildNumber = build.getBuildNumber();
        if (build.getProductInfoJsonData() != null) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                ProductInfoDTO productInfoDTO = mapper.readValue(build.getProductInfoJsonData(), ProductInfoDTO.class);
                return new BuildInfoDTO(buildNumber, productInfoDTO);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new BuildInfoDTO(buildNumber, null);
    }

    @Transactional
    public List<Build> findAllByProductCode(String productCode) {
        return buildRepository.findAllByProductCode(productCode);
    }

    @Transactional
    public Build findFirstByProductCodeAndBuildNumberStartsWith(String productCode, String buildNumber) {
        return buildRepository.findFirstByProductCodeAndBuildNumberStartsWith(productCode, buildNumber);
    }

    @Transactional
    public List<Build> findAll() {
        return buildRepository.findAll();
    }

}
