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

import net.sf.freecol.common.model.Colony;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Player;
import net.sf.freecol.server.FreeColServer;
import net.sf.freecol.server.control.ChangeSet;
import net.sf.freecol.server.model.ServerPlayer;

import org.w3c.dom.Element;


/**
 * The message sent when the client requests abandoning of a colony.
 */
public class AbandonColonyMessage extends AttributeMessage {

    public static final String TAG = "abandonColony";
    private static final String COLONY_TAG = "colony";


    /**
     * Create a new {@code AbandonColonyMessage} with the specified
     * colony.
     *
     * @param colony The {@code Colony} to abandon.
     */
    public AbandonColonyMessage(Colony colony) {
        super(TAG, COLONY_TAG, colony.getId());
    }

    /**
     * Create a new {@code AbandonColonyMessage} from a supplied element.
     *
     * @param game The {@code Game} this message belongs to.
     * @param element The {@code Element} to use to create the message.
     */
    public AbandonColonyMessage(Game game, Element element) {
        super(TAG, COLONY_TAG, getStringAttribute(element, COLONY_TAG));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ChangeSet serverHandler(FreeColServer freeColServer,
                                   ServerPlayer serverPlayer) {
        final String colonyId = getAttribute(COLONY_TAG);

        Colony colony;
        try {
            colony = serverPlayer.getOurFreeColGameObject(colonyId, Colony.class);
        } catch (Exception e) {
            return serverPlayer.clientError(e.getMessage());
        }
        if (colony.getUnitCount() != 0) {
            return serverPlayer.clientError("Attempt to abandon colony "
                + colonyId + " with non-zero unit count "
                + Integer.toString(colony.getUnitCount()));
        }

        // Proceed to abandon
        // FIXME: Player.settlements is still being fixed on the client side.
        return freeColServer.getInGameController()
            .abandonSettlement(serverPlayer, colony);
    }
}
