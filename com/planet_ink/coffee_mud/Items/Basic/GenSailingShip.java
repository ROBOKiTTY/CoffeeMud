package com.planet_ink.coffee_mud.Items.Basic;
import com.planet_ink.coffee_mud.Items.Basic.StdPortal;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Expire;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Move;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

import com.planet_ink.coffee_mud.Libraries.interfaces.*;

/*
   Copyright 2014-2014 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class GenSailingShip extends StdPortal implements PrivateProperty, BoardableShip
{
	@Override public String ID(){	return "GenSailingShip";}
	protected String 			readableText	= "";
	protected String 			owner 			= "";
	protected int 				price 			= 1000;
	protected Area 				area			= null;
	protected volatile int		directionFacing	= -1;
	protected volatile boolean	anchorDown		= true;

	public GenSailingShip()
	{
		super();
		setName("a sailing ship");
		setDisplayText("a sailing ship is here.");
		setMaterial(RawMaterial.RESOURCE_OAK);
		setDescription("");
		myUses=100;
		basePhyStats().setWeight(10000);
		setUsesRemaining(100);
		recoverPhyStats();
		CMLib.flags().setGettable(this, false);
		CMLib.flags().setSavable(this, false);
	}

	@Override 
	public boolean isGeneric()
	{
		return true;
	}

	@Override
	public void recoverPhyStats()
	{
		super.recoverPhyStats();
		if(usesRemaining()>0)
			phyStats().setDisposition(phyStats().disposition()|PhyStats.IS_SWIMMING);
	}
	
	@Override 
	public boolean subjectToWearAndTear()
	{
		return true;
	}
	
	@Override
	public Area getShipArea()
	{
		if(destroyed)
			return null;
		else
		if(area==null)
		{
			area=CMClass.getAreaType("StdBoardableShip");
			final String num=Double.toString(Math.random());
			area.setName(L("UNNAMED_@x1",num.substring(num.indexOf('.')+1)));
			area.setSavable(false);
			area.setTheme(Area.THEME_FANTASY);
			final Room R=CMClass.getLocale("WoodRoom");
			R.setRoomID(area.Name()+"#0");
			R.setSavable(false);
			area.addProperRoom(R);
			readableText=R.roomID();
		}
		return area;
	}

	@Override
	public void setDockableItem(Item dockableItem)
	{
		if(area instanceof BoardableShip)
			((BoardableShip)area).setDockableItem(dockableItem);
	}

	@Override
	public void setShipArea(String xml)
	{
		try
		{
			area=CMLib.coffeeMaker().unpackAreaObjectFromXML(xml);
			if(area instanceof BoardableShip)
			{
				area.setSavable(false);
				((BoardableShip)area).setDockableItem(this);
				for(final Enumeration<Room> r=area.getCompleteMap();r.hasMoreElements();)
					CMLib.flags().setSavable(r.nextElement(), false);
			}
			else
			{
				Log.warnOut("Failed to unpack a sailing ship area for the sailing ship");
				getShipArea();
			}
		}
		catch (final CMException e)
		{
			Log.warnOut("Unable to parse sailing ship xml for some reason.");
		}
	}

	@Override
	public void dockHere(Room R)
	{
		if(!R.isContent(this))
		{
			if(owner()==null)
				R.addItem(this,Expire.Never);
			else
				R.moveItemTo(me, Expire.Never, Move.Followers);
		}
		if (area instanceof BoardableShip)
			((BoardableShip)area).dockHere(R);
	}

	@Override
	public void unDock(boolean moveToOutside)
	{
		final Room R=getIsDocked();
		if(R!=null)
		{
			R.delItem(this);
			setOwner(null);
		}
		if (area instanceof BoardableShip)
			((BoardableShip)area).unDock(moveToOutside);
	}

	@Override
	public Room getIsDocked()
	{
		if (area instanceof BoardableShip)
			return ((BoardableShip)area).getIsDocked();
		if(owner() instanceof Room)
			return ((Room)owner());
		return null;
	}

	@Override 
	public String keyName() 
	{ 
		return readableText;
	}
	
	@Override 
	public void setKeyName(String newKeyName) 
	{ 
		readableText=newKeyName;
	}

	@Override 
	public String readableText()
	{
		return readableText;
	}
	
	@Override 
	public void setReadableText(String text)
	{
		readableText=text;
	}

	@Override
	public String text()
	{
		return CMLib.coffeeMaker().getPropertiesStr(this,false);
	}

	@Override
	public void setMiscText(String newText)
	{
		miscText="";
		CMLib.coffeeMaker().setPropertiesStr(this,newText,false);
		recoverPhyStats();
	}

	@Override
	public CMObject copyOf()
	{
		final GenSailingShip s=(GenSailingShip)super.copyOf();
		s.destroyed=false;
		s.setOwnerName("");
		final String xml=CMLib.coffeeMaker().getAreaObjectXML(getShipArea(), null, null, null, true).toString();
		s.setShipArea(xml);
		return s;
	}

	@Override
	public void stopTicking()
	{
		if(area!=null)
		{
			CMLib.threads().deleteAllTicks(area);
		}
		super.stopTicking();
		this.destroyed=false; // undo the weird thing
	}

	@Override
	protected Room getDestinationRoom()
	{
		getShipArea();
		Room R=null;
		final List<String> V=CMParms.parseSemicolons(readableText(),true);
		if(V.size()>0)
			R=getShipArea().getRoom(V.get(CMLib.dice().roll(1,V.size(),-1)));
		return R;
	}

	@Override
	public void destroy()
	{
		if(area!=null)
			CMLib.map().obliterateArea(area);
		super.destroy();
	}

	@Override 
	public int getPrice() 
	{ 
		return price; 
	}
	
	@Override 
	public void setPrice(int price) 
	{ 
		this.price=price; 
	}
	
	@Override 
	public String getOwnerName() 
	{ 
		return owner; 
	}
	
	@Override 
	public void setOwnerName(String owner) 
	{ 
		this.owner=owner;
	}
	
	@Override
	public CMObject getOwnerObject()
	{
		final String owner=getOwnerName();
		if(owner.length()==0) 
			return null;
		final Clan C=CMLib.clans().getClan(owner);
		if(C!=null) 
			return C;
		return CMLib.players().getLoadPlayer(owner);
	}
	
	@Override public String getTitleID() 
	{ 
		return this.toString(); 
	}

	@Override
	public void renameShip(String newName)
	{
		final Area area=this.area;
		if(area instanceof BoardableShip)
		{
			final Room oldEntry=getDestinationRoom();
			String registryNum=area.getBlurbFlag("REGISTRY");
			if(registryNum==null) 
				registryNum="";
			((BoardableShip)area).renameShip(newName);
			registryNum=Double.toString(Math.random());
			area.addBlurbFlag("REGISTRY Registry#"+registryNum.substring(registryNum.indexOf('.')+1));
			setReadableText(oldEntry.roomID());
			setShipArea(CMLib.coffeeMaker().getAreaObjectXML(area, null, null, null, true).toString());
		}
		for(final String word : new String[]{"NAME","NEWNAME","SHIPNAME","SHIP"})
		{
			for(final String rubs : new String[]{"<>","[]","{}","()"})
			{
				if(Name().indexOf(rubs.charAt(0)+word+rubs.charAt(1))>=0)
					setName(CMStrings.replaceAll(Name(), rubs.charAt(0)+word+rubs.charAt(1), newName));
			}
			for(final String rubs : new String[]{"<>","[]","{}","()"})
			{
				if(displayText().indexOf(rubs.charAt(0)+word+rubs.charAt(1))>=0)
					setDisplayText(CMStrings.replaceAll(displayText(), rubs.charAt(0)+word+rubs.charAt(1), newName));
			}
		}
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((msg.sourceMinor()==CMMsg.TYP_HUH)
		&&(msg.targetMessage()!=null)
		&&(area == CMLib.map().areaLocation(msg.source())))
		{
			List<String> cmds=CMParms.parse(msg.targetMessage());
			if(cmds.size()==0)
				return true;
			final String word=cmds.get(0).toUpperCase();
			final String secondWord=CMParms.combine(cmds,1).toUpperCase();
			if(word.equals("RAISE") && secondWord.equals("ANCHOR"))
			{
				final Room R=CMLib.map().roomLocation(this);
				if(!anchorDown)
					msg.source().tell(L("The anchor is already up."));
				else
				if(R!=null)
				{
					CMMsg msg2=CMClass.getMsg(msg.source(), CMMsg.MSG_NOISYMOVEMENT, "<S-NAME> raise(s) anchor.");
					if((R.okMessage(msg.source(), msg2) && this.okAreaMessage(msg2, true)))
					{
						R.send(msg.source(), msg2);
						this.sendAreaMessage(msg2, true);
					}
				}
				return false;
			}
			else
			if(word.equals("LOWER")  && secondWord.equals("ANCHOR"))
			{
				final Room R=CMLib.map().roomLocation(this);
				if(anchorDown)
					msg.source().tell(L("The anchor is already down."));
				else
				if(R!=null)
				{
					CMMsg msg2=CMClass.getMsg(msg.source(), CMMsg.MSG_NOISYMOVEMENT, "<S-NAME> lower(s) anchor.");
					if((R.okMessage(msg.source(), msg2) && this.okAreaMessage(msg2, true)))
					{
						R.send(msg.source(), msg2);
						this.sendAreaMessage(msg2, true);
					}
				}
				return false;
			}
			else
			if(word.equals("STEER"))
			{
				int dir=Directions.getCompassDirectionCode(secondWord);
				if(dir<0)
				{
					msg.source().tell(L("Steer the ship which direction?"));
					return false;
				}
				final Room R=CMLib.map().roomLocation(this);
				if((anchorDown)||(R==null))
				{
					msg.source().tell(L("The anchor is down, so you won`t be moving anywhere."));
					return false;
				}
				final Room targetRoom=R.getRoomInDir(dir);
				final Exit targetExit=R.getExitInDir(dir);
				if((targetRoom==null)||(targetExit==null)||(!targetExit.isOpen()))
				{
					msg.source().tell(L("There doesn't look to be anything in that direction."));
					return false;
				}
				CMMsg msg2=CMClass.getMsg(msg.source(), CMMsg.MSG_NOISYMOVEMENT, L("<S-NAME> change(s) coarse, steering @x1 @x2.",name(msg.source()),Directions.getDirectionName(dir)));
				if((R.okMessage(msg.source(), msg2) && this.okAreaMessage(msg2, true)))
				{
					R.send(msg.source(), msg2);
					this.sendAreaMessage(msg2, true);
				}
				this.directionFacing=dir;
				return false;
			}
			return true;
		}
		if(!super.okMessage(myHost, msg))
			return false;
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_OPEN:
			case CMMsg.TYP_CLOSE:
			case CMMsg.TYP_LOCK:
			case CMMsg.TYP_UNLOCK:
			{
				msg.setOthersMessage(CMStrings.replaceAll(msg.othersMessage(), "<T-NAME>", L("a hatch on <T-NAME>")));
				msg.setOthersMessage(CMStrings.replaceAll(msg.othersMessage(), "<T-NAMESELF>", L("a hatch on <T-NAMESELF>")));
				msg.setSourceMessage(CMStrings.replaceAll(msg.othersMessage(), "<T-NAME>", L("a hatch on <T-NAME>")));
				msg.setSourceMessage(CMStrings.replaceAll(msg.othersMessage(), "<T-NAMESELF>", L("a hatch on <T-NAMESELF>")));
				msg.setTargetMessage(CMStrings.replaceAll(msg.othersMessage(), "<T-NAME>", L("a hatch on <T-NAME>")));
				msg.setTargetMessage(CMStrings.replaceAll(msg.othersMessage(), "<T-NAMESELF>", L("a hatch on <T-NAMESELF>")));
				break;
			}
			case CMMsg.TYP_WEAPONATTACK:
			{
				if((msg.value() < 2) || (!okAreaMessage(msg,false)))
					return false;
				break;
			}
			}
		}
		return true;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(tickID == Tickable.TICKID_AREA)
		{
			if(amDestroyed())
				return false;
			if((!this.anchorDown) && (directionFacing != -1))
			{
				//TODO: MOVE THE SHIP
				// never really undock -- just redock somewhere else over and over, really!
				
			}
			return true;
		}
		return super.tick(ticking, tickID);
	}
	
	protected synchronized void destroyThisShip()
	{
		if(this.getOwnerName().length()>0)
		{
			final MOB M = CMLib.players().getLoadPlayer(this.getOwnerName());
			final PlayerStats pStats = (M!=null) ? M.playerStats() : null;
			final ItemCollection items= (pStats != null) ? pStats.getExtItems() : null;
			if(items != null)
				items.delItem(this);
		}
		final CMMsg expireMsg=CMClass.getMsg(CMLib.map().deity(), this, CMMsg.MASK_ALWAYS|CMMsg.MSG_EXPIRE, L("<T-NAME> is destroyed!"));
		for(final Enumeration<Room> r = getShipArea().getProperMap(); r.hasMoreElements();)
		{
			final Room R=r.nextElement();
			if(R!=null)
			{
				expireMsg.setTarget(R);
				final Set<MOB> players=CMLib.players().getPlayersHere(R);
				if(players.size()>0)
					for(final MOB M : players)
					{
						//players will get some fancy message when appearing in death room -- which means they should die!
						M.executeMsg(expireMsg.source(), expireMsg);
						CMLib.combat().justDie(expireMsg.source(), M);
					}
				R.send(expireMsg.source(), expireMsg);
			}
		}
		getShipArea().destroy();
		destroy();
	}
	
	protected boolean okAreaMessage(final CMMsg msg, boolean deckOnly)
	{
		boolean failed = false;
		final Area ship=getShipArea();
		if(ship!=null)
		{
			for(final Enumeration<Room> r = ship.getProperMap(); r.hasMoreElements();)
			{
				final Room R=r.nextElement();
				if((!deckOnly)||((R.domainType()&Room.INDOORS)==0))
				{
					failed = failed || R.okMessage(R, msg);
					if(failed)
						break;
				}
			}
		}
		return failed;
	}
	
	protected void sendAreaMessage(final CMMsg msg, boolean deckOnly)
	{
		final Area ship=getShipArea();
		if(ship!=null)
		{
			for(final Enumeration<Room> r = ship.getProperMap(); r.hasMoreElements();)
			{
				final Room R=r.nextElement();
				if((!deckOnly)||((R.domainType()&Room.INDOORS)==0))
					R.sendOthers(msg.source(), msg);
			}
		}
	}
	
	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);

		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_GET:
				if(msg.tool() instanceof ShopKeeper)
					transferOwnership(msg.source());
				break;
			case CMMsg.TYP_WEAPONATTACK: // damage taken
			{
				break;
			}
			case CMMsg.TYP_COLLISION:
			{
				break;
			}
			default:
				break;
			}
		}
		else
		if((msg.targetMinor()==CMMsg.TYP_SELL)
		&&(msg.tool()==this)
		&&(msg.target() instanceof ShopKeeper))
		{
			setOwnerName("");
			recoverPhyStats();
		}
		else
		if((msg.targetMinor()==CMMsg.TYP_GIVE)
		&&(msg.tool()==this)
		&&(getOwnerName().length()>0)
		&&((msg.source().Name().equals(getOwnerName()))
			||(msg.source().getLiegeID().equals(getOwnerName())&&msg.source().isMarriedToLiege())
			||(CMLib.clans().checkClanPrivilege(msg.source(), getOwnerName(), Clan.Function.PROPERTY_OWNER)))
		&&(msg.target() instanceof MOB)
		&&(!(msg.target() instanceof Banker))
		&&(!(msg.target() instanceof Auctioneer))
		&&(!(msg.target() instanceof PostOffice)))
			transferOwnership((MOB)msg.target());
	}

	protected Room findNearestDocks(Room R)
	{
		if(R!=null)
		{
			if(R.domainType()==Room.DOMAIN_OUTDOORS_SEAPORT)
				return R;
			TrackingLibrary.TrackingFlags flags;
			flags = new TrackingLibrary.TrackingFlags()
					.plus(TrackingLibrary.TrackingFlag.AREAONLY)
					.plus(TrackingLibrary.TrackingFlag.NOEMPTYGRIDS)
					.plus(TrackingLibrary.TrackingFlag.NOAIR)
					.plus(TrackingLibrary.TrackingFlag.NOHOMES)
					.plus(TrackingLibrary.TrackingFlag.UNLOCKEDONLY);
			final List<Room> rooms=CMLib.tracking().getRadiantRooms(R, flags, 25);
			for(final Room R2 : rooms)
			{
				if(R2.domainType()==Room.DOMAIN_OUTDOORS_SEAPORT)
					return R2;
			}
			for(final Room R2 : rooms)
			{
				if(R2.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE)
				{
					final Room underWaterR=R.getRoomInDir(Directions.DOWN);
					if((underWaterR!=null)
					&&(R2.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
					&&(R.getExitInDir(Directions.DOWN)!=null)
					&&(R.getExitInDir(Directions.DOWN).isOpen()))
					{
						for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
						{
							final Room adjacentR = underWaterR.getRoomInDir(d);
							final Exit adjacentE = underWaterR.getExitInDir(d);
							if((adjacentR!=null)
							&&(adjacentE!=null)
							&&(adjacentE.isOpen())
							&&(R2.domainType()!=Room.DOMAIN_OUTDOORS_WATERSURFACE)
							&&(R2.domainType()!=Room.DOMAIN_INDOORS_WATERSURFACE)
							&&(R2.domainType()!=Room.DOMAIN_OUTDOORS_UNDERWATER)
							&&(R2.domainType()!=Room.DOMAIN_INDOORS_UNDERWATER))
								return adjacentR;
						}
					}
				}
			}
		}
		return null;
	}

	protected void transferOwnership(final MOB buyer)
	{
		if(CMLib.clans().checkClanPrivilege(buyer, getOwnerName(), Clan.Function.PROPERTY_OWNER))
		{
			final Pair<Clan,Integer> targetClan=CMLib.clans().findPrivilegedClan(buyer, Clan.Function.PROPERTY_OWNER);
			if(targetClan!=null)
				setOwnerName(targetClan.first.clanID());
			else
				setOwnerName(buyer.Name());
		}
		else
			setOwnerName(buyer.Name());
		recoverPhyStats();
		final Session session=buyer.session();
		final Room R=CMLib.map().roomLocation(this);
		if(session!=null)
		{
			final GenSailingShip me=this;
			final InputCallback[] namer=new InputCallback[1];
			namer[0]=new InputCallback(InputCallback.Type.PROMPT)
			{
				@Override public void showPrompt() { session.println(L("\n\rEnter a new name for your ship: ")); }
				@Override public void timedOut() { }
				@Override public void callBack()
				{
					if((this.input.trim().length()==0)
					||(!CMLib.login().isOkName(this.input.trim(),true)))
					{
						session.println(L("^ZThat is not a permitted name.^N"));
						session.prompt(namer[0].reset());
						return;
					}
					me.renameShip(this.input.trim());
					buyer.tell(L("@x1 is now signed over to @x2.",name(),getOwnerName()));
					final Room finalR=findNearestDocks(R);
					if(finalR==null)
					{
						Log.errOut("Could not dock ship in area "+R.getArea().Name()+" due to lack of water surface.");
						buyer.tell(L("Nowhere was found to dock your ship.  Please contact the administrators!."));
					}
					else
					{
						me.dockHere(finalR);
						buyer.tell(L("You'll find your ship docked at '@x1'.",finalR.displayText(buyer)));
					}
					if ((buyer.playerStats() != null) && (!buyer.playerStats().getExtItems().isContent(me)))
						buyer.playerStats().getExtItems().addItem(me);
				}
			};
			session.prompt(namer[0]);
		}
		else
		{
			buyer.tell(L("@x1 is now signed over to @x2.",name(),getOwnerName()));
			if ((buyer.playerStats() != null) && (!buyer.playerStats().getExtItems().isContent(this)))
				buyer.playerStats().getExtItems().addItem(this);
			final Room finalR=findNearestDocks(R);
			if(finalR==null)
				Log.errOut("Could not dock ship in area "+R.getArea().Name()+" due to lack of water surface.");
			else
				dockHere(finalR);
		}
	}

	private final static String[] MYCODES={"HASLOCK","HASLID","CAPACITY","CONTAINTYPES","RESETTIME","RIDEBASIS","MOBSHELD",
											"AREA","OWNER","PRICE","DEFCLOSED","DEFLOCKED"
										  };
	@Override
	public String getStat(String code)
	{
		if(CMLib.coffeeMaker().getGenItemCodeNum(code)>=0)
			return CMLib.coffeeMaker().getGenItemStat(this,code);
		switch(getCodeNum(code))
		{
		case 0: return ""+hasALock();
		case 1: return ""+hasADoor();
		case 2: return ""+capacity();
		case 3: return ""+containTypes();
		case 4: return ""+openDelayTicks();
		case 5: return ""+rideBasis();
		case 6: return ""+riderCapacity();
		case 7: return (area==null)?"":CMLib.coffeeMaker().getAreaXML(area, null, null, null, true).toString();
		case 8: return getOwnerName();
		case 9: return ""+getPrice();
		case 10: return ""+defaultsClosed();
		case 11: return ""+defaultsLocked();
		default:
			return CMProps.getStatCodeExtensionValue(getStatCodes(), xtraValues, code);
		}
	}
	@Override
	public void setStat(String code, String val)
	{
		if(CMLib.coffeeMaker().getGenItemCodeNum(code)>=0)
			CMLib.coffeeMaker().setGenItemStat(this,code,val);
		else
		switch(getCodeNum(code))
		{
		case 0: setDoorsNLocks(hasADoor(),isOpen(),defaultsClosed(),CMath.s_bool(val),false,CMath.s_bool(val)&&defaultsLocked()); break;
		case 1: setDoorsNLocks(CMath.s_bool(val),isOpen(),CMath.s_bool(val)&&defaultsClosed(),hasALock(),isLocked(),defaultsLocked()); break;
		case 2: setCapacity(CMath.s_parseIntExpression(val)); break;
		case 3: setContainTypes(CMath.s_parseBitLongExpression(Container.CONTAIN_DESCS,val)); break;
		case 4: setOpenDelayTicks(CMath.s_parseIntExpression(val)); break;
		case 5: break;
		case 6: break;
		case 7: setShipArea(val); break;
		case 8: setOwnerName(val); break;
		case 9: setPrice(CMath.s_int(val)); break;
		case 10: setDoorsNLocks(hasADoor(),isOpen(),CMath.s_bool(val),hasALock(),isLocked(),defaultsLocked()); break;
		case 11: setDoorsNLocks(hasADoor(),isOpen(),defaultsClosed(),hasALock(),isLocked(),CMath.s_bool(val)); break;
		default:
			CMProps.setStatCodeExtensionValue(getStatCodes(), xtraValues, code, val);
			break;
		}
	}
	@Override
	protected int getCodeNum(String code)
	{
		for(int i=0;i<MYCODES.length;i++)
			if(code.equalsIgnoreCase(MYCODES[i]))
				return i;
		return -1;
	}
	private static String[] codes=null;
	@Override
	public String[] getStatCodes()
	{
		if(codes!=null)
			return codes;
		final String[] MYCODES=CMProps.getStatCodesList(GenSailingShip.MYCODES,this);
		final String[] superCodes=GenericBuilder.GENITEMCODES;
		codes=new String[superCodes.length+MYCODES.length];
		int i=0;
		for(;i<superCodes.length;i++)
			codes[i]=superCodes[i];
		for(int x=0;x<MYCODES.length;i++,x++)
			codes[i]=MYCODES[x];
		return codes;
	}
	@Override
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof GenSailingShip))
			return false;
		final String[] codes=getStatCodes();
		for(int i=0;i<codes.length;i++)
			if(!E.getStat(codes[i]).equals(getStat(codes[i])))
				return false;
		return true;
	}
}
