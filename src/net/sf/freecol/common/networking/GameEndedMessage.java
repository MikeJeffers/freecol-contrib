/**
 *  Copyright (C) 2002-2016   The FreeCol Team
 *
 *  This file is part of FreeCol.
 *
 *  FreeCol is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  FreeCol is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with FreeCol.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.sf.freecol.common.networking;

import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Player;
import net.sf.freecol.server.FreeColServer;
import net.sf.freecol.server.model.ServerPlayer;

import org.w3c.dom.Element;


/**
 * The message to signal the end of the game.
 */
public class GameEndedMessage extends AttributeMessage {

    public static final String TAG = "gameEnded";
    private static final String HIGH_SCORE_TAG = "highScore";
    private static final String WINNER_TAG = "winner";


    /**
     * Create a new {@code GameEndedMessage} with the supplied winner.
     *
     * @param winner The {@code Player} that has won.
     * @param highScore True if a new high score was reached.
     */
    public GameEndedMessage(Player winner, boolean highScore) {
        super(TAG, WINNER_TAG, winner.getId(),
              HIGH_SCORE_TAG, String.valueOf(highScore));
    }

    /**
     * Create a new {@code GameEndedMessage} from a supplied element.
     *
     * @param game The {@code Game} this message belongs to.
     * @param element The {@code Element} to use to create the message.
     */
    public GameEndedMessage(Game game, Element element) {
        super(TAG, WINNER_TAG, getStringAttribute(element, WINNER_TAG),
              HIGH_SCORE_TAG, getStringAttribute(element, HIGH_SCORE_TAG));
    }


    // Public interface

    /**
     * Who won?
     *
     * @param game The {@code Game} the winner is in.
     * @return The {@code Player} that won.
     */
    public Player getWinner(Game game) {
        return game.getFreeColGameObject(getAttribute(WINNER_TAG), Player.class);
    }

    /**
     * Get the high score attribute.
     *
     * Note: *not* a boolean, due to a kludge in client.IGIH.
     *
     * @return The score attribute.
     */
    public String getScore() {
        return getAttribute(HIGH_SCORE_TAG);
    }

    // No server handler method required.
    // This message is only sent to clients.
}
