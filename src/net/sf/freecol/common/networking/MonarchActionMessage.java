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
import net.sf.freecol.common.model.Monarch.MonarchAction;
import net.sf.freecol.common.model.Player;
import net.sf.freecol.common.model.StringTemplate;
import net.sf.freecol.server.FreeColServer;
import net.sf.freecol.server.control.ChangeSet;
import net.sf.freecol.server.model.ServerPlayer;

import org.w3c.dom.Element;


/**
 * The message sent when doing a monarch action.
 */
public class MonarchActionMessage extends DOMMessage {

    public static final String TAG = "monarchAction";
    private static final String ACTION_TAG = "action";
    private static final String MONARCH_TAG = "monarch";
    private static final String RESULT_TAG = "result";
    private static final String TAX_TAG = "tax";

    /** The monarch action. */
    private final MonarchAction action;

    /** A template describing the action. */
    private final StringTemplate template;

    /** The monarch image key. */
    private final String monarchKey;

    /** The tax rate, if appropriate. */
    private String tax;

    /** Is the offer accepted?  Valid in replies from client. */
    private String resultString;


    /**
     * Create a new {@code MonarchActionMessage} with the given action
     * to be sent to the client to solicit a response.
     *
     * @param action The {@code MonarchAction} to do.
     * @param template A {@code StringTemplate} describing the action.
     * @param monarchKey The resource key for the monarch image.
     */
    public MonarchActionMessage(MonarchAction action,
                                StringTemplate template, String monarchKey) {
        super(TAG);

        this.action = action;
        this.template = template;
        this.monarchKey = monarchKey;
        this.tax = null;
        this.resultString = null;
    }

    /**
     * Create a new {@code MonarchActionMessage} from a
     * supplied element.
     *
     * @param game The {@code Game} this message belongs to.
     * @param element The {@code Element} to use to create the message.
     */
    public MonarchActionMessage(Game game, Element element) {
        super(TAG);

        this.action = getEnumAttribute(element, ACTION_TAG,
            MonarchAction.class, (MonarchAction)null);
        this.monarchKey = getStringAttribute(element, MONARCH_TAG);
        this.tax = getStringAttribute(element, TAX_TAG);
        this.resultString = getStringAttribute(element, RESULT_TAG);
        this.template = getChild(game, element, 0, StringTemplate.class);
    }


    // Public interface

    /**
     * Gets the monarch action type of this message.
     *
     * @return The monarch action type.
     */
    public MonarchAction getAction() {
        return action;
    }

    /**
     * Gets the template of this message.
     *
     * @return The template.
     */
    public StringTemplate getTemplate() {
        return template;
    }

    /**
     * Gets the monarch key.
     *
     * @return The monarch key.
     */
    public String getMonarchKey() {
        return this.monarchKey;
    }

    /**
     * Gets the tax amount attached to this message.
     *
     * @return The tax amount, or negative if none present.
     */
    public int getTax() {
        try {
            return Integer.parseInt(tax);
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Sets the tax amount attached to this message.
     *
     * @param tax The tax amount.
     * @return This message.
     */
    public MonarchActionMessage setTax(int tax) {
        this.tax = Integer.toString(tax);
        return this;
    }

    /**
     * Gets the result.
     *
     * @return The result.
     */
    public boolean getResult() {
        return Boolean.parseBoolean(resultString);
    }

    /**
     * Sets the result.
     *
     * @param accept The new result.
     * @return This message.
     */
    public MonarchActionMessage setResult(boolean accept) {
        this.resultString = Boolean.toString(accept);
        return this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ChangeSet serverHandler(FreeColServer freeColServer,
                                   ServerPlayer serverPlayer) {
        return freeColServer.getInGameController()
            .monarchAction(serverPlayer, getAction(), getResult());
    }

    /**
     * Convert this MonarchMessage to XML.
     *
     * @return The XML representation of this message.
     */
    @Override
    public Element toXMLElement() {
        return new DOMMessage(TAG,
            ACTION_TAG, this.action.toString(),
            MONARCH_TAG, this.monarchKey,
            TAX_TAG, this.tax,
            RESULT_TAG, this.resultString)
            .add(this.template).toXMLElement();
    }
}
