package com.example.jb_products_info.services;

import com.example.jb_products_info.dto.BuildInfoDTO;
import com.example.jb_products_info.dto.ProductInfoDTO;
import com.example.jb_products_info.entities.Build;
import com.example.jb_products_info.repositories.BuildRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InfoService {

    private final BuildRepository buildRepository;

    public InfoService(BuildRepository buildRepository) {
        this.buildRepository = buildRepository;
    }

    public List<BuildInfoDTO> getBuildInfoList(String productCode) {
        List<Build> builds = buildRepository.findAllByProductCode(productCode);
        return builds.stream()
                .map(InfoService::convertToBuildInfoDTO)
                .collect(Collectors.toList());
    }

    public BuildInfoDTO getBuildInfoBy(String productCode,
                                       String buildNumber) {
        Build build = buildRepository.findFirstByProductCodeAndBuildNumberStartsWith(productCode, buildNumber);
        return build == null ? null : convertToBuildInfoDTO(build);
    }

    private static BuildInfoDTO convertToBuildInfoDTO(Build build) {
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
}
