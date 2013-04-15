package com.tradable.exampleApps.createPosition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;

import com.tradable.api.entities.Account;
import com.tradable.api.entities.Order;
import com.tradable.api.entities.OrderStatus;
import com.tradable.api.entities.OrderType;
import com.tradable.api.entities.Position;
import com.tradable.api.entities.Trade;
import com.tradable.api.services.account.AccountUpdateEvent;
import com.tradable.api.services.account.CurrentAccountService;
import com.tradable.api.services.account.CurrentAccountServiceListener;

public class AccountRelatedClass implements CurrentAccountServiceListener {

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
	
	private CurrentAccountService accountSubscriptionService;
	private Account currentAccount;
	private int accountId;
	
	private JTextPane textPane;
	
	public AccountRelatedClass(CurrentAccountService accountSubscriptionService, JTextPane textPane){
		
		this.textPane = textPane;
		
		//the "this" object is a CurrentAccountServiceListener object too, so we can add listeners 
		//this way. We also set the accountService object to the one Autowired in the factory 
		
		this.accountSubscriptionService = accountSubscriptionService;
		this.accountSubscriptionService.addListener(this);
		
	}
	
	public void destroy() {
		
		accountSubscriptionService.removeListener(this);

	}
	
	
	public int getAccountId(){
		return accountId;
	}
	
	public Account getCurrentAccount(){
		
		return currentAccount;
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
                	
                
                for (Position position : mychangedPositions){
                		

    				if (position.getQuantity() > 0){
    					textPane.getDocument().insertString(textPane.getCaretPosition() , 
    							" You are now long " + position.getQuantity() + " " 
    							+ position.getInstrument().getSymbol()+ "\n" , null);
    				}
    				else if (position.getQuantity() < 0){
    					textPane.getDocument().insertString(textPane.getCaretPosition() ,
    							" You are now short (" + Math.abs(position.getQuantity()) + ") " 
    							+ position.getInstrument().getSymbol()+ "\n" , null);
    				}
    				else{
    					textPane.getDocument().insertString(textPane.getCaretPosition() ,
    							" You are now flat on " + position.getInstrument().getSymbol()
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

}
