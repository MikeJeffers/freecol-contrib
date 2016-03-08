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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.xml.stream.XMLStreamException;

import net.sf.freecol.common.io.FreeColXMLReader;
import net.sf.freecol.common.io.FreeColXMLWriter;
import net.sf.freecol.common.model.GameOptions;
import static net.sf.freecol.common.util.CollectionUtils.*;


/**
 * A TradeRoute holds all information for a unit to follow along a trade route.
 */
public class TradeRoute extends FreeColGameObject
    implements Nameable, Ownable {

    private static final Logger logger = Logger.getLogger(TradeRoute.class.getName());

    /** The name of this trade route. */
    private String name;

    /**
     * The <code>Player</code> who owns this trade route.  This is
     * necessary to ensure that malicious clients can not modify the
     * trade routes of other players.
     */
    private Player owner;

    /** A list of stops. */
    private final List<TradeRouteStop> stops = new ArrayList<>();

    /** Silence the messaging for this trade route. */
    private boolean silent = false;


    /**
     * Creates a new <code>TradeRoute</code> instance.
     *
     * @param game The enclosing <code>Game</code>.
     * @param name The name of the trade route.
     * @param player The owner <code>Player</code>.
     */
    public TradeRoute(Game game, String name, Player player) {
        super(game);
        this.name = name;
        this.owner = player;
        this.silent = false;
    }

    /**
     * Creates a new <code>TradeRoute</code> instance.
     *
     * @param game The enclosing <code>Game</code>.
     * @param id The object identifier.
     */
    public TradeRoute(Game game, String id) {
        super(game, id);
    }


    /**
     * Copy all fields from another trade route to this one.  This is
     * useful when an updated route is received on the server side
     * from the client.
     *
     * @param other The <code>TradeRoute</code> to copy from.
     */
    public synchronized void updateFrom(TradeRoute other) {
        setName(other.getName());
        setOwner(other.getOwner());
        stops.clear();
        for (TradeRouteStop otherStop : other.getStops()) {
            addStop(new TradeRouteStop(otherStop));
        }
        this.silent = other.silent;
    }

    /**
     * Does this trade route generate no messages to the player?
     *
     * @return True if this trade route is silent.
     */
    public boolean isSilent() {
        return this.silent;
    }

    /**
     * Set the silence status of this trade route.
     *
     * @param silent The new silence status of this trade route.
     */
    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    /**
     * Get the stops in this trade route.
     *
     * @return A list of <code>TradeRouteStop</code>s.
     */
    public final List<TradeRouteStop> getStops() {
        return this.stops;
    }

    /**
     * Get a list of the trade route stops in this trade route, starting
     * at a given stop (inclusive) and a final stop (exclusive).
     *
     * @param start The starting <code>TradeRouteStop</code>.
     * @param end The end <code>TradeRouteStop</code>.
     * @return A list of stops, or null on error.
     */
    public List<TradeRouteStop> getStopSublist(TradeRouteStop start,
                                               TradeRouteStop end) {
        int i0 = getIndex(start), in = getIndex(end);
        if (i0 < 0 || in < 0) return null;
        List<TradeRouteStop> result = new ArrayList<>();
        while (i0 != in) {
            result.add(this.stops.get(i0));
            if (++i0 >= this.stops.size()) i0 = 0;
        }
        return result;
    }

    /**
     * Add a new <code>TradeRouteStop</code> to this trade route.
     *
     * @param stop The <code>TradeRouteStop</code> to add.
     */
    public void addStop(TradeRouteStop stop) {
        if (stop != null) this.stops.add(stop);
    }

    /**
     * Remove a <code>TradeRouteStop</code> from this trade route.
     *
     * @param stop The <code>TradeRouteStop</code> to remove.
     */
    public void removeStop(TradeRouteStop stop) {
        if (stop != null) this.stops.remove(stop);
    }

    /**
     * Get the index of a stop in this trade route.
     *
     * @param stop The <code>TradeRouteStop</code> to look for.
     * @return The index of the given stop, or negative on failure.
     */
    public int getIndex(TradeRouteStop stop) {
        int i = 0;
        for (TradeRouteStop trs : this.stops) {
            if (trs == stop) return i;
            i++;
        }
        return -1;
    }

    /**
     * Clear the stops in this trade route.
     */
    public void clearStops() {
        this.stops.clear();
    }

    /**
     * Get the units assigned to this route.
     *
     * @return A list of assigned <code>Unit</code>s.
     */
    public List<Unit> getAssignedUnits() {
        return transform(owner.getUnits(),
            u -> u.getTradeRoute() == this, Collectors.toList());
    }

    /**
     * Is a stop valid for a given unit?
     *
     * @param unit The <code>Unit</code> to check.
     * @param stop The <code>TradeRouteStop</code> to check.
     * @return True if the stop is valid.
     */
    public static boolean isStopValid(Unit unit, TradeRouteStop stop) {
        return TradeRoute.isStopValid(unit.getOwner(), stop);
    }

    /**
     * Is a stop valid for a given player?
     *
     * @param player The <code>Player</code> to check.
     * @param stop The <code>TradeRouteStop</code> to check.
     * @return True if the stop is valid.
     */
    public static boolean isStopValid(Player player, TradeRouteStop stop) {
        return (stop == null) ? false : stop.isValid(player);
    }

    /**
     * Check that the trade route is valid.
     *
     * @param other Ignore this name from the name collision check.
     * @return Null if the route is valid, or a <code>StringTemplate</code>
     *     explaining the problem if invalid.
     */
    public StringTemplate verify() {
        if (this.name == null) {
            return StringTemplate.template("model.tradeRoute.nullName");
        }
        if (this.owner == null) {
            return StringTemplate.template("model.tradeRoute.nullOwner");
        }

        // Check that the name is unique
        if (owner.getTradeRouteByName(this.name, this) != null) {
            return StringTemplate.template("model.tradeRoute.duplicateName")
                .addName("%name%", this.name);
        }

        // Verify that it has at least two stops
        if (this.stops.size() < 2) {
            return StringTemplate.template("model.tradeRoute.notEnoughStops");
        }

        // Check:
        // - all stops are valid
        // - there is at least one non-empty stop
        // - there is no goods that is present unmaintained at all stops
        Set<GoodsType> always = new HashSet<>(this.stops.get(0).getCargo());
        boolean empty = true;
        for (TradeRouteStop stop : this.stops) {
            if (!TradeRoute.isStopValid(owner, stop)) {
                return stop.invalidStopLabel(owner);
            }
            if (!stop.getCargo().isEmpty()) empty = false;
            always.retainAll(stop.getCargo());
        }
        final boolean enhancedTradeRoutes = getSpecification()
            .getBoolean(GameOptions.ENHANCED_TRADE_ROUTES);
        return (empty)
            ? StringTemplate.template("model.tradeRoute.allEmpty")
            : (!enhancedTradeRoutes && !always.isEmpty())
            ? StringTemplate.template("model.tradeRoute.alwaysPresent")
                .addNamed("%goodsType%", always.iterator().next())
            : null;
    }


    // Interface Nameable

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getName() {
        return this.name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setName(final String newName) {
        this.name = newName;
    }


    // Interface Ownable

    /**
     * {@inheritDoc}
     */
    @Override
    public final Player getOwner() {
        return this.owner;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setOwner(final Player newOwner) {
        this.owner = newOwner;
    }


    // Serialization

    private static final String NAME_TAG = "name";
    private static final String OWNER_TAG = "owner";
    private static final String SILENT_TAG = "silent";


    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeAttributes(FreeColXMLWriter xw) throws XMLStreamException {
        super.writeAttributes(xw);

        xw.writeAttribute(NAME_TAG, getName());

        xw.writeAttribute(OWNER_TAG, getOwner());

        xw.writeAttribute(SILENT_TAG, isSilent());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeChildren(FreeColXMLWriter xw) throws XMLStreamException {
        super.writeChildren(xw);

        for (TradeRouteStop stop : this.stops) stop.toXML(xw);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readAttributes(FreeColXMLReader xr) throws XMLStreamException {
        super.readAttributes(xr);

        this.name = xr.getAttribute(NAME_TAG, (String)null);

        this.owner = xr.findFreeColGameObject(getGame(), OWNER_TAG,
                                              Player.class, (Player)null, true);

        this.silent = xr.getAttribute(SILENT_TAG, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readChildren(FreeColXMLReader xr) throws XMLStreamException {
        // Clear containers.
        clearStops();

        super.readChildren(xr);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readChild(FreeColXMLReader xr) throws XMLStreamException {
        final String tag = xr.getLocalName();

        if (TradeRouteStop.getTagName().equals(tag)) {
            addStop(new TradeRouteStop(getGame(), xr));
            
        } else {
            super.readChild(xr);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(64);
        sb.append("[").append(getXMLTagName())
            .append(" \"").append(this.name).append("\"");
        if (this.owner != null) sb.append(" owner=").append(this.owner.getId());
        sb.append(" silent=").append(Boolean.toString(this.silent));
        for (TradeRouteStop stop : getStops()) sb.append(" ").append(stop);
        sb.append("]");
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getXMLTagName() { return getTagName(); }

    /**
     * Gets the tag name of the root element representing this object.
     *
     * @return "tradeRoute".
     */
    public static String getTagName() {
        return "tradeRoute";
    }
}
