package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Chant_FeelHeat extends Chant
{
	public String ID() { return "Chant_FeelHeat"; }
	public String name(){ return "Feel Heat";}
	public String displayText(){return "(Feel Heat)";}
	public int quality(){return Ability.MALICIOUS;}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public Environmental newInstance(){	return new Chant_FeelHeat();}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if((msg.amITarget(mob))&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		   &&(msg.sourceMinor()==CMMsg.TYP_FIRE))
		{
			int recovery=(int)Math.round(Util.mul((msg.value()),2.0));
			msg.setValue(msg.value()+recovery);
		}
		return true;
	}


	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if(tickID!=MudHost.TICK_MOB) return false;
		if((affecting()!=null)&&(affecting() instanceof MOB))
		{
			MOB dummy=(MOB)affecting();
			Room room=dummy.location();
			if(room!=null)
			{
				if((room.getArea().weatherType(room)==Area.WEATHER_HEAT_WAVE)
				&&(Dice.rollPercentage()>dummy.charStats().getSave(CharStats.SAVE_FIRE)))
				{
					int damage=Dice.roll(1,8,0);
					MUDFight.postDamage(invoker,dummy,null,damage,CMMsg.MASK_GENERAL|CMMsg.TYP_FIRE,Weapon.TYPE_BURNING,"The scorching heat <DAMAGE> <T-NAME>!");
				}
				else
				if((room.getArea().weatherType(room)==Area.WEATHER_DUSTSTORM)
				&&(Dice.rollPercentage()>dummy.charStats().getSave(CharStats.SAVE_FIRE)))
				{
					int damage=Dice.roll(1,16,0);
					MUDFight.postDamage(invoker,dummy,null,damage,CMMsg.MASK_GENERAL|CMMsg.TYP_FIRE,Weapon.TYPE_BURNING,"The burning hot dust <DAMAGE> <T-NAME>!");
				}
				else
				if((room.getArea().weatherType(room)==Area.WEATHER_DROUGHT)
				&&(Dice.rollPercentage()>dummy.charStats().getSave(CharStats.SAVE_FIRE)))
				{
					int damage=Dice.roll(1,8,0);
					MUDFight.postDamage(invoker,dummy,null,damage,CMMsg.MASK_GENERAL|CMMsg.TYP_FIRE,Weapon.TYPE_BURNING,"The burning dry heat <DAMAGE> <T-NAME>!");
				}
			}
		}
		return true;
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(canBeUninvoked())
			mob.tell("Your hot feeling is gone.");

		super.unInvoke();

	}

	public void affectCharStats(MOB affectedMOB, CharStats affectedStats)
	{
		super.affectCharStats(affectedMOB,affectedStats);
		affectedStats.setStat(CharStats.SAVE_FIRE,affectedStats.getStat(CharStats.SAVE_FIRE)-100);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) to <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> feel(s) very hot");
					maliciousAffect(mob,target,0,-1);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) to <T-NAMESELF>, but the magic fades.");
		// return whether it worked
		return success;
	}
}
