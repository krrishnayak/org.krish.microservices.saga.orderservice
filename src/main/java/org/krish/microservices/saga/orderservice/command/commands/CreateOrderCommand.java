/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.krish.microservices.saga.orderservice.command.commands;

import lombok.Builder;
import lombok.Data;
import org.axonframework.modelling.command.TargetAggregateIdentifier;
import org.krish.microservices.saga.orderservice.core.model.OrderStatus;

@Builder
@Data
public class CreateOrderCommand {
        
    @TargetAggregateIdentifier
    public final String orderId;
    
    private final String userId;
    private final String productId;
    private final int quantity;
    private final String addressId; 
    private final OrderStatus orderStatus;
}
