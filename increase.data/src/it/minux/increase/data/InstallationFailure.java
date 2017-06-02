package it.minux.increase.data;

import it.minux.increase.xml.InstallationFailureType;

public class InstallationFailure 
	extends PointEvent
{
	public InstallationFailure(InstallationFailureType event) {
		super(event);
	}
}
