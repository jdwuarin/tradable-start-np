package com.tradable;

import java.util.ArrayList; //added in order to get the Orders
import java.util.List; //not the awt //added in order to get the Orders
import java.util.Map; //added in order to get the Orders
import java.util.Map.Entry; //added in order to get the Orders

import javax.annotation.PostConstruct; //added in order to get the Orders

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.tradable.api.component.WorkspaceModule;
import com.tradable.api.component.WorkspaceModuleProperties;
import com.tradable.api.component.state.PersistedStateHolder;
import com.tradable.api.entities.Instrument;
import com.tradable.api.entities.OrderDuration;
import com.tradable.api.entities.OrderSide;
import com.tradable.api.entities.OrderStatus;
import com.tradable.api.entities.OrderType;
import com.tradable.api.entities.Account; //added in order to get the Orders
import com.tradable.api.entities.Order; //added in order to get the Orders
import com.tradable.api.services.account.AccountUpdateEvent;
import com.tradable.api.services.account.CurrentAccountService; //added in order to get the Orders
import com.tradable.api.services.account.CurrentAccountServiceListener; //added in order to get the Orders
import com.tradable.api.services.executor.ModifyOrderActionBuilder;
import com.tradable.api.services.executor.OrderAction;
import com.tradable.api.services.executor.OrderActionRequest;
import com.tradable.api.services.executor.PlaceOrderActionBuilder;
import com.tradable.api.services.executor.TradingRequest;
import com.tradable.api.services.executor.TradingRequestExecutor;
import com.tradable.api.services.executor.TradingRequestListener;
import com.tradable.api.services.executor.TradingResponse;
import com.tradable.api.services.instrument.InstrumentService; //added in order to get the Orders
import com.tradable.api.services.instrument.InstrumentServiceListener; //added in order to get the Orders
import com.tradable.api.services.instrument.InstrumentUpdateEvent; //added in order to get the Orders

import java.lang.String;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


public class HtCreateNewPosition extends JPanel implements WorkspaceModule, TradingRequestListener, CurrentAccountServiceListener, InstrumentServiceListener{


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final String TITLE = "Create a new Position";

	private static final Logger logger = LoggerFactory.getLogger(HtCreateNewPosition.class);

	private TradingRequestExecutor executor;

	private int commandIdSeed;
	
	/**
	 * 
	 * 
	 * @param executor
	 */
	
	
	/*****************************************
	 * all useful to get the Account and instrument objects
	 ****************************************/

	CurrentAccountService accountSubscriptionService;
	Account currentAccount = null;
	private int accountId;
	
	private InstrumentService instrumentService;
	private InstrumentServiceListener instrumentListener;
	private Instrument instrument= null;
	private Double quantity = 100000.0;
	Double limit = 0.0;
	
	
	/**************************************
	 **************************************
	 **************************************/
	
	private JTextField txtSometxt;
	
	
	
	public HtCreateNewPosition(TradingRequestExecutor executor, final CurrentAccountService accountSubscriptionService, 
			final InstrumentService instrumentService) {
			
		setLayout(null);
		setSize(250, 250);
		
		txtSometxt = new JTextField();
		txtSometxt.setBounds(42, 192, 164, 19);
		add(txtSometxt);
		txtSometxt.setColumns(10);
		
		putClientProperty(WorkspaceModuleProperties.COMPONENT_TITLE, TITLE);
		
		this.executor = executor;
		this.accountSubscriptionService = accountSubscriptionService;
		this.instrumentService = instrumentService;
		
		this.instrumentService.addListener(this);
		this.accountSubscriptionService.addListener(this);
		
		JButton btnNewButton = new JButton("New button");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				///////////////////////////////////////////////////////////////////////////////////////////////////////////////
				//Now getting the account ID and an instrument objects in order to place a limit order that will be working
				currentAccount = accountSubscriptionService.getCurrentAccount();
				accountId = currentAccount.getAccountId();
				
				
				instrument = instrumentService.getInstrument("EURUSD");
				//instrument = (Instrument) instrumentService.getInstruments().toArray()[0]; //instrument set arbitrarily to the first element in the instrument's list
				quantity = 100000.0d;
				limit = 1.2;
				
				////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				//Now calling the placeLimitOrder member function
				//setting a limit order at the price of 0.0 to sell the first instrument in our list. 
				//The order has until the end of the day to be filled for a quantity of 100'000
				//This order will obvioulsy never be filled as I am trying to BUY for 0 or less.
				placeLimitOrder(instrument, OrderSide.BUY, OrderDuration.DAY, quantity, limit); 
			}
		});
		btnNewButton.setBounds(46, 57, 89, 23);
		add(btnNewButton);
		
	}
	
	public void placeLimitOrder(Instrument instrument, OrderSide orderSide, OrderDuration orderDuration, Double quantity, Double limit) {
		
		PlaceOrderActionBuilder orderActionBuilder = new PlaceOrderActionBuilder();
		orderActionBuilder.setInstrument(instrument); // instrument object set in constructor
		orderActionBuilder.setOrderSide(orderSide);
		orderActionBuilder.setDuration(orderDuration);
		orderActionBuilder.setOrderType(OrderType.LIMIT); //so as to have it work or pend for a while
		orderActionBuilder.setLimitPrice(limit);
			

		orderActionBuilder.setQuantity(quantity);

		OrderAction orderAction = orderActionBuilder.build();

		OrderActionRequest request = new OrderActionRequest(++commandIdSeed, accountId, orderAction); //accoutnId was set in constructor

		logger.info("Executing command: {}", commandIdSeed);

		try {
			executor.execute(request, this);
		} catch (RuntimeException ex) {
			logger.error("Failed to submit command: {}", commandIdSeed, ex);
		}	
	}	
	
	
	
	public void modifyOrder(Order orderToModify, OrderDuration orderDuration, Double quantity){
		
		
		ModifyOrderActionBuilder orderActionBuilder = new ModifyOrderActionBuilder(orderToModify); //
		
	    //orderActionBuilder.setOrderSide(orderSide);
	    orderActionBuilder.setDuration(orderDuration);
	    orderActionBuilder.setOrderType(OrderType.MARKET); //change the order to a market order for it to pass right now
	    orderActionBuilder.setQuantity(quantity);

	    OrderAction orderAction = orderActionBuilder.build();

	    OrderActionRequest request = new OrderActionRequest(commandIdSeed, accountId, orderAction); //not incrementing the commandIdSeed as I am changing an order.

	    logger.info("Executing command: {}", commandIdSeed);

	    try {
	    	executor.execute(request, this);
	    } catch (RuntimeException ex) {
	    	logger.error("Failed to submit command: {}", commandIdSeed, ex);
	    }
		
	}
	
	
	
	@PostConstruct
	public List<Order> getWorkingOrdersList(){

		
		List<Order> allOrders = currentAccount.getOrders();
		List<Order> workingOrders = new ArrayList<Order>(); //to get the list of all working orders, I.e. orders that i can actually modify. 
		for (Order order : allOrders){
			if (order.getStatus() == OrderStatus.WORKING){
				workingOrders.add(order);
			}
		}
		
		if (workingOrders.size() > 0) 
			return workingOrders;
		else
			return null;
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
		accountSubscriptionService.removeListener(this);
		instrumentService.removeListener(instrumentListener);
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

	@Override
    public void accountUpdated(AccountUpdateEvent event) { //doesn't really do much...
        if (event.isReset()) { //I have an account open, thus an accountId
        	return;
        } 
        else {
        	
    		////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    		//Now finding a list of all the Working orders
    		//then going through the list and extracting the one that was most probably
    		//passed as a limit order. 
    		/*List<Order> workingOrders = getWorkingOrdersList();
    		Order orderToModify = null;
    		if (workingOrders == null){
    			//do whatever you might want like return an error
    		}
    		else{ //actually modify some trade
    			
    			for (Order order :  workingOrders){
    				
    				if (order.getInstrument() == instrument && order.getLimitPrice() == limit){ 
    					//we note that in order objects, getQUantity returns unfilled quantity of the instrument, 
    					//while getFilledQuantity gets the volume that is filled. So if order is partially filled,
    					//you have to add both.
    					
    					orderToModify = order;
    				}
    			}
    		}
    		////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    		//Now fixing this by calling the modifyOrder method
    		//changing the limit order to a market order that should be filled straight away. 
    		//The order has until the end of the day to be filled for a quantity of 150'000
    		//This order should be filled straight away on almost any instrument.
    		 *  
    		 */
        	
        	int num = 0;
        	for (Order order: event.getChangedOrders()) {
        		++num;
        		
        		if (order.getStatus() == OrderStatus.WORKING && order.getQuantity() == quantity) {
        			txtSometxt.setText("for loop " + String.valueOf(num));
        			modifyOrder(order, OrderDuration.DAY, quantity/2);
        		}
        	}
    		

        }
        

    }

    @Override
    public void instrumentsUpdated(InstrumentUpdateEvent event) { //this will happen once the account is loaded
        Map<Integer, Instrument> instruments = event.getUpdatedInstruments();
        for (Entry<Integer, Instrument> entry : instruments.entrySet()) {
            System.out.println("Instrument received " + entry.getValue().getSymbol());
        }
        
        
    }
}
