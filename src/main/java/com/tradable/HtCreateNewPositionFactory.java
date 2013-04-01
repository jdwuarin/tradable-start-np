package com.tradable;

import com.tradable.api.component.WorkspaceModule;
import com.tradable.api.component.WorkspaceModuleCategory;
import com.tradable.api.component.WorkspaceModuleFactory;

public class HtCreateNewPositionFactory implements WorkspaceModuleFactory{

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	@Override
	public WorkspaceModule createModule() {
		// TODO Auto-generated method stub
		return null;
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
