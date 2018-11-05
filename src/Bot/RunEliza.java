/*Eliza test program for local script testing
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
import Eliza.*;
import java.io.*;

public class RunEliza 
{
  ElizaMain theBot = new ElizaMain();
  public RunEliza()
  {
    theBot.readScript(true, "script");
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    String in = new String(), out;
    try{
      while(!in.equals(".bye"))
      {
        System.out.print(">>> ");
        in = br.readLine();
        out = theBot.processInput(in);
        System.out.println("<<< " + out);

        if(in.equals(".reload"))
        {
          theBot = new ElizaMain();
          theBot.readScript(true, "script");
          System.out.println("Bot reloaded...");
        }
      }
    }catch(IOException e){}
  }

  public static void main(String[] args)
  {
    RunEliza runEliza = new RunEliza();
  }
}