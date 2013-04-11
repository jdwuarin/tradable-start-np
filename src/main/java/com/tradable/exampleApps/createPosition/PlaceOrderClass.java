package com.tradable.exampleApps.createPosition;

import org.slf4j.Logger;

import com.tradable.api.entities.Instrument;
import com.tradable.api.entities.Order;
import com.tradable.api.entities.OrderDuration;
import com.tradable.api.entities.OrderSide;
import com.tradable.api.entities.OrderType;
import com.tradable.api.entities.Position;
import com.tradable.api.services.executor.ModifyOrderAction;
import com.tradable.api.services.executor.ModifyOrderActionBuilder;
import com.tradable.api.services.executor.OrderActionRequest;
import com.tradable.api.services.executor.PlaceOrderAction;
import com.tradable.api.services.executor.PlaceOrderActionBuilder;
import com.tradable.api.services.executor.TradingRequest;
import com.tradable.api.services.executor.TradingRequestExecutor;
import com.tradable.api.services.executor.TradingRequestListener;
import com.tradable.api.services.executor.TradingResponse;
import com.tradable.api.services.executor.groups.CreateOCOGroupRequestBuilder;
import com.tradable.api.services.executor.groups.OrderGroupRequest;

public class PlaceOrderClass implements TradingRequestListener{
	
	//========================================(3)========================================//
	//The TradingRequestExecutor object is in charge of executing the trades once all the 
	//settings pertaining to it have been set. The commandIdSeed is just a number that allows
	//us to track internally the number of the command we are at. This is usde in the log.
	//==================================================================================
	private TradingRequestExecutor executor;
	private int commandIdSeed;
	//==================================================================================	
	//==================================================================================
	
	private Logger logger;
	private int accountId;
	
	
	public PlaceOrderClass(TradingRequestExecutor executor, Logger logger){

		//========================================(3)========================================//
		this.executor = executor; //no need to add listeners as this object takes action.
		
		this.logger = logger;
	}
	
	public void setAccountId(int accountId){
		this.accountId = accountId;
	}

	public void placeOrder(Instrument instrument, OrderSide orderSide, OrderDuration orderDuration, 
			OrderType orderType, Double quantity){
		placeOrder(instrument, orderSide, orderDuration, orderType, quantity, 0.0);
	}
	
	public void placeOrder(Instrument instrument, OrderSide orderSide, OrderDuration orderDuration, 
			OrderType orderType, Double quantity, Double limit) {
		
		PlaceOrderActionBuilder orderActionBuilder = new PlaceOrderActionBuilder();
		orderActionBuilder.setInstrument(instrument); // instrument object set in constructor
		orderActionBuilder.setOrderSide(orderSide);
		orderActionBuilder.setDuration(orderDuration);
		orderActionBuilder.setOrderType(orderType); //so as to have it work or pend for a while
		if(orderType == OrderType.LIMIT && limit > 0.0)
			orderActionBuilder.setLimitPrice(limit);
		else if(orderType == OrderType.LIMIT && limit == 0.0) //missing limit price. 
			return; //do nothing
		else if (orderType == OrderType.MARKET && limit != 0.0) //setting limit to market order
			return; //do nothing
		else{}
		

		orderActionBuilder.setQuantity(quantity);
		PlaceOrderAction orderAction = orderActionBuilder.build();
		
		OrderActionRequest request = new OrderActionRequest(accountId, orderAction); 
		

		logger.info("Executing command: {}", ++commandIdSeed);

		try {
			executor.execute(request, this);
		} 
		
		catch (Exception ex) {
			logger.error("Failed to submit command: {}", commandIdSeed, ex);
		}	
	}	
	
	
	//==================================================================================//
	//Beware when using the ModifyOrderAction classes, as they main contain bugs and thus 
	//their behavior can be somewhat unexpected although it will seem to work most of the
	//time when the market is open.
	//==================================================================================//
	public void modifyOrder(Order orderToModify, OrderDuration orderDuration, Double quantity){
		
		
		ModifyOrderActionBuilder orderActionBuilder = new ModifyOrderActionBuilder(orderToModify);
		orderActionBuilder.setOrderType(OrderType.LIMIT);
		orderActionBuilder.setLimitPrice(1.01* HtCreateNewPosition.ask.getPrice()); //setting ask price to get filled almost surely
	    orderActionBuilder.setDuration(orderDuration);
	    orderActionBuilder.setQuantity(quantity);

	    ModifyOrderAction modOrderAction = orderActionBuilder.build();

	    OrderActionRequest modRequest = new OrderActionRequest(accountId, modOrderAction);
		
	    logger.info("Executing command: {}", ++commandIdSeed);

	    try {
	    	executor.execute(modRequest, this);
	    } 
	    catch (RuntimeException  ex) {
	    	logger.error("Failed to submit command: {}", commandIdSeed, ex);
	    }
		
	}
	
	
	public void OCOOrder(Position oCOPosition, Double stopLossPrice, Double takeProfitPrice){
			
		CreateOCOGroupRequestBuilder OCOBuilder = new CreateOCOGroupRequestBuilder();

		if (oCOPosition == null) {return;}
		
		//We then have to set the position to the builder.
		//Quantity is set to 3000 too. Could be different.
		//setStopLoss sets the price we want to set our stop loss to.
		//This sets the price of the STOP order. We also specify here 
		//that this is a SELL order (as we are long 3000 EURUSD)
		//setTakeProfit sets the price for the LIMIT order. Our
		//limit order is set slightly higher than the current price so 
		//that we would actually take a profit is the price was hit.
		OCOBuilder.setPosition(oCOPosition);
		OCOBuilder.setQuantity(3000.0);
		OCOBuilder.setRequestId(++commandIdSeed);
		OCOBuilder.setStopLoss(stopLossPrice, OrderSide.SELL);
		OCOBuilder.setTakeProfit(takeProfitPrice);

		OrderGroupRequest orderGroupRequest = OCOBuilder.build();
		
		
	    logger.info("Executing command: {}", commandIdSeed);

	    try {
	    	executor.execute(orderGroupRequest, this);
	    } 
	    catch (RuntimeException  ex) {
	    	logger.error("Failed to submit command: {}", commandIdSeed, ex);
	    }
		
	}

	@Override
	public void requestExecuted(TradingRequestExecutor executor, TradingRequest request, TradingResponse response) {
		if (response.isSuccess())
			logger.info("Command is successfully executed: {}", request.getId());
					
		else 
			logger.error("Command is failed to execute: {}", request.getId(), response.getCause());
		
	}

}
