package com.tradable;

import org.springframework.beans.factory.annotation.Autowired;

import com.tradable.api.component.WorkspaceModule;
import com.tradable.api.component.WorkspaceModuleCategory;
import com.tradable.api.component.WorkspaceModuleFactory;
import com.tradable.api.services.account.CurrentAccountService;
import com.tradable.api.services.executor.TradingRequestExecutor;
import com.tradable.api.services.instrument.InstrumentService;

public class HtCreateNewPositionFactory implements WorkspaceModuleFactory{

	@Autowired
	private TradingRequestExecutor executor;  //create a trading request executor in 
	
	@Autowired
	CurrentAccountService accountSubscriptionService;
	
	@Autowired
	InstrumentService instrumentService;
	
	@Autowired
	public void setExecutor(TradingRequestExecutor executor) {
		this.executor = executor;		
	}	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	@Override
	public WorkspaceModule createModule() {
		// TODO Auto-generated method stub
		return new HtCreateNewPosition(executor, accountSubscriptionService, instrumentService);
	}

	@Override
	public WorkspaceModuleCategory getCategory() {
		// TODO Auto-generated method stub
		return WorkspaceModuleCategory.MISCELLANEOUS;
	}

	@Override
	public String getDisplayName() {
		// TODO Auto-generated method stub
		return "How-To Create New Position";
	}

	@Override
	public String getFactoryId() {
		// TODO Auto-generated method stub
		return "com.tradable.HtCreateNewPosition";
	}

}



/*
 * package com.tradable.api.samples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.tradable.api.entities.Instrument;
import com.tradable.api.entities.OrderDuration;
import com.tradable.api.entities.OrderSide;
import com.tradable.api.entities.OrderType;
import com.tradable.api.services.executor.OrderAction;
import com.tradable.api.services.executor.OrderActionRequest;
import com.tradable.api.services.executor.PlaceOrderActionBuilder;
import com.tradable.api.services.executor.TradingRequest;
import com.tradable.api.services.executor.TradingRequestExecutor;
import com.tradable.api.services.executor.TradingRequestListener;
import com.tradable.api.services.executor.TradingResponse;

 public class OrderActionRequestSample implements TradingRequestListener {

  private static final Logger logger = LoggerFactory.getLogger(OrderActionRequestSample.class);

  private TradingRequestExecutor executor;

  private int commandIdSeed;

  @Autowired
  public void setExecutor(TradingRequestExecutor executor) {
      this.executor = executor;
  }

  public void placeMarketOrder(Integer accountId, Instrument instrument, OrderSide orderSide, OrderDuration orderDuration, Double quantity) {
      PlaceOrderActionBuilder orderActionBuilder = new PlaceOrderActionBuilder();
      orderActionBuilder.setInstrument(instrument);
      orderActionBuilder.setOrderSide(orderSide);
      orderActionBuilder.setDuration(orderDuration);
      orderActionBuilder.setOrderType(OrderType.MARKET);
      orderActionBuilder.setQuantity(quantity);

      OrderAction orderAction = orderActionBuilder.build();

      OrderActionRequest request = new OrderActionRequest(++commandIdSeed, accountId, orderAction);

      logger.info("Executing command: {}", commandIdSeed);

      try {
          executor.execute(request, this);
      } catch (RuntimeException ex) {
          logger.error("Failed to submit command: {}", commandIdSeed, ex);
      }
  }

  @Override
  public void requestExecuted(TradingRequestExecutor executor, TradingRequest request, TradingResponse response) {
      if (response.isSuccess()) {
          logger.info("Command is successfully executed: {}", request.getId());
      } else {
          logger.error("Command is failed to execute: {}", request.getId(), response.getCause());
      }
  }
 }
 */
