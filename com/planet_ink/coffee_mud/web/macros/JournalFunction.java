package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class JournalFunction extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String last=httpReq.getRequestParameter("JOURNAL");
		if(last==null) return "Function not performed -- no Journal specified.";
		Vector info=(Vector)httpReq.getRequestObjects().get("JOURNAL: "+last);
		if(info==null)
		{
			info=ExternalPlay.DBReadJournal(last);
			httpReq.getRequestObjects().put("JOURNAL: "+last,info);
		}
		MOB M=Authenticate.getMOB(Authenticate.getLogin(httpReq));
		if(M==null)	return "Function not allowed -- noone is logged in!";
		if(parms.containsKey("NEWPOST"))
		{
			String from=M.Name();
			String to=httpReq.getRequestParameter("TO");
			if((to==null)||(to.equalsIgnoreCase("all"))) to="ALL";
			if(!to.equals("ALL"))
			{
				if(!ExternalPlay.DBUserSearch(null,to))
					return "Post not submitted -- TO user does not exist.  Try 'All'.";
			}
			String subject=httpReq.getRequestParameter("SUBJECT");
			if(subject.length()==0)
				return "Post not submitted -- No subject!";
			String text=httpReq.getRequestParameter("NEWTEXT");
			if(text.length()==0)
				return "Post not submitted -- No text!";
			ExternalPlay.DBWriteJournal(last,from,to,subject,text,-1);
			httpReq.getRequestObjects().remove("JOURNAL: "+last);
			return "Post submitted.";
		}
		String lastlast=(String)httpReq.getRequestParameter("JOURNALMESSAGE");
		int num=0;
		if(lastlast!=null) num=Util.s_int(lastlast);
		if((num<0)||(num>=info.size()))
			return "Function not performed -- illegal journal message specified.";
		String to= ((String)((Vector)info.elementAt(num)).elementAt(3));
		if(M.isASysOp(null)||(to.equalsIgnoreCase("all"))||(to.equalsIgnoreCase(M.Name())))
		{
			if(parms.containsKey("DELETE"))
			{
				ExternalPlay.DBDeleteJournal(last,num);
				httpReq.addRequestParameters("JOURNALMESSAGE","");
				httpReq.getRequestObjects().remove("JOURNAL: "+last);
				return "Message #"+num+" deleted.";
			}
			else
			if(parms.containsKey("REPLY"))
			{
				String text=httpReq.getRequestParameter("NEWTEXT");
				if(text.length()==0)
					return "Reply not submitted -- No text!";
				ExternalPlay.DBWriteJournal(last,M.Name(),"","",text,num);
				httpReq.getRequestObjects().remove("JOURNAL: "+last);
				return "Reply submitted";
			}
			else
				return "";
		}
		else
			return "You are not allowed to perform this function.";
	}
}
