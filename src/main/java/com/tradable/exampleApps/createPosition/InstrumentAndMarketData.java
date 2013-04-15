package com.tradable.exampleApps.createPosition;

import java.util.Collection;

import javax.swing.JTextField;

import com.tradable.api.entities.Instrument;
import com.tradable.api.services.instrument.InstrumentService;
import com.tradable.api.services.instrument.InstrumentServiceListener;
import com.tradable.api.services.instrument.InstrumentUpdateEvent;
import com.tradable.api.services.marketdata.Quote;
import com.tradable.api.services.marketdata.QuoteTickEvent;
import com.tradable.api.services.marketdata.QuoteTickListener;
import com.tradable.api.services.marketdata.QuoteTickService;
import com.tradable.api.services.marketdata.QuoteTickSubscription;

public class InstrumentAndMarketData implements InstrumentServiceListener, 
QuoteTickListener{

	//We here declare the objects that will be used for for monitoring the user account's
	//instruments and for monitoring the bid and ask prices of an instrument once we have
	//subscribed to listening to changes in it.
	//The two Quote objects will be used once an instrument is identified to update and
	//display the latest bid and ask prices.
	//==================================================================================
	private InstrumentService instrumentService;
	private QuoteTickService quoteTickService;
	private QuoteTickSubscription tickSubscription;
	
	private Instrument currentInstrument;
	private Quote bid;
	private Quote ask;
	private JTextField bidTextField; 
	private JTextField askTextField;
	
	//==================================================================================	


	public InstrumentAndMarketData(InstrumentService instrumentService, QuoteTickService quoteTickService, 
			JTextField bidTextField, JTextField askTextField){
		this.instrumentService = instrumentService;
		this.quoteTickService = quoteTickService;
		this.bidTextField = bidTextField;
		
		
		//We note, for the QuoteTickService interface, the only available method to its objects
		//is the createSubscription() one. Then the subscription listens for the changes in the
		//this object.
		//====================================================================================	
		
		this.instrumentService.addListener(this);	
		tickSubscription = this.quoteTickService.createSubscription();
        tickSubscription.setListener(this);  //num: subscription now listens to this (as I am implementing the quoteTickListener)
		
	}
	
	

	public void destroy(){
		this.instrumentService.removeListener(this);
		tickSubscription.destroy();
	}
	
	public void setCurrentInstrument(Instrument currentInstrument){
		this.currentInstrument = currentInstrument;
	}
	
	public void setCurrentTickSubscriptionSymbol(){
		tickSubscription.setSymbol(currentInstrument.getSymbol());
	}
	
	public void setCurrentQuotes(){
        ask = tickSubscription.getAsk(currentInstrument.getSymbol());            
        bid = tickSubscription.getBid(currentInstrument.getSymbol());
	}
	
	public Quote getCurrentAsk(){
		return ask;
	}
	
	public Quote getCurrentBid(){
		return bid;
	}
	
	public Collection<Instrument> getAccountInstruments(){
		return instrumentService.getInstruments();
	}
	
	public Instrument getAccountInstrument(String symbol){
		return instrumentService.getInstrument(symbol);
	}
	
	public Instrument getCurrentInstrument(){
		return currentInstrument;
	}
	
	
	//========================================(2)========================================//
	//Remember that instrument events occur only when the user switches session accounts
	//or when the list is changed. This will almost never happen while you are running this
	//example, so no need to implement this method.
	//====================================================================================	
	
    @Override
    public void instrumentsUpdated(InstrumentUpdateEvent event) { 
        
    }
    
	@Override
	public void quotesUpdated(QuoteTickEvent event) {
		
		//Quote objects have to be updated in order to get the correct
		//current prices. Not the subscription object is updated by the
		//container, thus the only way to update the Quote objects is to use it
		this.setCurrentQuotes();

		bidTextField.setText("currBid: " + String.valueOf(bid.getPrice()));	
		askTextField.setText("currAsk: " + String.valueOf(ask.getPrice()));     
		
	}
    //====================================================================================
    //====================================================================================	

}
