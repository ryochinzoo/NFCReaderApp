package com.example.nfcreaderfornpo;

import java.util.Date;

public class nfclistdata {
	private String participantname;
	private Date attendate;
	private String id;
	
	public void setName(String name){
		this.participantname = name;
	}
	
	public void setDate(Date date){
		this.attendate = date;
	}
	
	public String getName(){
		return this.participantname;
	}
	
	public Date getDate(){
		return this.attendate;
	}

	public String getId() {
		
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
		
	}
}
