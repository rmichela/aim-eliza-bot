/*User class. Represents users.
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
import java.io.*;
import java.util.*;

//the user class contains all the stuff for each user
class User implements Runnable
{
  String name;
  String logname;
  String input;
  Bot parent;
  Mutater M = new ElizaMutater();
  FileWriter log;
  JavaTOC outPort;
  boolean engaged = false;
  boolean verified = false;
  Vector observer = new Vector();

  //create the user destroyer to kill this object if it becomes inactive
  UserDestroyer destroyer = new UserDestroyer(this);

  public User(String name, JavaTOC outPort, Bot parent)
  {
    this.name = name;
    this.outPort = outPort;
    this.parent = parent;

    //start the log based on the time and date
    Calendar now = Calendar.getInstance();
    logname = now.get(Calendar.MONTH) + "-" + now.get(Calendar.DAY_OF_MONTH) + "-" + now.get(Calendar.YEAR) + " " + name + ".botlog";
    try
    {
      PrintWriter out = new PrintWriter(new FileWriter(logname, true));
      out.println("--------------" + now.get(Calendar.HOUR_OF_DAY) + ":" + now.get(Calendar.MINUTE) + "--------------");
      out.close();
    }catch(IOException e){}
  }

  //take in a string and send back an im
  public void process(String input)
  {
    if (!input.equals("hello")) verified = true;
    if(!engaged)
    {
      this.input = input;
      Thread t = new Thread(this);
      t.start();
      engaged = true;
    }
  }

  //the processing thread. so that im's dont interfere with each other's delay
  public void run()
  {
    try
    {
      destroyer.reset();
    
      PrintWriter out = new PrintWriter(new FileWriter(logname, true));

      String output = M.mutate(input);

      System.out.println(name + " >>> " + input);
      out.println(name + " >>> " + input);

      Random r = new Random();
      int delay = 2;
      delay += (r.nextInt(5));

      Thread.sleep(1000*delay);
    
      synchronized(outPort)
      {
        outPort.send(name, output);
      }
      System.out.println(name + " <<< " + output);
      out.println(name + " <<< " + output);
      Thread.sleep(1010);

      handleObservers(name + " (rec) : " + input + "<br>" + name + " (snd) : " + output); // thats a <<< in html
      
      out.close();
    }
    catch(InterruptedException e){}
    catch(IOException e)
    {
      System.out.println("Error in transmit thread! " + e.toString());
    }
    finally
    {
      engaged = false;
    }
  }

  public void bind(String user)
  {
   observer.add(user);
  }

  public boolean unbind(String user)
  {
    if(observer.remove(user))
    {
      return true;
    }
    else
    {
      return false;
    }
  }

  private void handleObservers(String message)
  {
    for(int i = 0; i < observer.size(); i++)
    {
      try{Thread.sleep(10);}catch(InterruptedException e){}
      synchronized(outPort)
      {
        outPort.send((String)(observer.get(i)), message);
      }
    }
  }

  public void handleInterjection(String interj)
  {
    handleObservers(name + " (interjection) : " + interj);
    try
    {
      PrintWriter out = new PrintWriter(new FileWriter(logname, true));
      out.println("Interjection >> " + interj);
      out.close();
    }
    catch(IOException e){}
  }

  public boolean isVerified()
  {
    return verified;
  }
}