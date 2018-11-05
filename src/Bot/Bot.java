/*Main Bot Driver class
 *Copyright (C) 2003 Ryan Michela
 *
 *This program is free software; you can redistribute it and/or
 *modify it under the terms of the GNU General Public License
 *as published by the Free Software Foundation; either version 2
 *of the License, or (at your option) any later version.
 *
 *This program is distributed in the hope that it will be useful,
 *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *GNU General Public License for more details.
 *
 *You should have received a copy of the GNU General Public License
 *along with this program; if not, write to the Free Software
 *Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package Bot;

import JavaTOC.*;
import java.util.*;
import java.io.*;

public class Bot implements Chatable, Runnable 
{
  Map people = new HashMap(20, .75f);     //Map of all active users
  JavaTOC TOC = new JavaTOC(this);      //The connection to AIM

  String TOClog = "TOClog.botlog";

  //connection strings
  String userName;
  String password;

  //warning level
  int warningLevel;

  int countDown;

    //get the background process going
    Thread bgProcess = new Thread(this);
  
  public Bot(String name, String pw)
  {
    userName = name;
    password = pw;
    
    //Create the connection
    try
    {
      if(TOC.login(userName, password))
      {
        //initialize the toclog
        Calendar now = Calendar.getInstance();
        String timestamp = now.get(Calendar.MONTH) + "-" + now.get(Calendar.DAY_OF_MONTH) + "-" + now.get(Calendar.YEAR);
        toclog("-------------------------- " + timestamp + " -----------------------------"); 

        //start the TOC dispatch thread
        bgProcess.start();
      }
      else
      {
        System.out.println("Authentication with AIM service failed.");
      }
    }catch(IOException e){System.out.println("Communications error while connecting to host. " + e.toString());}
  }

  public static void main(String[] args)
  {
    if (args.length >= 2)
    {
      //get command line params
      String name = args[0];
      String pw = args[1];
      //create new bot to start system
      Bot bot = new Bot(name, pw);
    }
    else
    {
      System.out.println("Usage: java Bot.Bot username password");
    }
  }

  //unknown TOC event handler
  public void unknown(String str)
  {
    if(str.charAt(3) != '2')
    {
      System.out.println(str);
      StringTokenizer st = new StringTokenizer(str, ":");
      String unknown = st.nextToken();
      if(unknown.equals("UPDATE_BUDDY"))
      {
        st.nextToken();
        st.nextToken();
        warningLevel = Integer.parseInt(st.nextToken());
        if(warningLevel > 80)
        {
          toclog("Warning level too high. Shutting down.");
          System.exit(0);
        }
        
      }
      else if(unknown.equals("EVILED"))
      {
        st.nextToken();
        if(st.hasMoreTokens())
        {
          try{Thread.sleep(3000);} catch(InterruptedException e){}
          synchronized(TOC)
          {
            String name = st.nextToken();
            TOC.send(name, "Screw you!");
            TOC.warn(name, false);
          }
        }
      }
      toclog(str);
    }
  }

  //TOC error handler
  public void error(String str, String var)
  {
    toclog("Error: " + str + " : "  + err(Integer.parseInt(str)));
  }

  //TOC IM handler
  public void im(String from, String message)
  {
    message = dehtmlify(message);
    if(!checkControlStatement(from, message))
    {
      User victim;
      //check to see if user is active. if not activate user
      if (!people.containsKey(despaceify(from)))
      {
        people.put(despaceify(from), new User(from, TOC, this));
        toclog("Initiating conversation: " + from);
      }
      //send the user an IM
      victim = (User)people.get(despaceify(from));
      victim.process(message);
    }
  }

  public void toclog(String str)
  {
      try{
        PrintWriter out = new PrintWriter(new FileWriter(TOClog, true));
        out.println(str);
        out.close();
      }
      catch(IOException e){}
  }

  private String despaceify(String sn)
  {
    String despaced = new String();;
    StringTokenizer st = new StringTokenizer(sn);
    despaced = st.nextToken();
    while(st.hasMoreTokens())
    {
      despaced += "_";
      despaced += st.nextToken();
    }
    return despaced;
  }

  //strip IMs of all html tags
  private String dehtmlify(String input)
  {
    String ret = new String();
    boolean c = true;
    for(int i = 0; i < input.length(); i++)
    {
      if(c)
      {
       if (input.charAt(i) == '<')
       {
         c = false;
       }
       else
       {
         ret += input.charAt(i);
       }
      }
      else
      {
        if(input.charAt(i) == '>')
        {
          c = true;
        }
      }
    }
    return ret;
  }

  //the main thread loop
  public void run()
  {
    try
    {
      TOC.processTOCEvents();
    }
    catch(IOException e)
    {
      System.out.println("Main loop IO exception: " + e.toString());
      e.printStackTrace();
    }
  }

  //handles control statements. retrun true if one was recieved
  public boolean checkControlStatement(String from, String message)
  {
    StringTokenizer st = new StringTokenizer(message);
    if(st.hasMoreTokens())
    {
      String first = st.nextToken();
      if(first.equals("#!kill"))
      {
        synchronized(TOC)
        {
          TOC.logout();
        }
        people.clear();
        System.exit(0);
        return true; 
      }
      else if(first.equals("#!attack"))
      {
        if(st.hasMoreTokens())
        {
          String target = st.nextToken();
          if(!target.equals(userName))
          {
            im(target, "hello");
          }
        }
        return true;
      }
      else if(first.equals("#!send"))
      {
        if(st.hasMoreTokens())
        {
          String text = new String();
          String target = st.nextToken();

          while(st.hasMoreTokens())
          {
            text += st.nextToken();
            text += " ";
          }

          synchronized(TOC)
          {
            TOC.send(target, text);
          }

          if(people.containsKey(despaceify(target)))
          {
            ((User)people.get(despaceify(target))).handleInterjection(text);
          }
          
        }
        return true;
      }
      else if(first.equals("#!list"))
      {
        Set s = people.keySet();
        Iterator it = s.iterator();
        if(! it.hasNext())
        {
          synchronized(TOC)
          {
            TOC.send(from, "List empty.");
          }
          return true;
        }
        String users = new String();
        while (it.hasNext())
        {
          String U = (String)it.next();
          if( ((User)people.get( U )).isVerified() ) users += "*";
          users += U.toString();
          users += "<br>";
        }
        synchronized(TOC)
        {
          TOC.send(from, users);
        }
        return true;
      }
      else if(first.equals("#!bind"))
      {
        if(st.hasMoreTokens())
        {
          String SN = st.nextToken();
          if(people.containsKey(SN))
          {
            ((User)people.get(SN)).bind(from);
            synchronized(TOC)
            {
              TOC.send(from, SN +" ===> " + from);
            }
          }
          else
          {
            synchronized(TOC)
            {
              TOC.send(from, SN + " is not active");
            }
          }
        }
        return true;
      }
      else if(first.equals("#!unbind"))
      {
        if(st.hasMoreTokens())
        {
          String SN = st.nextToken();
          if(people.containsKey(SN))
          {
            if(((User)people.get(SN)).unbind(from))
            {
              synchronized(TOC)
              {
                TOC.send(from, SN +" =X=> " + from);
              }
            }
            else
            {
              synchronized(TOC)
              {
                TOC.send(from, "You were never bound to " + SN);
              }
            }
          }
          else
          {
            synchronized(TOC)
            {
              TOC.send(from, SN + " is not active");
            }
          }
        }
        return true;
      }
      else if(first.equals("#!warn"))
      {
        synchronized(TOC)
        {
          TOC.send(from, "The bot is at " + warningLevel + "%");
        }
        return true;
      }
      else if(first.equals("#!reauthent"))
      {
        if(st.hasMoreTokens())
        {
          String newName = st.nextToken();
          if(st.hasMoreTokens())
          {
            String newPass = st.nextToken();
            if(st.hasMoreTokens())
            {
              String oldPW = st.nextToken();
              if(password.equals(oldPW))
              {
                try
                {
                  synchronized(TOC)
                  {
                    TOC.send(from, "Reauthenticating...");
                    TOC.logout();
                    if(TOC.login(newName, newPass))
                    {
                      TOC.send(from, "Reauthentication successfull!");
                      userName = newName;
                      password = newPass;
                      toclog("REAUTHENT! " + newName + " " + newPass);
                    }
                    else
                    {
                      TOC.login(userName, password); //old account
                      TOC.send(from, "Reauthentication failed!");
                    }
                  }
                }
                catch(IOException e)
                {
                  System.out.println("Reauthentication failed due to network trouble...");
                }
              }
              else
              {
                synchronized(TOC)
                {
                  TOC.send(from, "Incorrect current password!");
                }
              }
              return true;
            }
            else
            {
              return false;
            }
          }
          else
          {
            return false;
          }
        }
        else
        {
          return false;
        }
      }
      else if(first.equals("#!slap"))
      {
        if(st.hasMoreTokens())
        {
          String target = st.nextToken();
          TOC.warn(target, false);
          return true;
        }
        else
        {
          return false;
        }
      }
      else if(first.equals("#!"))
      {
        String out = new String();
        out += "#! = display this message<br>";
        out += "#!kill = kill server<br>";
        out += "#!attack SN = initiate conversation with SN<br>";
        out += "#!send SN message = interject message into conversation with SN<br>";
        out += "#!list = list active conversations<br>";
        out += "#!bind SN = watch conversation with SN<br>";
        out += "#!unbind SN = stop watching conversation with SN<br>";
        out += "#!warn = display the bot's current warning level<br>";
        out += "#!reauthent SN PW OldPW = reauthenticate the bot using SN and PW. OldPW is the password of the current login";
        synchronized(TOC)
        {
          TOC.send(from, out);
        }
        return true;
      }
      else
      {
        return false;
      }
    }
    return false;
  }

  private String err(int num)
  {
    {
       String error;

       switch ( num ) {
       case 901:error="$1 not currently available";break;
       case 902:error="Warning of $1 not currently available";break;
       case 903:error="A message has been dropped, you are exceeding the server speed limit";break;
       case 911:error="Error validating input";break;
       case 912:error="Invalid account";break;
       case 913:error="Error encountered while processing request";break;
       case 914:error="Service unavailable";break;
       case 950:error="Chat in $1 is unavailable.";break;
       case 960:error="You are sending message too fast to $1";break;
       case 961:error="You missed an im from $1 because it was too big.";break;
       case 962:error="You missed an im from $1 because it was sent too fast.";break;
       case 970:error="Failure";break;
       case 971:error="Too many matches";break;
       case 972:error="Need more qualifiers";break;
       case 973:error="Dir service temporarily unavailable";break;
       case 974:error="Email lookup restricted";break;
       case 975:error="Keyword Ignored";break;
       case 976:error="No Keywords";break;
       case 977:error="Language not supported";break;
       case 978:error="Country not supported";break;
       case 979:error="Failure unknown $1";break;
       case 980:error="Incorrect nickname or password.";break;
       case 981:error="The service is temporarily unavailable.";break;
       case 982:error="Your warning level is currently too high to sign on.";break;
       case 983:error="You have been connecting and disconnecting too frequently.\nWait 10 minutes and try again.\n If you continue to try, you will need to wait even longer.";break;
       case 989:error="An unknown signon error has occurred $1";break;
       default:error="Unknown";break;

       }
       return error;
    }
  }
}