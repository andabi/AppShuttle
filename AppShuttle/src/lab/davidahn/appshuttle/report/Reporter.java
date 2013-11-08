package lab.davidahn.appshuttle.report;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import lab.davidahn.appshuttle.AppShuttleApplication;
import lab.davidahn.appshuttle.R;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class Reporter {
	private AppShuttleApplication _appShuttleContext;
	private SharedPreferences _preferenceSettings;
	private Handler _handler;
	private AccountManager _accountManager;
	
	private List<File> _attachList = new ArrayList<File>();

	public Reporter(Handler handler){
		_appShuttleContext = AppShuttleApplication.getContext();
		_preferenceSettings = _appShuttleContext.getPreferences();
		_handler = handler;
		_accountManager = AccountManager.get(_appShuttleContext);
	}
	
	public void addAttach(File file){
		_attachList.add(file);
	}

	public boolean report() {
		String[] toArr = {_preferenceSettings.getString("email.receiver.addr", "andabi412@gmail.com")};
		String subject = "[appShuttle] ";
		Account[] account = _accountManager.getAccounts();
		if(account.length <= 0) subject+="unknown";
		else subject+=account[0].name+" ("+account[0].type+")";
		Date currentTime = new Date(System.currentTimeMillis());
		String body = "From "+account[0].name+" ("+account[0].type+")"+" at "+currentTime.toString();		
		return sendEmailTo(toArr, subject, body, _attachList);
	}
	
	private boolean sendEmailTo(String[] toArr, String subject, String body,
			List<File> attachment) {
		Mail m = new Mail(_preferenceSettings.getString("email.sender.addr", "davidahn412@gmail.com"), _preferenceSettings.getString("email.sender.pwd", "rnrmfepdl"));
	
		m.setTo(toArr);
		m.setFrom(_preferenceSettings.getString("email.sender.addr", "davidahn412@gmail.com"));
		m.setSubject(subject);
		m.setBody(body);
		Iterator<File> it = attachment.iterator();
		try {
			while(it.hasNext()) m.addAttachment(it.next().getAbsolutePath());
			if (m.send()) {
				Log.i("mail", _appShuttleContext.getResources().getString(R.string.report_success_msg));
				_handler.post(new Runnable(){
					@Override
					public void run(){
						Toast.makeText(_appShuttleContext, _appShuttleContext.getResources().getString(R.string.report_success_msg), Toast.LENGTH_SHORT).show();						
					}
				});
				return true;
			} else {
				Log.d("mail", _appShuttleContext.getResources().getString(R.string.report_failure_msg));
				_handler.post(new Runnable(){
					@Override
					public void run(){
						Toast.makeText(_appShuttleContext, _appShuttleContext.getResources().getString(R.string.report_failure_msg), Toast.LENGTH_SHORT).show();						
					}
				});
			}
		} catch (Exception e) {			
			Log.e("mail", "Could not send email", e);
			_handler.post(new Runnable(){
				@Override
				public void run(){
					Toast.makeText(_appShuttleContext, _appShuttleContext.getResources().getString(R.string.report_failure_msg), Toast.LENGTH_SHORT).show();						
				}
			});
		}
		return false;
	}
}
