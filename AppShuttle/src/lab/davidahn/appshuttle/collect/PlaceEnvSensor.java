package lab.davidahn.appshuttle.collect;

import lab.davidahn.appshuttle.context.SnapshotUserCxt;
import lab.davidahn.appshuttle.context.env.DurationUserEnv;
import lab.davidahn.appshuttle.context.env.EnvType;
import lab.davidahn.appshuttle.context.env.InvalidLocationException;
import lab.davidahn.appshuttle.context.env.PlaceUserEnv;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class PlaceEnvSensor implements EnvSensor {
	private static PlaceEnvSensor placeEnvSensor;
	private PlaceUserEnv prevUPlace;
	private PlaceUserEnv currUPlace;
	private LocEnvSensor locEnvCollector;
    private SharedPreferences settings;
    private DurationUserEnv.Builder durationUserEnvBuilder;
	
	private PlaceEnvSensor(Context cxt){
		settings = cxt.getSharedPreferences("AppShuttle", Context.MODE_PRIVATE);
		locEnvCollector = LocEnvSensor.getInstance(cxt);
		prevUPlace = null;
		currUPlace = null;
	}
	
	public static PlaceEnvSensor getInstance(Context cxt){
		if(placeEnvSensor == null) placeEnvSensor = new PlaceEnvSensor(cxt);
		return placeEnvSensor;
	}
	
	//TODO check about invalid loc
	public PlaceUserEnv sense(){
		prevUPlace = currUPlace;
		currUPlace = new PlaceUserEnv(locEnvCollector.getCurrULoc().getLoc());
		return currUPlace;
//		if(GlobalState.prevUCxt == null){
//			GlobalState.placeMoved = false;
//			currUPlace = currULoc;
//		} else {
//			UserLoc prevUPlace = ((PlaceUserEnv) GlobalState.prevUCxt.getUserEnv(EnvType.PLACE)).getPlace();
//			if(prevUPlace == null){
//				GlobalState.placeMoved = false;
//				currUPlace = currULoc;
//			} else {
//				try {
//					if(!currULoc.proximity(prevUPlace, settings.getInt("collection.place.tolerance.distance", 5000))) {
//						currUPlace = currULoc;
//						GlobalState.placeMoved = true;
//						Log.i("changed env", "place moved");
//					} else {
//						currUPlace = prevUPlace;
//						GlobalState.placeMoved = false;
//					}
//				} catch (InvalidLocationException e) {
//					;
//				}
//			}
//		}
//		uCxt.addUserEnv(EnvType.PLACE, new PlaceUserEnv(currUPlace));
	}
	
	public boolean isChanged(){
		boolean changed = false;
		if(prevUPlace != null){
//			UserLoc prevUPlace = ((PlaceUserEnv) GlobalState.prevUCxt.getUserEnv(EnvType.PLACE)).getPlace();
//			if(prevUPlace == null){
//				GlobalState.placeMoved = false;
//				currUPlace = currULoc;
//			} else {
			try {
				if(!currUPlace.getPlace().proximity(prevUPlace.getPlace(), settings.getInt("collection.place.tolerance.distance", 5000))) {
//					currUPlace = currULoc;
					changed = true;
//					GlobalState.placeMoved = true;
					Log.i("changed env", "place moved");
//				} else {
//					currUPlace = prevUPlace;
//					GlobalState.placeMoved = false;
				}
			} catch (InvalidLocationException e) {
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
				durationUserEnvBuilder.setEndTime(uCxt.getTime());
				res = durationUserEnvBuilder.build();
				durationUserEnvBuilder = makeDurationUserEnvBuilder(uCxt);
			}
		}
		return res;
	}
	
	private DurationUserEnv.Builder makeDurationUserEnvBuilder(SnapshotUserCxt uCxt) {
		return new DurationUserEnv.Builder()
			.setTime(uCxt.getTime())
			.setEndTime(uCxt.getTime())
			.setTimeZone(uCxt.getTimeZone())
			.setUserEnv(uCxt.getUserEnv(EnvType.PLACE));
	}
}
