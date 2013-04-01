package com.tradable;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.tradable.api.component.WorkspaceModule;
import com.tradable.api.component.state.PersistedStateHolder;

public class HtCreateNewPosition extends JPanel implements WorkspaceModule{
	private static final String TITLE = "Create a new Position";
	
	public HtCreateNewPosition() {
		
		setLayout(null);
		setSize(250, 250);
		
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
