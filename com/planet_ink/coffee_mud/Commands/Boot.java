package com.planet_ink.coffee_mud.Commands.extra;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Boot extends StdCommand
{
	public Boot(){}

	private String[] access={"BOOT"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(!mob.isASysOp(null))
		{
			mob.tell("Only the Archons can do that.");
			return false;
		}
		commands.removeElementAt(0);
		if(mob.session()==null) return false;
		if(commands.size()==0)
		{
			mob.tell("Boot out who?");
			return false;
		}
		String whom=Util.combine(commands,0);
		boolean boot=false;
		for(int s=0;s<Sessions.size();s++)
		{
			Session S=Sessions.elementAt(s);
			if((S.mob()!=null)&&(EnglishParser.containsString(S.mob().name(),whom)))
			{
				if(S==mob.session())
				{
					mob.tell("Try QUIT.");
					return false;
				}
				else
				if((mob.isASysOp(S.mob().location())))
				{
					mob.tell("You boot "+S.mob().name());
					if(S.mob().location()!=null)
						S.mob().location().show(S.mob(),null,CMMsg.MSG_OK_VISUAL,"Something is happening to <S-NAME>.");
					S.setKillFlag(true);
					boot=true;
					break;
				}
			}
		}
		if(!boot)
			mob.tell("You can't find anyone by that name.");
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}
	public boolean arcCommand(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
