/*User Destroyer class. Kills users
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

//user destroyer class kills a user if it has been idle for 15 minutes
class UserDestroyer implements Runnable
{
  User target;
  int minutesLeft;

  public UserDestroyer(User target)
  {
    this.target = target;
    reset();
    Thread t = new Thread(this);
    t.setDaemon(true);
    t.start();
  }

  //reset the clock to 5
  public void reset()
  {
    minutesLeft = 5;
  }

  //main thread loop
  public void run()
  {
    //wait untill 5 idle minutes has elapsed
    try
    {
      while(minutesLeft > 0)
      {
        Thread.sleep(60000); //one min
        minutesLeft--;
      }
      //then deregister the User
      target.parent.people.remove(target.name);
    }catch(InterruptedException e){}
  }
}
