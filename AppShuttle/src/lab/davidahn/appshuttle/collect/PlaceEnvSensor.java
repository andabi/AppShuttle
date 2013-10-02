package lab.davidahn.appshuttle.collect;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import lab.davidahn.appshuttle.context.env.DurationUserEnv;
import lab.davidahn.appshuttle.context.env.InvalidUserEnvException;
import lab.davidahn.appshuttle.context.env.UserEnv;
import lab.davidahn.appshuttle.context.env.UserLoc;
import lab.davidahn.appshuttle.context.env.UserLoc.UserLocValidity;
import lab.davidahn.appshuttle.context.env.UserPlace;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

public class PlaceEnvSensor extends BaseEnvSensor {
	private UserPlace _prevUPlace;
	private UserPlace _currUPlace;
    private DurationUserEnv.Builder _durationUserEnvBuilder;
	
    private static PlaceEnvSensor placeEnvSensor = new PlaceEnvSensor();

    private PlaceEnvSensor(){
    	super();
		_prevUPlace = _currUPlace = null;
	}
	
	public static PlaceEnvSensor getInstance(){
		return placeEnvSensor;
	}
	
	public UserPlace sense(){
		_prevUPlace = _currUPlace;
		
		LocEnvSensor _locEnvCollector = LocEnvSensor.getInstance();
		UserLoc currLoc = _locEnvCollector.getCurrULoc();
		
		_currUPlace = UserPlace.create(null, currLoc);
		
		if(!currLoc.isValid()) {
			return _currUPlace;
		}
		
		if(_prevUPlace != null && !_locEnvCollector.isChanged( /* && _prevUPlace.isValid() */ )){
			_currUPlace = _prevUPlace;
			
			return _currUPlace;
		}
		
		double currLocLongitude, currLocLatitude;
		try {
			currLocLatitude = currLoc.getLatitude();
			currLocLongitude = currLoc.getLongitude();
		} catch (InvalidUserEnvException e) {
			throw new RuntimeException("current location is not invalid");
		}

		try {
	    	Geocoder _geocoder = new Geocoder(_appShuttleContext);
    		List<Address> geocoded = _geocoder.getFromLocation(currLocLatitude, currLocLongitude, 1);
			if(geocoded == null || geocoded.isEmpty())
				return _currUPlace;
		
			Address addr = geocoded.get(0);
			String addressLine = addr.getAddressLine(0);
			
			if(addressLine == null) {
				return _currUPlace;
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
			
			UserLocValidity coordinatesValidity = addr.hasLongitude() && addr.hasLatitude() ? UserLocValidity.VALID : UserLocValidity.INVALID;
			UserLoc coordinates = UserLoc.create(coordinatesValidity, addr.getLongitude(), addr.getLatitude());
			_currUPlace = UserPlace.create(placeName, coordinates);
			
			return _currUPlace;
		} catch (IOException e) {
			return _currUPlace;
		}
	}
	
	public boolean isChanged(){
		if(_prevUPlace == null)
			return false;
		
		try {
			if(!_currUPlace.isSame(_prevUPlace)) {
				Log.i("user env", "place moved");
				return true;
			} else {
				return false;
			}
		} catch (InvalidUserEnvException e) {
			return false;
		}
	}
	
	public List<DurationUserEnv> preExtractDurationUserEnv(Date currTimeDate,
			TimeZone currTimeZone) {
		return Collections.emptyList();
	}

	public DurationUserEnv extractDurationUserEnv(Date currTimeDate, TimeZone currTimeZone, UserEnv uEnv) {
		DurationUserEnv res = null;
		if(_durationUserEnvBuilder == null) {
			_durationUserEnvBuilder = makeDurationUserEnvBuilder(currTimeDate, currTimeZone, uEnv);
		} else {
			if(isChanged()){
				res = _durationUserEnvBuilder.setEndTime(currTimeDate).setTimeZone(currTimeZone).build();
				_durationUserEnvBuilder = makeDurationUserEnvBuilder(currTimeDate, currTimeZone, uEnv);
			}
		}
		return res;
	}
	
	public DurationUserEnv postExtractDurationUserEnv(Date currTimeDate, TimeZone currTimeZone) {
		DurationUserEnv res = _durationUserEnvBuilder.setEndTime(currTimeDate).setTimeZone(currTimeZone).build();
		_durationUserEnvBuilder = null;
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