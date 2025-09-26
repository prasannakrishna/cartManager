package com.bhagwat.retail.cart.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProspectusDto {
    private String productId;
    private Long totalEstimatedQuantity;
}
