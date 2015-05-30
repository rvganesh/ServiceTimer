package com.fourtime.bean;

public class Item{
	public Item(String item, long original, String value){
		this.item = item;
		this.value = value;
		this.original = original;
	}
	public String item, value;
	public long original;
}