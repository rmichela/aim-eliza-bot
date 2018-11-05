/*Muteter implementation for Eliza
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

//the mutator class mutates the input string to produce the output
class ElizaMutater implements Mutater
{
  ElizaMain theBot = new ElizaMain();
  String script = "script";

  public ElizaMutater()
  {
    theBot.readScript(true, script);
  }

  //the mutate function does the mutating
  public String mutate(String input)
  {
    return theBot.processInput(input);
  }
}