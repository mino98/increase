package it.minux.increase.layers;

import it.minux.increase.data.UserLocation;

import java.util.List;

public class UsersLocationsLayer
	extends HeatmapLayer<UserLocation>
{
	public UsersLocationsLayer(List<UserLocation> locations) {
		super(locations);
		setName("Users Locations"); // TODO i18n
		setPickEnabled(false);
	}
}
