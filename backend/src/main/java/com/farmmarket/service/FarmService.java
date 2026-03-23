package com.farmmarket.service;

import com.farmmarket.dto.request.CreateFarmRequest;
import com.farmmarket.dto.response.FarmResponse;
import com.farmmarket.entity.Farm;
import com.farmmarket.entity.User;
import com.farmmarket.exception.BadRequestException;
import com.farmmarket.exception.ResourceNotFoundException;
import com.farmmarket.mapper.FarmMapper;
import com.farmmarket.repository.FarmRepository;
import com.farmmarket.repository.ProductRepository;
import com.farmmarket.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FarmService {

    private final FarmRepository farmRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final FarmMapper farmMapper;

    public FarmResponse getFarmById(UUID farmId) {
        Farm farm = farmRepository.findById(farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Farm not found"));
        return farmMapper.toResponse(farm);
    }

    public FarmResponse getFarmByFarmerId(UUID farmerId) {
        Farm farm = farmRepository.findByFarmerId(farmerId).orElse(null);
        if (farm == null) return null;
        return farmMapper.toResponse(farm);
    }

    @Transactional
    public FarmResponse createFarm(UUID farmerId, CreateFarmRequest request) {
        if (farmRepository.existsByFarmerId(farmerId)) {
            throw new BadRequestException("You already have a farm");
        }

        User farmer = userRepository.findById(farmerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Farm farm = Farm.builder()
                .farmer(farmer)
                .farmName(request.getFarmName())
                .description(request.getDescription())
                .farmSizeAcres(request.getFarmSizeAcres())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .zipCode(request.getZipCode())
                .country(request.getCountry())
                .latitude(BigDecimal.valueOf(request.getLatitude()))
                .longitude(BigDecimal.valueOf(request.getLongitude()))
                .isOrganic(request.getIsOrganic())
                .deliveryRadiusKm(request.getDeliveryRadiusKm())
                .minimumOrder(request.getMinimumOrder())
                .build();

        farm = farmRepository.save(farm);
        return farmMapper.toResponse(farm);
    }

    @Transactional
    public FarmResponse updateFarm(UUID farmerId, CreateFarmRequest request) {
        Farm farm = farmRepository.findByFarmerId(farmerId)
                .orElseThrow(() -> new ResourceNotFoundException("Farm not found"));

        if (request.getFarmName() != null) farm.setFarmName(request.getFarmName());
        if (request.getDescription() != null) farm.setDescription(request.getDescription());
        if (request.getCity() != null) farm.setCity(request.getCity());
        if (request.getState() != null) farm.setState(request.getState());
        if (request.getLatitude() != null) farm.setLatitude(BigDecimal.valueOf(request.getLatitude()));
        if (request.getLongitude() != null) farm.setLongitude(BigDecimal.valueOf(request.getLongitude()));
        if (request.getIsOrganic() != null) farm.setIsOrganic(request.getIsOrganic());
        if (request.getDeliveryRadiusKm() != null)
            farm.setDeliveryRadiusKm(request.getDeliveryRadiusKm());

        farm = farmRepository.save(farm);
        return farmMapper.toResponse(farm);
    }

    public List<FarmResponse> getNearbyFarms(
            double lat, double lng, int radiusKm) {
        return farmRepository.findNearbyFarms(lat, lng, radiusKm, 50)
                .stream()
                .map(farmMapper::toResponse)
                .collect(Collectors.toList());
    }
}