package com.ngage.beeldvan.model;

import java.util.List;

public class LocationData {
	private String name;
	private int singular ;
	private int cid;
	private List<Locations> locations;
	
	public LocationData() {
		// TODO Auto-generated constructor stub
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getSingular() {
		return singular;
	}
	public void setSingular(int singular) {
		this.singular = singular;
	}
	public int getCid() {
		return cid;
	}
	public void setCid(int cid) {
		this.cid = cid;
	}
	public List<Locations> getLocations() {
		return locations;
	}
	public void setLocations(List<Locations> locations) {
		this.locations = locations;
	}

}
