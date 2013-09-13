package lab.davidahn.appshuttle.collect;

import java.io.IOException;
import java.util.List;
import java.util.StringTokenizer;

import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.context.SnapshotUserCxt;
import lab.davidahn.appshuttle.context.env.DurationUserEnv;
import lab.davidahn.appshuttle.context.env.EnvType;
import lab.davidahn.appshuttle.context.env.InvalidUserEnvException;
import lab.davidahn.appshuttle.context.env.PlaceUserEnv;
import lab.davidahn.appshuttle.context.env.UserLoc;
import lab.davidahn.appshuttle.context.env.UserLoc.Validity;
import lab.davidahn.appshuttle.context.env.UserPlace;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

public class PlaceEnvSensor implements EnvSensor {
	private static PlaceEnvSensor placeEnvSensor;
	private Geocoder geocoder;
	private PlaceUserEnv prevUPlace;
	private PlaceUserEnv currUPlace;
	private LocEnvSensor locEnvCollector;
    private DurationUserEnv.Builder durationUserEnvBuilder;
	private SharedPreferences preferenceSettings;
	
	private PlaceEnvSensor(Context cxt){
		preferenceSettings = cxt.getSharedPreferences(cxt.getResources().getString(R.string.app_name), Context.MODE_PRIVATE);
		geocoder = new Geocoder(cxt);
		locEnvCollector = LocEnvSensor.getInstance(cxt);
		prevUPlace = null;
		currUPlace = null;
	}
	
	public synchronized static PlaceEnvSensor getInstance(Context cxt){
		if(placeEnvSensor == null) placeEnvSensor = new PlaceEnvSensor(cxt);
		return placeEnvSensor;
	}
	
	public PlaceUserEnv sense(){
		prevUPlace = currUPlace;
		currUPlace = new PlaceUserEnv(new UserPlace(0, 0, null, Validity.INVALID));
		UserLoc currLoc = locEnvCollector.getCurrULoc().getLoc();
		if(prevUPlace != null && 
				prevUPlace.getPlace().isValid() && 
				currLoc.isValid() && 
				!locEnvCollector.isChanged()){
			currUPlace = prevUPlace;
		} else {
			try {
				double currLongitude = currLoc.getLongitude();
				double currLatitude = currLoc.getLatitude();
				try {
					List<Address> geocoded = geocoder.getFromLocation(currLatitude, currLongitude, 1);
					if(geocoded != null && !geocoded.isEmpty()) {
						Address addr = geocoded.get(0);
						String addressLine = addr.getAddressLine(0);
						
						if(addressLine == null) {
							return currUPlace;
						}
							
						StringTokenizer st = new StringTokenizer(addressLine);
						int numWord = preferenceSettings.getInt("collection.place.num_address_prefix_words", 3);
						StringBuilder sb = new StringBuilder();
						while(st.hasMoreTokens() && numWord-- > 0){
							sb.append(st.nextToken()).append(" ");
						}
						sb.deleteCharAt(sb.length()-1);
						String placeName = sb.toString();
						
						double placeLongitude = currLongitude;
						double placeLatitude = currLatitude;
						if(addr.hasLongitude())
							placeLongitude = addr.getLongitude();
						if(addr.hasLatitude())
							placeLatitude = addr.getLatitude();
						currUPlace = new PlaceUserEnv(new UserPlace(placeLongitude, placeLatitude, placeName));
					}
				} catch (IOException e) {
					;
				}
			} catch (InvalidUserEnvException e) {
				;
			}
		}
		return currUPlace;
	}
	
	public boolean isChanged(){
		boolean changed = false;
		if(prevUPlace != null){
			try {
				if(!currUPlace.getPlace().isSame(prevUPlace.getPlace())) {
					changed = true;
					Log.i("changed env", "place moved");
				}
			} catch (InvalidUserEnvException e) {
				;
			}
		}
		return changed;
	}
	
	public DurationUserEnv refineDurationUserEnv(SnapshotUserCxt uCxt) {
		DurationUserEnv res = null;
		if(durationUserEnvBuilder == null) {
			durationUserEnvBuilder = makeDurationUserEnvBuilder(uCxt);
		} else {
			if(isChanged()){
				durationUserEnvBuilder.setEndTime(uCxt.getTimeDate());
				res = durationUserEnvBuilder.build();
				durationUserEnvBuilder = makeDurationUserEnvBuilder(uCxt);
			}
		}
		return res;
	}
	
	private DurationUserEnv.Builder makeDurationUserEnvBuilder(SnapshotUserCxt uCxt) {
		return new DurationUserEnv.Builder()
			.setTime(uCxt.getTimeDate())
			.setEndTime(uCxt.getTimeDate())
			.setTimeZone(uCxt.getTimeZone())
			.setUserEnv(uCxt.getUserEnv(EnvType.PLACE));
	}
}

//String countryName = addr.getCountryName();
//if(countryName != null)
//	sb.append(countryName);
//
//String adminArea = addr.getAdminArea();
//if(adminArea != null)
//	sb.append(" ").append(adminArea);
//
//String locality = addr.getLocality();
//if(locality != null)
//	sb.append(" ").append(locality);
//
//StringTokenizer st = new StringTokenizer(sb.toString());
//if(st.countTokens() < 4){
//	String featureName = addr.getFeatureName();
//	if(featureName != null)
//		sb.append(" ").append(featureName);
//}
