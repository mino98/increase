package it.minux.increase.data;

import it.minux.increase.xml.SupportRequestType;

public class SupportRequest 
	extends PointEvent
{
	public SupportRequest(SupportRequestType request) {
		super(request);
	}
}
