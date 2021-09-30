package org.krish.microservices.saga.orderservice.saga;

import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.CommandResultMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.spring.stereotype.Saga;
import org.krish.microservices.saga.core.commands.ReserveProductCommand;
import org.krish.microservices.saga.core.events.ProductReservedEvent;
import org.krish.microservices.saga.core.model.User;
import org.krish.microservices.saga.core.query.FetchUserPaymentDetailsQuery;
import org.krish.microservices.saga.orderservice.core.events.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Saga
public class OrderSaga {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(OrderSaga.class);

	@Autowired
	private CommandGateway commandGateway;
	
	@Autowired
	private QueryGateway queryGateway;

	@StartSaga
	@SagaEventHandler(associationProperty = "orderId")
	public void handle(OrderCreatedEvent event) {
		
		ReserveProductCommand command = ReserveProductCommand.builder().orderId(event.getOrderId())
				.productId(event.getProductId())
				.quantity(event.getQuantity())
				.userId(event.getUserId())
				.build();
		
		LOGGER.info("OrderCreatedEvent handled for orderId: " + command.getOrderId() + 
				" and productId: " + command.getProductId() );
		
		commandGateway.send(command, new CommandCallback<ReserveProductCommand, Object>() {

			@Override
			public void onResult(CommandMessage<? extends ReserveProductCommand> commandMessage,
					CommandResultMessage<? extends Object> commandResultMessage) {
				   if(commandResultMessage.isExceptional()) {
					   // Start a compensating transaction
//						RejectOrderCommand rejectOrderCommand = new RejectOrderCommand(orderCreatedEvent.getOrderId(),
//								commandResultMessage.exceptionResult().getMessage());
//						
//						commandGateway.send(rejectOrderCommand);
				   }
			}
		});
	}
	
	@SagaEventHandler(associationProperty = "orderId")
	public void handle(ProductReservedEvent event) {
		LOGGER.info("ProductReservedEvent is called for productId: "+ event.getProductId() + 
        		" and orderId: " + event.getOrderId());
		
		FetchUserPaymentDetailsQuery query = new FetchUserPaymentDetailsQuery();
		query.setUserId(event.getUserId());
		User user = null;
		
		try {
			user = queryGateway.query(query, ResponseTypes.instanceOf(User.class)).join();
		}
		catch(Exception e) {
			//compensating trnx
		}
		
		if(user == null) {
			//compensating trnx
		}
		
		LOGGER.info("Payment Details fetched successfully for user "+user.getFirstName());
	}

}
