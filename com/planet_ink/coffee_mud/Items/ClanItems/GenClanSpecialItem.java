package com.planet_ink.coffee_mud.Items.ClanItems;


import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GenClanSpecialItem extends StdClanSpecialItem
{
	public String ID(){	return "GenClanSpecialItem";}
	protected String readableText="";
	public GenClanSpecialItem()
	{
		super();
		setName("a generic clan item");
		setDisplayText("a generic clan item sits here.");
		setDescription("");
		baseEnvStats().setWeight(2);
		setMaterial(EnvResource.RESOURCE_OAK);
		recoverEnvStats();
		isReadable=false;
	}
	
	public Environmental newInstance()
	{
		return new GenClanSpecialItem();
	}
	public boolean isGeneric(){return true;}

	public String text()
	{
		return CoffeeMaker.getPropertiesStr(this,false);
	}

	public String readableText(){return readableText;}
	public void setReadableText(String text){readableText=text;}
	public void setMiscText(String newText)
	{
		miscText="";
		CoffeeMaker.setPropertiesStr(this,newText,false);
		recoverEnvStats();
	}
	private static String[] MYCODES={"CLANID","CITYPE"};
	public String getStat(String code)
	{
		if(CoffeeMaker.getGenItemCodeNum(code)>=0)
			return CoffeeMaker.getGenItemStat(this,code);
		else
		switch(getCodeNum(code))
		{
		case 0: return clanID();
		case 1: return ""+ciType();
		}
		return "";
	}
	public void setStat(String code, String val)
	{
		if(CoffeeMaker.getGenItemCodeNum(code)>=0)
			CoffeeMaker.setGenItemStat(this,code,val);
		else
		switch(getCodeNum(code))
		{
		case 0: setClanID(val); break;
		case 1: setCIType(Util.s_int(val)); break;
		}
	}
	protected int getCodeNum(String code){
		for(int i=0;i<MYCODES.length;i++)
			if(code.equalsIgnoreCase(MYCODES[i])) return i;
		return -1;
	}
	private static String[] codes=null;
	public String[] getStatCodes()
	{
		if(codes!=null) return codes;
		String[] superCodes=CoffeeMaker.GENITEMCODES;
		codes=new String[superCodes.length+MYCODES.length];
		int i=0;
		for(;i<superCodes.length;i++)
			codes[i]=superCodes[i];
		for(int x=0;x<MYCODES.length;i++,x++)
			codes[i]=MYCODES[x];
		return codes;
	}
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof GenClanItem)) return false;
		String[] codes=getStatCodes();
		for(int i=0;i<codes.length;i++)
			if(!E.getStat(codes[i]).equals(getStat(codes[i])))
				return false;
		return true;
	}
}
