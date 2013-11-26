package lab.davidahn.appshuttle.report;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lab.davidahn.appshuttle.AppShuttleApplication;
import android.content.SharedPreferences;

public class Reporter {
	private String senderAddr;
	private String senderPwd;
	private String[] receivers;
	private String subject;
	private String body;
	private List<File> attachList = new ArrayList<File>();

	public Reporter(String[] receivers, String subject, String body){
		SharedPreferences preference = AppShuttleApplication.getContext().getPreferences();
		senderAddr = preference.getString("email.sender.addr", "appshuttle2@gmail.com");
		senderPwd = preference.getString("email.sender.pwd", "appshuttle2@");
		this.receivers = receivers;
		this.subject = subject;
		this.body = body;
	}
	
	public void addAttach(File file){
		attachList.add(file);
	}

	public boolean report() throws Exception {
		Mail m = new Mail(senderAddr, senderPwd);
		m.setFrom(senderAddr);
		m.setTo(receivers);
		m.setSubject(subject);
		m.setBody(body);
		
		Iterator<File> it = attachList.iterator();
		while(it.hasNext()) 
			m.addAttachment(it.next().getAbsolutePath());
		
		return m.send();
	}
}
