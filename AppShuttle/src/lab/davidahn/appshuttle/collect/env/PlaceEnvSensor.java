package lab.davidahn.appshuttle.collect.env;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import android.location.Address;
import android.location.Geocoder;

public class PlaceEnvSensor extends BaseEnvSensor {
	private UserPlace prevUPlace;
	private UserPlace currUPlace;
    private DurationUserEnv.Builder durationUserEnvBuilder;
	
    private static PlaceEnvSensor placeEnvSensor = new PlaceEnvSensor();

    private PlaceEnvSensor(){
    	super();
		prevUPlace = currUPlace = InvalidUserPlace.getInstance();
	}
	
	public static PlaceEnvSensor getInstance(){
		return placeEnvSensor;
	}
	
	@Override
	public UserPlace sense(Date currTimeDate, TimeZone currTimeZone){
		prevUPlace = currUPlace;
		
		LocEnvSensor locEnvSensor = LocEnvSensor.getInstance();
		UserLoc currLoc = locEnvSensor.getCurrULoc();
		
		currUPlace = InvalidUserPlace.getInstance();
		
		if(!currLoc.isValid()) {
			return currUPlace;
		}
		
		if(!locEnvSensor.isChanged() && prevUPlace.isValid()){
			currUPlace = prevUPlace;
//			Log.d("place", "(continue) "+currUPlace.toString());
			return currUPlace;
		}
		
		double currLocLatitude, currLocLongitude;
		try {
			currLocLatitude = currLoc.getLatitude();
			currLocLongitude = currLoc.getLongitude();
		} catch (InvalidUserEnvException e) {
			throw new RuntimeException("current location is valid. no reachable.");
		}

		try {
	    	Geocoder _geocoder = new Geocoder(_appShuttleContext);
    		List<Address> geocoded = _geocoder.getFromLocation(currLocLatitude, currLocLongitude, 1);
			if(geocoded == null || geocoded.isEmpty()) {
//				Log.d("place", "(geocode = null) "+currUPlace.toString());
				return currUPlace;
			}
		
			Address addr = geocoded.get(0);
			String addressLine = addr.getAddressLine(0);
			
			if(addressLine == null) {
//				Log.d("place", "(addressLine = null) "+currUPlace.toString());
				return currUPlace;
			}

			String adminArea = addr.getAdminArea();
			String subAdminArea = addr.getSubAdminArea();
			String locality = addr.getLocality();
			String subLocality = addr.getSubLocality();
			
			StringBuilder sb = new StringBuilder();
			if(adminArea != null)
				sb.append(" ").append(adminArea);
			if(subAdminArea != null)
				sb.append(" ").append(subAdminArea);
			if(locality != null)
				sb.append(" ").append(locality);
			if(subLocality != null)
				sb.append(" ").append(subLocality);
			
			String placeName = sb.toString();

			UserLoc coordinates = InvalidUserLoc.getInstance();
			if(addr.hasLongitude() && addr.hasLatitude())
				coordinates = UserLoc.create(addr.getLatitude(), addr.getLongitude());

			currUPlace = UserPlace.create(placeName, coordinates);
			
//			Log.i("place", currUPlace.toString());
			
			return currUPlace;
		} catch (IOException e) {
//			Log.d("place", "(Geocoder IOException) "+currUPlace.toString());

			return currUPlace;
		}
	}
	
	@Override
	public boolean isChanged(){
		if(!currUPlace.equals(prevUPlace)) {
//			Log.i("user env", "place moved");
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public List<DurationUserEnv> preExtractDurationUserEnv(Date currTimeDate, TimeZone currTimeZone) {
		return Collections.emptyList();
	}

	@Override
	public DurationUserEnv extractDurationUserEnv(Date currTimeDate, TimeZone currTimeZone, UserEnv uEnv) {
		DurationUserEnv res = null;
		if(durationUserEnvBuilder == null) {
			durationUserEnvBuilder = makeDurationUserEnvBuilder(currTimeDate, currTimeZone, uEnv);
		} else {
			if(isChanged()/* || LocEnvSensor.getInstance().isChanged()*/ || isAutoExtractionTime(currTimeDate, currTimeZone)){
				res = durationUserEnvBuilder.setEndTime(currTimeDate).setTimeZone(currTimeZone).build();
				durationUserEnvBuilder = makeDurationUserEnvBuilder(currTimeDate, currTimeZone, uEnv);
			}
		}
		return res;
	}
	
	@Override
	public DurationUserEnv postExtractDurationUserEnv(Date currTimeDate, TimeZone currTimeZone) {
		DurationUserEnv res = durationUserEnvBuilder.setEndTime(currTimeDate).setTimeZone(currTimeZone).build();
		durationUserEnvBuilder = null;
		return res;
	}
	
	private DurationUserEnv.Builder makeDurationUserEnvBuilder(Date currTimeDate, TimeZone currTimeZone, UserEnv uEnv) {
		return new DurationUserEnv.Builder()
			.setTime(currTimeDate)
			.setEndTime(currTimeDate)
			.setTimeZone(currTimeZone)
			.setEnvType(uEnv.getEnvType())
			.setUserEnv(uEnv);
	}
}
	
//	public DurationUserEnv extractDurationUserEnv(SnapshotUserCxt uCxt) {
//		DurationUserEnv res = null;
//		if(durationUserEnvBuilder == null) {
//			durationUserEnvBuilder = makeDurationUserEnvBuilder(uCxt);
//		} else {
//			if(isChanged()){
//				durationUserEnvBuilder.setEndTime(uCxt.getTimeDate());
//				res = durationUserEnvBuilder.build();
//				durationUserEnvBuilder = makeDurationUserEnvBuilder(uCxt);
//			}
//		}
//		return res;
//	}
//	
//	private DurationUserEnv.Builder makeDurationUserEnvBuilder(SnapshotUserCxt uCxt) {
//		return new DurationUserEnv.Builder()
//			.setTime(uCxt.getTimeDate())
//			.setEndTime(uCxt.getTimeDate())
//			.setTimeZone(uCxt.getTimeZone())
//			.setUserEnv(uCxt.getUserEnv(EnvType.PLACE));
//	}
//}

//StringTokenizer st = new StringTokenizer(addressLine);
//int numWord = preferenceSettings.getInt("collection.place.num_address_prefix_words", 3);

//while(st.hasMoreTokens() && numWord-- > 0){
//	sb.append(st.nextToken()).append(" ");
//}
//sb.deleteCharAt(sb.length()-1);
//String countryName = addr.getCountryName();
//if(countryName != null)
//	sb.append(countryName);