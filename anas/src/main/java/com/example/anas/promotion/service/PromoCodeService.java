package com.example.anas.promotion.service;

import com.example.anas.promotion.entity.PromoCode;
import com.example.anas.promotion.repository.PromoCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PromoCodeService {
    private final PromoCodeRepository promoCodeRepository;

    public Optional<PromoCode> findByCode(String code) {
        return promoCodeRepository.findByCodeAndIsActiveTrue(code);
    }

    public boolean validatePromoCode(String code, BigDecimal orderAmount) {
        Optional<PromoCode> promoCodeOpt = promoCodeRepository.findByCodeAndIsActiveTrue(code);
        if (promoCodeOpt.isEmpty()) {
            return false;
        }

        PromoCode promoCode = promoCodeOpt.get();

        // Проверка активности
        if (!promoCode.getIsActive()) {
            return false;
        }

        // Проверка дат
        LocalDateTime now = LocalDateTime.now();
        if (promoCode.getValidFrom() != null && now.isBefore(promoCode.getValidFrom())) {
            return false;
        }
        if (promoCode.getValidTo() != null && now.isAfter(promoCode.getValidTo())) {
            return false;
        }

        // Проверка минимальной суммы заказа
        if (promoCode.getMinOrderAmount() != null && 
            orderAmount.compareTo(promoCode.getMinOrderAmount()) < 0) {
            return false;
        }

        // Проверка максимального количества использований
        if (promoCode.getMaxUses() != null && 
            promoCode.getCurrentUses() >= promoCode.getMaxUses()) {
            return false;
        }

        return true;
    }

    public BigDecimal calculateDiscount(String code, BigDecimal orderAmount) {
        Optional<PromoCode> promoCodeOpt = promoCodeRepository.findByCodeAndIsActiveTrue(code);
        if (promoCodeOpt.isEmpty() || !validatePromoCode(code, orderAmount)) {
            return BigDecimal.ZERO;
        }

        PromoCode promoCode = promoCodeOpt.get();
        BigDecimal discount = BigDecimal.ZERO;

        if (promoCode.getDiscountPercent() != null && promoCode.getDiscountPercent() > 0) {
            discount = orderAmount.multiply(BigDecimal.valueOf(promoCode.getDiscountPercent()))
                    .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        } else if (promoCode.getDiscountAmount() != null) {
            discount = promoCode.getDiscountAmount();
            if (discount.compareTo(orderAmount) > 0) {
                discount = orderAmount;
            }
        }

        return discount;
    }

    @Transactional
    public void incrementUsage(String code) {
        Optional<PromoCode> promoCodeOpt = promoCodeRepository.findByCode(code);
        if (promoCodeOpt.isPresent()) {
            PromoCode promoCode = promoCodeOpt.get();
            promoCode.setCurrentUses(promoCode.getCurrentUses() + 1);
            promoCodeRepository.save(promoCode);
        }
    }

    @Transactional
    public PromoCode savePromoCode(PromoCode promoCode) {
        return promoCodeRepository.save(promoCode);
    }

    public java.util.List<PromoCode> getAllPromoCodes() {
        return promoCodeRepository.findAll();
    }

    public Optional<PromoCode> getPromoCodeById(Long id) {
        return promoCodeRepository.findById(id);
    }

    @Transactional
    public void deletePromoCode(Long id) {
        promoCodeRepository.deleteById(id);
    }

    @Transactional
    public void togglePromoCode(Long id) {
        Optional<PromoCode> promoCodeOpt = promoCodeRepository.findById(id);
        if (promoCodeOpt.isPresent()) {
            PromoCode promoCode = promoCodeOpt.get();
            promoCode.setIsActive(!promoCode.getIsActive());
            promoCodeRepository.save(promoCode);
        }
    }
}
