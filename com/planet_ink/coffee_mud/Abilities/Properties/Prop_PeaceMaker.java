package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_PeaceMaker extends Property
{
	public String ID() { return "Prop_PeaceMaker"; }
	public String name(){ return "Strike Neuralizing";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_AREAS|Ability.CAN_MOBS;}
	public Environmental newInstance(){	return new Prop_PeaceMaker();}
	public String accountForYourself()
	{ return "Peace Maker";	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((Util.bset(msg.sourceCode(),CMMsg.MASK_MALICIOUS))
		||(Util.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
		||(Util.bset(msg.othersCode(),CMMsg.MASK_MALICIOUS)))
		{
			if((msg.source()!=null)
			&&(msg.target()!=null)
			&&(msg.source()!=affected)
			&&(msg.source()!=msg.target()))
			{
				if(affected instanceof MOB)
				{
					MOB mob=(MOB)affected;
					if((Sense.aliveAwakeMobile(mob,true))
					&&(!mob.isInCombat()))
					{
						String t="No fighting!";
						if(text().length()>0)
						{
							Vector V=Util.parseSemicolons(text(),true);
							t=(String)V.elementAt(Dice.roll(1,V.size(),-1));
						}
						CommonMsgs.say(mob,msg.source(),t,false,false);
					}
					else
						return super.okMessage(myHost,msg);
				}
				else
				{
					String t="You feel too peaceful here.";
					if(text().length()>0)
					{
						Vector V=Util.parseSemicolons(text(),true);
						t=(String)V.elementAt(Dice.roll(1,V.size(),-1));
					}
					msg.source().tell(t);
				}
				if(msg.source().getVictim()!=null)
					msg.source().getVictim().makePeace();
				msg.source().makePeace();
				msg.modify(msg.source(),msg.target(),msg.tool(),CMMsg.NO_EFFECT,"",CMMsg.NO_EFFECT,"",CMMsg.NO_EFFECT,"");
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}
}
