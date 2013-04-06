//============================================================================
// Name        : HtCreateNewPosition.java
// Author      : John-David "JD" Wuarin
// Copyright (c) tradable ApS.
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense,
// and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.
//
//============================================================================

/*************************************************************************************
 * This program is to be used as a learning tool and as a basis for creating
 * bigger more elaborate tradable apps. The use of the different tradable APIs is 
 * heavily commented and the different services and listeners used are segmented
 * in an obvious manner so as to allow developers to remove and keep only the
 * code samples of interest to them. 
 * 
 *  For the README on how to to start working on your own project using this 
 *  code, please go to: https://github.com/john-dwuarin/tradable-start-np
 *************************************************************************************/


package com.tradable;

//= These libraries are imported either for the graphics component or for standard utility=//
//====================================================================================
import java.util.ArrayList; 
import java.util.Collection;
import java.util.List; 
import java.util.Random;
import java.lang.String;


import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.JButton;
import javax.swing.text.BadLocationException;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//====================================================================================
//===================================================================================


//========= (0) component API, has to be imported in any project==========//
//====================================================================================
import com.tradable.api.component.WorkspaceModule;
import com.tradable.api.component.WorkspaceModuleProperties;
import com.tradable.api.component.state.PersistedStateHolder;
//====================================================================================
//====================================================================================

//========= (1) Import if App will be using the CurrentAccountService API==========//
//==================================================================================
import com.tradable.api.entities.Account;
import com.tradable.api.services.account.AccountUpdateEvent;
import com.tradable.api.services.account.CurrentAccountService;
import com.tradable.api.services.account.CurrentAccountServiceListener;
//====================================================================================
//====================================================================================

//========= (2) Import if App will be using the InstrumentService API==========//
//====================================================================================
import com.tradable.api.entities.Instrument;
import com.tradable.api.services.instrument.InstrumentService;
import com.tradable.api.services.instrument.InstrumentServiceListener;
import com.tradable.api.services.instrument.InstrumentUpdateEvent;
//====================================================================================
//====================================================================================

//========= (3) Import if App will be using the TradingRequestExecutor API==========//
//====================================================================================
import com.tradable.api.entities.Order; //This is also used in (1) when listening 
										//to activity on the user's account.
import com.tradable.api.entities.Trade;
import com.tradable.api.entities.Position;
import com.tradable.api.entities.OrderDuration;
import com.tradable.api.entities.OrderSide;
import com.tradable.api.entities.OrderStatus;
import com.tradable.api.entities.OrderType;
import com.tradable.api.services.executor.ModifyOrderActionBuilder;
import com.tradable.api.services.executor.OrderAction;
import com.tradable.api.services.executor.OrderActionRequest;
import com.tradable.api.services.executor.PlaceOrderActionBuilder;
import com.tradable.api.services.executor.TradingRequest;
import com.tradable.api.services.executor.TradingRequestExecutor;
import com.tradable.api.services.executor.TradingRequestListener;
import com.tradable.api.services.executor.TradingResponse;
//====================================================================================
//====================================================================================

public class HtCreateNewPosition extends JPanel implements WorkspaceModule, 
		TradingRequestListener, CurrentAccountServiceListener, InstrumentServiceListener, ActionListener{
	
	//====================================================================================
	//This a static final long object serialVersionUID variable has to be declared as 
	//the HtCreateNewPosition class is a serializable class. Once an object is serialized
	//(i.e. it is converted to physical memory), the deserialization process will use this
	//number to make sure the obtained object (object creaed from the deserialization 
	//pprcess) is effectively of this class. If not, an InvalidClassException is thrown.
	//That every instance of the class has the same serial number. 
	//====================================================================================
	private static final long serialVersionUID = 8426444465622687177L;

	private static final Logger logger = LoggerFactory.getLogger(HtCreateNewPosition.class);
	
	private static final String TITLE = "Rename me";
	
	
	
	
	private TradingRequestExecutor executor;
	private int commandIdSeed;

	CurrentAccountService accountSubscriptionService;
	Account currentAccount = null;
	private int accountId;
	
	private InstrumentService instrumentService;
	private Instrument instrument= null;

	
	private JTextPane textPane;
	private JButton btnNewButton;
	
	private static int clickRound;
	
	public HtCreateNewPosition(TradingRequestExecutor executor, 
			final CurrentAccountService accountSubscriptionService, 
			final InstrumentService instrumentService) {
			
		
		//============= This code sets up the visual component of our Module==============//
		//====================================================================================	
		setLayout(null);
		setSize(400, 400);	
		setBackground(Color.DARK_GRAY);
		putClientProperty(WorkspaceModuleProperties.COMPONENT_TITLE, TITLE);
		putClientProperty(WorkspaceModuleProperties.COMPONENT_RESIZE_ENABLED, false);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setBounds(0, 60, 400, 340);
		add(scrollPane);
		
		textPane = new JTextPane();
		textPane.setEditable(false);
		scrollPane.setViewportView(textPane); //setting the JScrollPane object to the textPane.
		
		JButton btnNewButton = new JButton("Click Me");
		btnNewButton.addActionListener(this); //we note we have to add an action listener here.
		btnNewButton.setBounds(150, 13, 100, 34);
		add(btnNewButton);	
		
		
		//====================================================================================	
		//====================================================================================	
		
		
		
		//========================================(1)========================================//
		//this object is a CurrentAccountServiceListener object too, so we can add listener 
		//this way. We also set the accountService object to the one Autowired in the factory 
		this.accountSubscriptionService = accountSubscriptionService;
		this.accountSubscriptionService.addListener(this);
		//====================================================================================	
		//====================================================================================	
		
		//========================================(2)========================================//
		this.instrumentService = instrumentService;
		this.instrumentService.addListener(this);
		
		//========================================(3)========================================//
		this.executor = executor; //no need to add listeners as this object takes action.
		
		
		clickRound = 0;
		
	}
	
	
	 
	
	//========================================(1)========================================//
	//Upon instantiation, the account will detect by default that it is reset. So isReet
	//will return true. We therefore get the value for our currAccount Account object and
	//from there our accountId which will be used to pass orders.
	//We also recall that CurrentAccountServiceListener fires up an event whenever
	//an order is placed, a trade takes place, and or a position changes. This method
	//thus includes code that simply writes down whatever it sees happening in the textPane.
	//====================================================================================	


	@Override
    public void accountUpdated(AccountUpdateEvent event) {
        if (event.isReset()) {
        	
    		currentAccount = accountSubscriptionService.getCurrentAccount();
    		accountId = currentAccount.getAccountId();
        } 
        else {
        	
            Collection<Order> myChangedOrders = event.getChangedOrders();
            Collection<Trade> mychangedTrades = event.getChangedTrades();
            Collection<Position> mychangedPositions = event.getChangedPositions();
            
            try {
            
                
                for (Order order : myChangedOrders){
                	
                	if (order.getStatus() == OrderStatus.ACCEPTED){
                		textPane.getDocument().insertString(textPane.getDocument().getLength() ,
     					"Order for " + order.getQuantity() + " " + order.getInstrument().getSymbol()
     					+ " accepted \n", null);
     					break;
                	}
                	
                	else if (order.getStatus() == OrderStatus.COMPLETED){
                		textPane.getDocument().insertString(textPane.getDocument().getLength() ,
                		"Order for " + order.getQuantity() + " " + order.getInstrument().getSymbol()
                		+ " completed \n", null);
        				break;
                	}
                	
                	else if (order.getStatus() == OrderStatus.CANCELED){
                		textPane.getDocument().insertString(textPane.getDocument().getLength() ,
                		"Order for " + order.getQuantity() + " " + order.getInstrument().getSymbol()
                		+ " canceled \n", null);
        				break;
                	}
                	
                	else if (order.getStatus() == OrderStatus.EXPIRED){
                		textPane.getDocument().insertString(textPane.getDocument().getLength() ,
                		"Order for " + order.getQuantity() + " " + order.getInstrument().getSymbol()
                		+ " expired \n", null);
        				break;
                	}
                	
                	else if (order.getStatus() == OrderStatus.NEW){
                		textPane.getDocument().insertString(textPane.getDocument().getLength() ,
                		"Order for " + order.getQuantity() + " " + order.getInstrument().getSymbol()
                		+ " new \n", null);
        				break;
                	}
                	
                	else if (order.getStatus() == OrderStatus.REJECTED){
                		textPane.getDocument().insertString(textPane.getDocument().getLength() ,
                		"Order for " + order.getQuantity() + " " + order.getInstrument().getSymbol()
                		+ " rejected \n", null);
        				break;
                	}
                	
                	else if (order.getStatus() == OrderStatus.REPLACED){
                		textPane.getDocument().insertString(textPane.getDocument().getLength() ,
                		"Order for " + order.getQuantity() + " " + order.getInstrument().getSymbol()
                		+ " replaced \n", null);
        				break;
                	}
                	
                	else if (order.getStatus() == OrderStatus.WAITING){
                		textPane.getDocument().insertString(textPane.getDocument().getLength() ,
                		"Order for " + order.getQuantity() + " " + order.getInstrument().getSymbol()
                		+ " waiting \n", null);
        				break;
                	}
                	
                	else if (order.getStatus() == OrderStatus.WORKING){
                		
                		if (order.getType() == OrderType.LIMIT){
    	            		textPane.getDocument().insertString(textPane.getDocument().getLength() ,
    	    	            "Order for " + order.getQuantity() + " " + order.getInstrument().getSymbol()
    	    	            + " limit order is working \n", null);
    	    	            break;
                		}
                		
                		else if (order.getType() == OrderType.MARKET){
    	            		textPane.getDocument().insertString(textPane.getDocument().getLength() ,
    	    	            "Order for " + order.getQuantity() + " " + order.getInstrument().getSymbol()
    	    	            + " market order is working \n", null);
    	    	            break;
                		}
                		
                    	
                		else{
    	            		textPane.getDocument().insertString(textPane.getDocument().getLength() ,
    	    	            "Order for " + order.getQuantity() + " " + order.getInstrument().getSymbol()
    	    	            + " is working \n", null);
    	            		break;
                		}
                			
                	}
                	
                }
                	
                
                for (Position position : mychangedPositions){
                		

    				if (position.getQuantity() > 0){
    					textPane.getDocument().insertString(textPane.getCaretPosition() , 
    							" You are now long " + position.getQuantity() + " " 
    							+ position.getInstrument().getSymbol()+ "\n" , null);
    				}
    				else if (position.getQuantity() < 0){
    					textPane.getDocument().insertString(textPane.getCaretPosition() ,
    							" You are now short (" + Math.abs(position.getQuantity()) + ")" 
    							+ position.getInstrument().getSymbol()+ "\n" , null);
    				}
    				else{
    					textPane.getDocument().insertString(textPane.getCaretPosition() ,
    							" You are now flat on " + position.getInstrument().getSymbol()
    							+ "\n" , null);
    				}
                	            	
                }
                
                
                for (Trade trade : mychangedTrades){
                	
    				if (trade.getQuantity() > 0){
    					textPane.getDocument().insertString(textPane.getCaretPosition() , 
    							" You just went long " + trade.getQuantity() + " "
    							+ trade.getInstrument().getSymbol() 
    							+ " at the price of " + trade.getPrice() + "\n", null);
    				}
    				else if (trade.getQuantity() < 0){
    					textPane.getDocument().insertString(textPane.getCaretPosition() , 
    							" You just went short (" + Math.abs(trade.getQuantity()) + ") " 
    							+ trade.getInstrument().getSymbol() 
    							+ " at the price of " + trade.getPrice() + "\n", null);
    				}
    				else{
    					textPane.getDocument().insertString(textPane.getCaretPosition() , 
    							"You apparently traded nothing, this should never happen. This is a bug."
    							+ "\n" , null);
    				}
        	
            	
                }
                
    			textPane.getDocument().insertString(textPane.getCaretPosition() , "\n" , null);
            
    		} 
            
            catch (BadLocationException e) {
    			e.printStackTrace();
    		}
    		

        }
        

    }	
    //====================================================================================
    //====================================================================================		
	
	
	
	
	//========================================(2)========================================//
	//Remember that instrument events occur only when the user switches session accounts
	//or when the list is changed. This will almost never happen while you are running this
	//example, so no need to implement this method.
	//====================================================================================	

    @Override
    //this will happen once the account is loaded
    public void instrumentsUpdated(InstrumentUpdateEvent event) { 
        
    }
    //====================================================================================
    //====================================================================================	
    
    
    
    
	//========================================(3)========================================//
	//We have a set of different methods and implementations. Our first method listens for
    //actions performed by the user. In this event, an event will be fired any time the
    //user clicks the button. THe method was set up so that 
    //
    //actionPerformed(..):
    //
    //1). THe first click will set randomly the instrument to use amongst the Collection
    //of instruments that was returned using the getInstruments(String Symbol) method.
    //This is meant to show how the COllection might be used to search through it and
    //find instruments.
    //2). on the first click, the user
    //places a good till cancelled market order for 100'000. When the market is open, this 
    //order should be filled almost instantly and the program will print the order, trade
    //and position information out accordingly.
    //3). On the second click, the user will try placing a limit order for 75'000.
    //The limit is set so that the order will never be filled and it will remain pending
    //or in "working" state. 
    //4). On the third click, the user changes the last pending order he placed and 
    //now places a limit order for 90'000 that will be filled when the price reached the limit 
    //when the market is open. We note that on the third click, the App gets a list of all
    //working methods using the getWorkingOrdersList() method which is defined here which
    //quite simply returns a list of working methods. Once it has the list of workingOrders,
    //it selects the one to modify by checking both the instrument it uses and the Quantity ordered.
    //
    //placeOrder(..):
    //Is an Overloaded method that allows a user to place either a Limit or a Market order.
    //It uses the PlaceOrderActionBuilder class which lets us set the properties of the order we
    //want to pass. Once all the properties are set, the method calls the build() method
    //from the PlaceOrderActionBuilder class. This creates an order with the properties we just set.
    //We then create our OrderActionRequest object that also uses our accountId information
    //in order for the container to identify what account the trade has to be passed onto.
    //The commandIdSeed that is used for the logfile.
    //Now that our request object is created we execute using the executor object that was
    //set in the constructor.
    //
    //modifyOrder(..):
    //Is very similar to our PlaceOrder method. The main differences are that we pass 
    //the order to modify as an argument of ModifyOrderActionBuilder (The order to modify
    //can actually be set after the ModifyOrderActionBuilder object is created using the 
    // setOrder(Order order) method.) and that we hard code the fact that the order is a
    //Market order.
    //
    //requestExecuted(..):
    //is just the overridden method from the TradingRequestExecutor interface. It simply
    //obtains a trading response from the executor and returns whether the response was 
    //a success or not in the log.
	//====================================================================================	
	
    
	@Override
	public void actionPerformed(ActionEvent arg0) {
		
		//instrument = instrumentService.getInstrument("EURUSD"); //
		//instrument = (Instrument) instrumentService.getInstruments().toArray()[0];
		
		//we send a market order that should be filled almost immediately.
		
		//This is mostly to show how to use getInstruments. Most of the instruments
		//are actually not tradable and some or all the orders won't go through.
		if (clickRound == 0){
			Random randGen = new Random();
			int randomIndex = randGen.nextInt(instrumentService.getInstruments().size()-1);
			instrument = (Instrument) instrumentService.getInstruments().toArray()[randomIndex];
			
			try {
				textPane.getDocument().insertString(textPane.getCaretPosition() , 
						"Intrument set to: ." + instrument.getSymbol() + "\n" , null);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
			
		}
		
		else if (clickRound % 3 == 1){
			
			placeOrder(instrument, OrderSide.SELL, OrderDuration.DAY, OrderType.MARKET, 100000.0); 

		}
		
		else if (clickRound % 3 == 2){
			//setting the limit price at a value that will not be filled at this time (April 6th 2013)
			// yet won't be rejected by the container.
			placeOrder(instrument, OrderSide.BUY, OrderDuration.DAY, OrderType.LIMIT, 75000.0, 1.2); 
			
		}
		
		else{ //clickRound is > 0 && clickRound % 3 == 0
			
    		List<Order> workingOrders = getWorkingOrdersList();
    		Order orderToModify = null;
    		if (workingOrders == null){
    			return; //there was an error, the order could not be found.
    		}
    		else{
    			for(Order order : workingOrders){

    				if(order.getInstrument().getSymbol() == instrument.getSymbol() && order.getQuantity() == 75000.0){
    					orderToModify = order;
    				}
    				
    			}
    		}

			modifyOrder(orderToModify, OrderDuration.DAY, 90000.0);
			
		}
		
		++clickRound;
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

		OrderAction orderAction = orderActionBuilder.build();

		OrderActionRequest request = new OrderActionRequest(accountId, orderAction); 

		logger.info("Executing command: {}", ++commandIdSeed);

		try {
			executor.execute(request, this);
		} 
		
		catch (RuntimeException ex) {
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
	    orderActionBuilder.setDuration(orderDuration);
	    orderActionBuilder.setQuantity(quantity);

	    OrderAction orderAction = orderActionBuilder.build();

	    OrderActionRequest request = new OrderActionRequest(accountId, orderAction);
	    
	    logger.info("Executing command: {}", ++commandIdSeed);

	    try {
	    	executor.execute(request, this);
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
	
	
	public List<Order> getWorkingOrdersList(){

		
		List<Order> allOrders = currentAccount.getOrders();
		
		//to get the list of all working orders, I.e. orders that i can actually modify. 
		List<Order> workingOrders = new ArrayList<Order>(); 
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
	
    //====================================================================================
    //====================================================================================		

 
	
	
	
    
	@Override
	public void destroy() {
		
		accountSubscriptionService.removeListener(this);
		instrumentService.removeListener(this);
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
