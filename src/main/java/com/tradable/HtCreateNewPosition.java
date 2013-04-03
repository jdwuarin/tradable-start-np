package com.tradable;

import javax.swing.JComponent;
import javax.swing.JPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.tradable.api.component.WorkspaceModule;
import com.tradable.api.component.state.PersistedStateHolder;
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

public class HtCreateNewPosition extends JPanel implements WorkspaceModule, TradingRequestListener{


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final String TITLE = "Create a new Position";

	private static final Logger logger = LoggerFactory.getLogger(HtCreateNewPosition.class);

	private TradingRequestExecutor _executor;

	private int commandIdSeed;
	
	
	
	public HtCreateNewPosition(TradingRequestExecutor executor) {
		
		setLayout(null);
		setSize(250, 250);
		
		_executor = executor;
		
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
			_executor.execute(request, this);
		} catch (RuntimeException ex) {
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
	 



	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public PersistedStateHolder getPersistedState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JComponent getVisualComponent() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public void loadPersistedState(PersistedStateHolder arg0) {
		// TODO Auto-generated method stub
		
	}

}
