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

import net.sf.freecol.common.model.Ability;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.IndianSettlement;
import net.sf.freecol.common.model.Direction;
import net.sf.freecol.common.model.Player;
import net.sf.freecol.common.model.Tile;
import net.sf.freecol.common.model.Unit;
import net.sf.freecol.common.model.Unit.MoveType;
import net.sf.freecol.server.FreeColServer;
import net.sf.freecol.server.control.ChangeSet;
import net.sf.freecol.server.model.ServerPlayer;

import org.w3c.dom.Element;


/**
 * The message sent when scouting a native settlement.
 */
public class ScoutIndianSettlementMessage extends AttributeMessage {

    public static final String TAG = "scoutIndianSettlement";
    private static final String DIRECTION_TAG = "direction";
    private static final String UNIT_TAG = "unit";


    /**
     * Create a new {@code ScoutIndianSettlementMessage} with the
     * supplied unit and direction.
     *
     * @param unit The {@code Unit} that is learning.
     * @param direction The {@code Direction} the unit is looking.
     */
    public ScoutIndianSettlementMessage(Unit unit, Direction direction) {
        super(TAG, UNIT_TAG, unit.getId(),
              DIRECTION_TAG, String.valueOf(direction));
    }

    /**
     * Create a new {@code ScoutIndianSettlementMessage} from a
     * supplied element.
     *
     * @param game The {@code Game} this message belongs to.
     * @param element The {@code Element} to use to create the message.
     */
    public ScoutIndianSettlementMessage(Game game, Element element) {
        super(TAG, UNIT_TAG, getStringAttribute(element, UNIT_TAG),
              DIRECTION_TAG, getStringAttribute(element, DIRECTION_TAG));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ChangeSet serverHandler(FreeColServer freeColServer,
                                   ServerPlayer serverPlayer) {
        final String unitId = getAttribute(UNIT_TAG);
        final String directionString = getAttribute(DIRECTION_TAG);

        Unit unit;
        try {
            unit = serverPlayer.getOurFreeColGameObject(unitId, Unit.class);
        } catch (Exception e) {
            return serverPlayer.clientError(e.getMessage());
        }
        if (!unit.hasAbility(Ability.SPEAK_WITH_CHIEF)) {
            return serverPlayer.clientError("Unit lacks ability"
                + " to speak to chief: " + unitId);
        }

        Tile tile;
        try {
            tile = unit.getNeighbourTile(directionString);
        } catch (Exception e) {
            return serverPlayer.clientError(e.getMessage());
        }

        IndianSettlement is = tile.getIndianSettlement();
        if (is == null) {
            return serverPlayer.clientError("There is no native settlement at: "
                + tile.getId());
        }

        MoveType type = unit.getMoveType(is.getTile());
        if (type != MoveType.ENTER_INDIAN_SETTLEMENT_WITH_SCOUT) {
            return serverPlayer.clientError("Unable to enter "
                + is.getName() + ": " + type.whyIllegal());
        }

        // Valid request, do the scouting.
        return freeColServer.getInGameController()
            .scoutIndianSettlement(serverPlayer, unit, is);
    }
}
