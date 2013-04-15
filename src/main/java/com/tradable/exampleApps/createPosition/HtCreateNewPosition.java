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


package com.tradable.exampleApps.createPosition;

//= These libraries are imported either for the graphics component or for standard utility=//
//====================================================================================
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
import com.tradable.api.services.account.CurrentAccountService;
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
import com.tradable.api.services.preferences.PreferenceKey;
import com.tradable.api.services.preferences.PreferenceListener;
import com.tradable.api.services.preferences.PreferenceService;
//====================================================================================
//====================================================================================

//========= (3) Import if App will be using the TradingRequestExecutor API==========//
//====================================================================================
import com.tradable.api.entities.Order; //This is also used in (1) when listening 
										//to activity on the user's account.
import com.tradable.api.entities.Position;
import com.tradable.api.entities.OrderDuration;
import com.tradable.api.entities.OrderSide;
import com.tradable.api.entities.OrderType;
import com.tradable.api.services.executor.TradingRequestExecutor;


//====================================================================================
//====================================================================================

public class HtCreateNewPosition extends JPanel implements WorkspaceModule, ActionListener, PreferenceListener{
	
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
	private AccountRelatedClass accountRelatedObject;
	//==================================================================================	
	
	//========================================(2)========================================//
	private InstrumentAndMarketData dataObject;
	//==================================================================================	
	
	private PlaceOrderClass placeOrderObject;
	
	private PreferenceService preferenceService;
	private Boolean oneClickEnabled = null;
	private Boolean mulTracksEnabled = null;
	private Boolean confirmedOrder = null;
	
	private JTextPane textPane;
	private JButton btnNewButton;
	final JTextField bidTextField;
	final JTextField askTextField;

	
	static int clickRound;
	
	public HtCreateNewPosition(TradingRequestExecutor executor, 
			CurrentAccountService accountSubscriptionService, 
			InstrumentService instrumentService, QuoteTickService quoteTickService,
			PreferenceService preferenceService) {
			
		
		//============= This code sets up the visual component of our Module==============//
		//====================================================================================	
		setLayout(null);
		setSize(400, 400);	
		setBackground(Color.DARK_GRAY);
		putClientProperty(WorkspaceModuleProperties.COMPONENT_TITLE, TITLE);
		putClientProperty(WorkspaceModuleProperties.COMPONENT_RESIZE_ENABLED, false);
		
		//used for printing the quotes
		bidTextField = new JTextField();
		bidTextField.setEditable(false);
		bidTextField.setBounds(40, 70, 140, 30);
		add(bidTextField);
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
		btnNewButton = new JButton("Click Me");
		btnNewButton.addActionListener(this); //we note we have to add an action listener here.
		btnNewButton.setBounds(150, 20, 100, 30);
		add(btnNewButton);	
		
		
		//====================================================================================	
		//====================================================================================	
		
		//setting all the values for used objects.

		dataObject = new InstrumentAndMarketData(instrumentService, quoteTickService, 
			bidTextField, askTextField);
		accountRelatedObject = new AccountRelatedClass(accountSubscriptionService, textPane);
		placeOrderObject = new PlaceOrderClass(executor, logger);
		this.preferenceService = preferenceService;
		//========================================(1)========================================//
		//
		//====================================================================================	
		
		//========================================(2)========================================//
		//
		//====================================================================================	
		

        //========================================(3)========================================//
		placeOrderObject.setAccountId(accountRelatedObject.getAccountId());
		this.preferenceService.addPreferenceListener(PreferenceKey.ONE_CLICK_TRADING_ENABLED, this);
		this.preferenceService.addPreferenceListener(PreferenceKey.MULTIPLE_TRACKS_ENABLED, this);
		oneClickEnabled = this.preferenceService.isOneClickTradingEnabled();
		mulTracksEnabled = this.preferenceService.isMultipleTracksEnabled();
		confirmedOrder = false;
        
		clickRound = 0;
		
	}	
    
    
    
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
    //now places a limit order for 2'000 that should be filled instantly as the set limit 
    //is slightly higher than the latest found ask price. We note that on the fourth click, 
    //the App gets a list of all working methods using the getWorkingOrdersList() method which is 
    //defined here which quite simply returns a list of working orders. Once it has the list of 
    //working orders, it selects the one to modify by checking both the instrument it uses and the 
	//Quantity ordered.
	//When clicked again, go to 1).
	//====================================================================================	
	
    
	@Override
	public void actionPerformed(ActionEvent arg0) {
		
		
		try{
			
			if (clickRound == 0){
				Random randGen = new Random();
				int randomIndex = randGen.nextInt(dataObject.getAccountInstruments().size()-1);
				dataObject.setCurrentInstrument((Instrument) 
						dataObject.getAccountInstruments().toArray()[randomIndex]);
				//instrument = instrumentService.getInstrument("EURUSD"); //used for testing on liquid instrument.
				
				
				textPane.getDocument().insertString(textPane.getCaretPosition() , 
						"Intrument set to: ." + dataObject.getCurrentInstrument().getSymbol() + "\n" , null);
				

				dataObject.setCurrentTickSubscriptionSymbol();
				dataObject.setCurrentQuotes();
	            
				try {
		            bidTextField.setText("currBid: " + String.valueOf(dataObject.getCurrentBid().getPrice()));	
		            askTextField.setText("currAsk: " + String.valueOf(dataObject.getCurrentAsk().getPrice()));

				} catch (NullPointerException e) {
					textPane.getDocument().insertString(textPane.getCaretPosition() , 
							"NullPointerException caught, symbol cannot be used at this time " +
							"because prices aren't being updated fast enough!\n\n" + 
							"Click again to get prices for another symbol\n\n" , null);
					
					e.printStackTrace();
					return; //clickRound not incremented.
				}
				
			}
			
			
			else{
				
				
				if(oneClickEnabled || confirmedOrder){
					
					if (clickRound == 1){
						placeOrderObject.placeOrder(dataObject.getCurrentInstrument(), 
								OrderSide.SELL, OrderDuration.DAY, OrderType.MARKET, 2000.0);
					}
					
					else if (clickRound == 2){
						//setting the limit price at a value that will not be filled (15 % below the current asking price)
						placeOrderObject.placeOrder(dataObject.getCurrentInstrument(), OrderSide.BUY, 
								OrderDuration.DAY, OrderType.LIMIT, 1000.0, 0.85*dataObject.getCurrentAsk().getPrice());

					}
					
					else if (clickRound == 3){
			    		List<Order> workingOrders = accountRelatedObject.getWorkingOrdersList();
			    		Order orderToModify = null;
			    		if (workingOrders == null){
			    			return; //there was an error, the order could not be found.
			    		}
			    		else{
			    			for(Order order : workingOrders){
			    				
			    				if((order.getInstrument().getSymbol().equals(dataObject.getCurrentInstrument().getSymbol())) 
			    						&& (order.getQuantity() == 1000.0))
			    					orderToModify = order;
			    				
			    			}
			    		}

			    		placeOrderObject.modifyOrder(orderToModify, OrderDuration.DAY, 5000.0, 
			    				1.01* dataObject.getCurrentAsk().getPrice());
			    		
						textPane.getDocument().insertString(textPane.getCaretPosition() , 
								"Order is being modified \n\n" , null);		
						
					}
					
					else{
						
						//find a position you want to set an OCO order to.
						//This code assumes a position long 3000 EURUSD was taken
						//the current account. The position object is found that way
						Position oCOPosition = null;
						List<Position> positions = accountRelatedObject.getCurrentAccount().getPositions();
						for (Position pos : positions){
							if (pos.getInstrument().getSymbol().equals(dataObject.getCurrentInstrument().getSymbol())
									&& (pos.getQuantity() == 3000.0)){
								oCOPosition = pos;
							}
							
						}
						
						placeOrderObject.OCOOrder(oCOPosition, 0.98 * dataObject.getCurrentBid().getPrice(), 
								1.02 * dataObject.getCurrentAsk().getPrice());
						clickRound = 0;
						return;
								
					}
					
					
					confirmedOrder = false;
				}//end if(oneClickEnabled....)
				else{
					textPane.getDocument().insertString(textPane.getCaretPosition() , 
						"One Click trading is disabled. Click again to confirm order.\n\n" , null);
					
					confirmedOrder = true;
					return;
				}
				
				
			}
			
			
		}
		catch (BadLocationException e) {
			e.printStackTrace();
		}
				
		
		++clickRound;
	}
	
	
    //====================================================================================
    //====================================================================================		

	

	@Override
	public void preferenceChanged(PreferenceKey preferenceKey, Object newValue) {
		if (preferenceKey == PreferenceKey.ONE_CLICK_TRADING_ENABLED){
			oneClickEnabled = (Boolean) newValue;
			confirmedOrder = false;
		}
		else if (preferenceKey == PreferenceKey.MULTIPLE_TRACKS_ENABLED)
			mulTracksEnabled = (Boolean) newValue;
		else{}
	}
 
	
	//====================================================================================
    //Don't forget to remove listeners. For the subscription object, the destroy() method
	//takes care of that for us.
	//====================================================================================
	@Override
	public void destroy() {
		
		accountRelatedObject.destroy();
		preferenceService.removePreferenceListener(this);
		dataObject.destroy();
	}

	@Override
	public PersistedStateHolder getPersistedState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JComponent getVisualComponent() {
		return this;
	}

	@Override
	public void loadPersistedState(PersistedStateHolder arg0) {
		// TODO Auto-generated method stub
		
	}


}
