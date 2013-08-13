package lab.davidahn.appshuttle.report;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import lab.davidahn.appshuttle.R;
import lab.davidahn.appshuttle.utils.Mail;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class Reporter {
	private AccountManager accountManager;
	private Context cxt;
	List<File> attachList = new ArrayList<File>();
	private Handler handler;
	SharedPreferences settings;

	public Reporter(Context cxt, Handler handler){
		this.cxt = cxt;
		accountManager = AccountManager.get(cxt);
		this.handler = handler;
		settings = cxt.getSharedPreferences("AppShuttle", Context.MODE_PRIVATE);
	}
	
	public void addAttach(File file){
		attachList.add(file);
	}

	public boolean report() {
		String[] toArr = {settings.getString("email.receiver.addr", "andabi412@gmail.com")};
		String subject = "[appShuttle] ";
		Account[] account = accountManager.getAccounts();
		if(account.length <= 0) subject+="unknown";
		else subject+=account[0].name+" ("+account[0].type+")";
		Date currentTime = new Date(System.currentTimeMillis());
		String body = "From "+account[0].name+" ("+account[0].type+")"+" at "+currentTime.toString();		
		return sendEmailTo(toArr, subject, body, attachList);
	}
	
	private boolean sendEmailTo(String[] toArr, String subject, String body,
			List<File> attachment) {
		Mail m = new Mail(settings.getString("email.sender.addr", "davidahn412@gmail.com"), settings.getString("email.sender.pwd", "rnrmfepdl"));
	
		m.setTo(toArr);
		m.setFrom(settings.getString("email.sender.addr", "davidahn412@gmail.com"));
		m.setSubject(subject);
		m.setBody(body);
		Iterator<File> it = attachment.iterator();
		try {
			while(it.hasNext()) m.addAttachment(it.next().getAbsolutePath());
			if (m.send()) {
				Log.i("mail", cxt.getResources().getString(R.string.report_success_msg));
				handler.post(new Runnable(){
					@Override
					public void run(){
						Toast.makeText(cxt, cxt.getResources().getString(R.string.report_success_msg), Toast.LENGTH_SHORT).show();						
					}
				});
				return true;
			} else {
				Log.d("mail", cxt.getResources().getString(R.string.report_failure_msg));
				handler.post(new Runnable(){
					@Override
					public void run(){
						Toast.makeText(cxt, cxt.getResources().getString(R.string.report_failure_msg), Toast.LENGTH_SHORT).show();						
					}
				});
			}
		} catch (Exception e) {			
			Log.e("mail", "Could not send email", e);
			handler.post(new Runnable(){
				@Override
				public void run(){
					Toast.makeText(cxt, cxt.getResources().getString(R.string.report_failure_msg), Toast.LENGTH_SHORT).show();						
				}
			});
		}
		return false;
	}
}
