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

package net.sf.freecol.common.model;

import javax.xml.stream.XMLStreamException;

import net.sf.freecol.common.i18n.Messages;
import net.sf.freecol.common.io.FreeColXMLReader;
import net.sf.freecol.common.io.FreeColXMLWriter;
import net.sf.freecol.common.util.Utils;


/**
 * A trade item consisting of a colony.
 */
public class ColonyTradeItem extends TradeItem {

    public static final String TAG = "colonyTradeItem";

    /** The identifier of the colony to change hands. */
    private String colonyId;

    /**
     * The colony name, which is useful when the colony is unknown to
     * the offer recipient.
     */
    private String colonyName;


    /**
     * Creates a new {@code ColonyTradeItem} instance.
     *
     * @param game The enclosing {@code Game}.
     * @param source The source {@code Player}.
     * @param destination The destination {@code Player}.
     * @param colony The {@code Colony} to trade.
     */
    public ColonyTradeItem(Game game, Player source, Player destination,
                           Colony colony) {
        super(game, Messages.nameKey("model.tradeItem.colony"),
              source, destination);
        colonyId = colony.getId();
        colonyName = colony.getName();
        if (colony.getOwner() != source) {
            throw new IllegalArgumentException("Bad source for colony "
                + colony.getId());
        }
        if (destination == null || !destination.isEuropean()) {
            throw new IllegalArgumentException("Bad destination: "
                + destination);
        }
    }

    /**
     * Creates a new {@code ColonyTradeItem} instance.
     *
     * @param game The enclosing {@code Game}.
     * @param xr The {@code FreeColXMLReader} to read from.
     * @exception XMLStreamException if there is an error reading the stream.
     */
    public ColonyTradeItem(Game game, FreeColXMLReader xr) throws XMLStreamException {
        super(game, xr);
    }


    // Interface TradeItem

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid() {
        return colonyId != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isUnique() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StringTemplate getLabel() {
        return StringTemplate.template(Messages.descriptionKey("model.tradeItem.colony"))
            .addName("%colony%", colonyName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Colony getColony(Game game) {
        return game.getFreeColGameObject(colonyId, Colony.class);
    }

    /**
     * {@inheritDoc}
     */
    public int evaluateFor(Player player) {
        final Colony colony = getColony(player.getGame());
        return (colony == null || !getSource().owns(colony))
            ? Integer.MIN_VALUE
            : colony.evaluateFor(player);
    }
    

    // Serialization

    private static final String COLONY_TAG = "colony";
    private static final String COLONY_NAME_TAG = "colonyName";


    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeAttributes(FreeColXMLWriter xw) throws XMLStreamException {
        super.writeAttributes(xw);

        xw.writeAttribute(COLONY_TAG, colonyId);

        xw.writeAttribute(COLONY_NAME_TAG, colonyName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readAttributes(FreeColXMLReader xr) throws XMLStreamException {
        super.readAttributes(xr);

        colonyId = xr.getAttribute(COLONY_TAG, (String)null);

        colonyName = xr.getAttribute(COLONY_NAME_TAG, (String)null);
    }

    /**
     * {@inheritDoc}
     */
    public String getXMLTagName() { return TAG; }


    // Override Object

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (other instanceof ColonyTradeItem) {
            return Utils.equals(this.colonyId, ((ColonyTradeItem)other).colonyId)
                && Utils.equals(this.colonyName, ((ColonyTradeItem)other).colonyName)
                && super.equals(other);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 37 * hash + Utils.hashCode(this.colonyId);
        return 37 * hash + Utils.hashCode(this.colonyName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(16);
        sb.append('[').append(getId())
            .append(' ').append(colonyName).append(']');
        return sb.toString();
    }
}
