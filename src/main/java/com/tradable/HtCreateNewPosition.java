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
import javax.swing.JTextField;
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
import com.tradable.api.services.marketdata.Quote;
import com.tradable.api.services.marketdata.QuoteTickEvent;
import com.tradable.api.services.marketdata.QuoteTickListener;
import com.tradable.api.services.marketdata.QuoteTickService;
import com.tradable.api.services.marketdata.QuoteTickSubscription;
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
import com.tradable.api.services.executor.ModifyOrderAction;
import com.tradable.api.services.executor.ModifyOrderActionBuilder;
import com.tradable.api.services.executor.OrderAction;
import com.tradable.api.services.executor.OrderActionRequest;
import com.tradable.api.services.executor.PlaceOrderAction;
import com.tradable.api.services.executor.PlaceOrderActionBuilder;
import com.tradable.api.services.executor.TradingRequest;
import com.tradable.api.services.executor.TradingRequestExecutor;
import com.tradable.api.services.executor.TradingRequestListener;
import com.tradable.api.services.executor.TradingResponse;
//====================================================================================
//====================================================================================

public class HtCreateNewPosition extends JPanel implements WorkspaceModule, 
		TradingRequestListener, CurrentAccountServiceListener, InstrumentServiceListener, 
		QuoteTickListener, ActionListener{
	
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

	//========================================(1)========================================//
	//Declaring CurrentAccountService object and Account object. The CurrentAccountService
	//object will be set in the constructor (from our factory). We declare it here too, in
	//order for it to be accessible in other methods and not just the constructor. We also
	//declare an Account object which will be set whenever the account is reset. Such an
	//event is said to occur whenever the Module starts up and whenever the user selects
	//a different account to use. The accountId value is found once the Account object
	//is instantiated. This also happens whenever the Account is reset, i.e. in this
	//case, when a new Module's instantiation is open. The accountId is used for passing
	//orders.
	//==================================================================================	
	CurrentAccountService accountSubscriptionService;
	Account currentAccount;
	private int accountId;
	//==================================================================================	
	//==================================================================================	
	
	//========================================(2)========================================//
	//We here declare the objects that will be used for for monitoring the user account's
	//instruments and for monitoring the bid and ask prices of an instrument once we have
	//subscribed to listening to changes in it.
	//The two Quote objects will be used once an instrument is identified to update and
	//display the latest bid and ask prices.
	//==================================================================================
	private InstrumentService instrumentService;
	private Instrument instrument;
	private QuoteTickService quoteTickService;
	private QuoteTickSubscription subscription;
	private Quote bid;
	private Quote ask;
	//==================================================================================	
	//==================================================================================	
	
	//========================================(3)========================================//
	//The TradingRequestExecutor object is in charge of executing the trades once all the 
	//settings pertaining to it have been set. The commandIdSeed is just a number that allows
	//us to track internally the number of the command we are at. This is usde in the log.
	//==================================================================================
	private TradingRequestExecutor executor;
	private int commandIdSeed;
	//==================================================================================	
	//==================================================================================
	
	
	
	private JTextPane textPane;
	private JButton btnNewButton;
	final JTextField bidTextfield;
	final JTextField askTextField;

	
	static int clickRound;
	
	public HtCreateNewPosition(TradingRequestExecutor executor, 
			CurrentAccountService accountSubscriptionService, 
			InstrumentService instrumentService, QuoteTickService quoteTickService) {
			
		
		//============= This code sets up the visual component of our Module==============//
		//====================================================================================	
		setLayout(null);
		setSize(400, 400);	
		setBackground(Color.DARK_GRAY);
		putClientProperty(WorkspaceModuleProperties.COMPONENT_TITLE, TITLE);
		putClientProperty(WorkspaceModuleProperties.COMPONENT_RESIZE_ENABLED, false);
		
		//used for printing the quotes
		bidTextfield = new JTextField();
		bidTextfield.setEditable(false);
		bidTextfield.setBounds(40, 70, 140, 30);
		add(bidTextfield);
		askTextField = new JTextField();
		askTextField.setEditable(false);
		askTextField.setBounds(220, 70, 140, 30);
		add(askTextField);

		
		//used for printing account events such as Order, Trade or Position changes
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setBounds(0, 120, 400, 280);
		add(scrollPane);	
		textPane = new JTextPane();
		textPane.setEditable(false);
		scrollPane.setViewportView(textPane); //setting the JScrollPane object to the textPane.
		
		
		//used for the button to allow the user to set instruments and pass orders.
		JButton btnNewButton = new JButton("Click Me");
		btnNewButton.addActionListener(this); //we note we have to add an action listener here.
		btnNewButton.setBounds(150, 20, 100, 30);
		add(btnNewButton);	
		
		
		//====================================================================================	
		//====================================================================================	
		
		
		
		//========================================(1)========================================//
		//the "this" object is a CurrentAccountServiceListener object too, so we can add listeners 
		//this way. We also set the accountService object to the one Autowired in the factory 
		this.accountSubscriptionService = accountSubscriptionService;
		this.accountSubscriptionService.addListener(this);
		//====================================================================================	
		//====================================================================================	
		
		//========================================(2)========================================//
		//
		//We note, for the QuoteTickService interface, the only available method to its objects
		//is the createSubscription() one. Then the subscription listens for the changes in the
		//this object.
		//====================================================================================	
		this.instrumentService = instrumentService;
		this.instrumentService.addListener(this);
		this.quoteTickService = quoteTickService; 
		subscription = quoteTickService.createSubscription();
        subscription.setListener(this);  //num: subscription now listens to this (as I am implementing the quoteTickListener)
		
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
	//We note that currentAccount is set on every event, as this is currently the only way
	//to always have the latest information in the currentAccount object.
	//====================================================================================	


	@Override
    public void accountUpdated(AccountUpdateEvent event) {
	
		currentAccount = accountSubscriptionService.getCurrentAccount();
        if (event.isReset()) {
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
    
	@Override
	public void quotesUpdated(QuoteTickEvent event) {
		bidTextfield.setText("currBid: " + String.valueOf(bid.getPrice()));	
		askTextField.setText("currAsk: " + String.valueOf(ask.getPrice()));                                                    	    	
		
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
    //1). On The first click will set randomly the instrument to use amongst the Collection
    //of instruments that was returned using the getInstruments(String Symbol) method.
    //This is meant to show how the Collection might be used to search through it and
    //find instruments. Then, the QuoteTickSubscription is set to the symbol of the 
	//instrument in question and the latest bid and ask prices are printed in the
	//appropriate fields. If an exception occurs (due to the randomly chosen symbol),
	//the next click will find another instrument and try printing its values too.
    //2). On the second click, the user
    //places a good till cancelled market order for 2'000. When the market is open, this 
    //order should be filled almost instantly and the program will print the order, trade
    //and position information out accordingly.
    //3). On the third click, the user will try placing a limit order for 1'000.
    //The limit is set so that the order will never be filled and it will remain pending
    //or in "working" state. 
    //4). On the fourth click, the user changes the last pending order he placed and 
    //now places a limit order for 1'500 that should be filled instantly as the set limit 
    //is slightly higher than the latest found ask price. We note that on the fourth click, 
    //the App gets a list of all working methods using the getWorkingOrdersList() method which is 
    //defined here which quite simply returns a list of working orders. Once it has the list of 
    //working orders, it selects the one to modify by checking both the instrument it uses and the 
	//Quantity ordered.
	//When clicked again, go to 1).
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
			

			subscription.setSymbol(instrument.getSymbol());			
            ask = subscription.getAsk(instrument.getSymbol());            
            bid = subscription.getBid(instrument.getSymbol());
            
			try {
	            bidTextfield.setText("currBid: " + String.valueOf(bid.getPrice()));	
	            askTextField.setText("currAsk: " + String.valueOf(ask.getPrice()));

			} catch (Exception e) {
				try {
					textPane.getDocument().insertString(textPane.getCaretPosition() , 
							"Exception caught, symbol cannot be used at this time \n" + 
							"Click again to get prices for another symbol\n\n" , null);
				} catch (BadLocationException ex) {
					ex.printStackTrace();
				}
				e.printStackTrace();
				return; //clickRound not incremented.
			}
			
		}
		
		else if (clickRound == 1){
			
			placeOrder(instrument, OrderSide.SELL, OrderDuration.DAY, OrderType.MARKET, 2000.0); 

		}
		
		else if (clickRound == 2){
			//setting the limit price at a value that will not be filled (15 % below the current asking price)
			placeOrder(instrument, OrderSide.BUY, OrderDuration.DAY, OrderType.LIMIT, 1000.0, 0.85*ask.getPrice()); 
			
		}
		
		else{ //clickRound is 3
			
    		List<Order> workingOrders = getWorkingOrdersList();
    		Order orderToModify = null;
    		if (workingOrders == null){
    			clickRound = 0;
    			return; //there was an error, the order could not be found.
    		}
    		else{
    			for(Order order : workingOrders){
    				
    				if((order.getInstrument().getSymbol() == instrument.getSymbol()) 
    						&& (order.getQuantity() == 1000.0))
    					orderToModify = order;
    				
    			}
    		}

			modifyOrder(orderToModify, OrderDuration.DAY, 1500.0);
			try {
				textPane.getDocument().insertString(textPane.getCaretPosition() , 
						"Order is being modified \n" + 
						"Click again to get prices for another symbol\n\n" , null);
				clickRound = 0;
				return;
			} catch (BadLocationException ex) {
				ex.printStackTrace();
				clickRound = 0;
			}
			
			
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
		orderActionBuilder.setLimitPrice(1.01* ask.getPrice()); //setting ask price to get filled almost surely
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
	
	@Override
	public void requestExecuted(TradingRequestExecutor executor, TradingRequest request, TradingResponse response) {
		if (response.isSuccess())
			logger.info("Command is successfully executed: {}", request.getId());
					
		else 
			logger.error("Command is failed to execute: {}", request.getId(), response.getCause());
		
	}
	
	
	public List<Order> getWorkingOrdersList(){

		//to get the list of all working orders, I.e. orders that i can actually modify. 
		List<Order> workingOrders = new ArrayList<Order>(); 
		for (Order order : currentAccount.getOrders()){
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

 
	
	
	//====================================================================================
    //Don't forget to remove listeners. For the subscription object, the destroy() method
	//takes care of that for us.
	//====================================================================================
	@Override
	public void destroy() {
		
		accountSubscriptionService.removeListener(this);
		instrumentService.removeListener(this);
		subscription.destroy();
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
