package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class AntiVagrant extends ActiveTicker
{
	public String ID(){return "AntiVagrant";}
	private int speakDown=3;
	private MOB target=null;
	private boolean kickout=false;
	private boolean anywhere=false;

	public AntiVagrant()
	{
		super();
		minTicks=2; maxTicks=3; chance=99;
		tickReset();
	}
	public Behavior newInstance()
	{
		return new AntiVagrant();
	}

	public void setParms(String parms)
	{
		kickout=parms.toUpperCase().indexOf("KICK")>=0;
		anywhere=parms.toUpperCase().indexOf("ANYWHERE")>=0;
		super.setParms(parms);
	}

	public void wakeVagrants(MOB observer)
	{
		if(!canFreelyBehaveNormal(observer)) return;
		if(anywhere||(observer.location().domainType()==Room.DOMAIN_OUTDOORS_CITY))
		{
			if(target!=null)
			if(Sense.isSleeping(target)&&(target!=observer))
			{
				CommonMsgs.say(observer,target,"Damn lazy good for nothing!",false,false);
				FullMsg msg=new FullMsg(observer,target,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> shake(s) <T-NAME> awake.");
				if(observer.location().okMessage(observer,msg))
				{
					observer.location().send(observer,msg);
					target.tell(observer.name()+" shakes you awake.");
					CommonMsgs.stand(target,true);
					if((kickout)&&(!Sense.isSitting(target))&&(!Sense.isSleeping(target)))
						MUDTracker.beMobile(target,true,false,false,false,null);
				}
			}
			else
			if((Sense.isSitting(target)&&(target!=observer)))
			{
				CommonMsgs.say(observer,target,"Get up and move along!",false,false);
				FullMsg msg=new FullMsg(observer,target,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> stand(s) <T-NAME> up.");
				if(observer.location().okMessage(observer,msg))
				{
					observer.location().send(observer,msg);
					CommonMsgs.stand(target,true);
					if((kickout)&&(!Sense.isSitting(target))&&(!Sense.isSleeping(target)))
						MUDTracker.beMobile(target,true,false,false,false,null);
				}
			}
			target=null;
			for(int i=0;i<observer.location().numInhabitants();i++)
			{
				MOB mob=observer.location().fetchInhabitant(i);
				if((mob!=null)
				&&(mob!=observer)
				&&((Sense.isSitting(mob))||(Sense.isSleeping(mob))))
				{
				   target=mob;
				   break;
				}
			}
		}
	}


	public void executeMsg(Environmental affecting, CMMsg msg)
	{
		// believe it or not, this is for arrest behavior.
		super.executeMsg(affecting,msg);
		if((msg.sourceMinor()==CMMsg.TYP_SPEAK)
		&&(msg.sourceMessage()!=null)
		&&(msg.sourceMessage().toUpperCase().indexOf("SIT")>=0))
			speakDown=3;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);

		if(tickID!=MudHost.TICK_MOB) return true;

		// believe it or not, this is for arrest behavior.
		if(speakDown>0)	{	speakDown--;return true;	}

		if((canFreelyBehaveNormal(ticking))&&(canAct(ticking,tickID)))
		{
			MOB mob=(MOB)ticking;
			wakeVagrants(mob);
			return true;
		}
		return true;
	}
}