package lab.davidahn.appshuttle.collect.env;

import java.io.IOException;
import java.util.List;
import java.util.TimeZone;

import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

public class PlaceEnvSensor extends BaseEnvSensor {

	private static PlaceEnvSensor placeEnvSensor = new PlaceEnvSensor();

    private PlaceEnvSensor(){
    	super();
		prevEnv = currEnv = InvalidUserPlace.getInstance();
	}
	
	public static PlaceEnvSensor getInstance(){
		return placeEnvSensor;
	}
	
	@Override
	public UserPlace sense(long currTime, TimeZone currTimeZone){
		UserPlace place = InvalidUserPlace.getInstance();
		
		LocEnvSensor locEnvSensor = LocEnvSensor.getInstance();
		UserLoc currLoc = (UserLoc)locEnvSensor.getCurrEnv();
		if(!currLoc.isValid())
			return place;
		
		if(!locEnvSensor.isChanged() && prevEnv.isValid()){
			place = (UserPlace)prevEnv;
			Log.d("place", "(continue) " + place.toString());
			return place;
		}
		
		double currLocLatitude, currLocLongitude;
		try {
			currLocLatitude = currLoc.getLatitude();
			currLocLongitude = currLoc.getLongitude();
		} catch (InvalidUserEnvException e) {
			throw new RuntimeException("current location is valid. no reachable.");
		}

		try {
	    	Geocoder geocoder = new Geocoder(cxt);
    		List<Address> geocoded = geocoder.getFromLocation(currLocLatitude, currLocLongitude, 1);
			if(geocoded == null || geocoded.isEmpty()) {
				Log.d("place", "(geocode = null) " + place.toString());
				return place;
			}
		
			Address addr = geocoded.get(0);
			String addressLine = addr.getAddressLine(0);
			
			if(addressLine == null) {
				Log.d("place", "(addressLine = null) " + place.toString());
				return place;
			}

			//TODO
			String adminArea = addr.getAdminArea();
			String subAdminArea = addr.getSubAdminArea();
			String locality = addr.getLocality();
			String subLocality = addr.getSubLocality();
			String thoroughfare = addr.getThoroughfare();
			String subThoroughfare = addr.getSubThoroughfare();
			
			StringBuilder sb = new StringBuilder();
			if(adminArea != null)
				sb.append(" ").append(adminArea);
			if(subAdminArea != null)
				sb.append(" ").append(subAdminArea);
			if(locality != null)
				sb.append(" ").append(locality);
			if(subLocality != null)
				sb.append(" ").append(subLocality);
			if(thoroughfare != null)
				sb.append(" ").append(thoroughfare);
			if(subThoroughfare != null)
				sb.append(" ").append(subThoroughfare);
			String placeName = sb.toString();

			UserLoc coordinates = InvalidUserLoc.getInstance();
			if(addr.hasLongitude() && addr.hasLatitude())
				coordinates = UserLoc.create(addr.getLatitude(), addr.getLongitude());
			place = UserPlace.create(placeName, coordinates);
			Log.i("place", place.toString());
		} catch (IOException e) {
			Log.d("place", "(Geocoder IOException) "+ place.toString());
		}
		return place;
	}
}

//StringTokenizer st = new StringTokenizer(addressLine);
//int numWord = preferenceSettings.getInt("collection.env.place.num_address_prefix_words", 3);

//while(st.hasMoreTokens() && numWord-- > 0){
//	sb.append(st.nextToken()).append(" ");
//}
//sb.deleteCharAt(sb.length()-1);
//String countryName = addr.getCountryName();
//if(countryName != null)
//	sb.append(countryName);