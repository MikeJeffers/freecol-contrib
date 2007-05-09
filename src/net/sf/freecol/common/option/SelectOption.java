
package net.sf.freecol.common.option;

import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import net.sf.freecol.client.gui.i18n.Messages;


/**
* Represents an option where the valid choice is an integer.
*/
public class SelectOption extends AbstractOption {
    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(SelectOption.class.getName());

    public static final String  COPYRIGHT = "Copyright (C) 2003-2006 The FreeCol Team";
    public static final String  LICENSE = "http://www.gnu.org/licenses/gpl.html";
    public static final String  REVISION = "$Revision$";


    protected int value;
    protected String[] options;

    
    /**
     * Creates a new <code>SelectOption</code>.
     *
     * @param id The identifier for this option. This is used when the object should be
     *           found in an {@link OptionGroup}.
     * @param name The name of the <code>Option</code>. This text is used for identifying
     *           the option for a user. Example: The text related to a checkbox.
     * @param shortDescription Should give a short description of the <code>SelectOption</code>.
     *           This might be used as a tooltip text.
     * @param options All possible values.
     * @param defaultOption The index of the default value.
     */
    public SelectOption(String id, String name, String shortDescription, String[] options, int defaultOption) {
        this(id, name, shortDescription, options, defaultOption, false);
    }
    
    /**
     * Creates a new <code>SelectOption</code>.
     *
     * @param id The identifier for this option. This is used when the object should be
     *           found in an {@link OptionGroup}.
     * @param name The name of the <code>Option</code>. This text is used for identifying
     *           the option for a user. Example: The text related to a checkbox.
     * @param shortDescription Should give a short description of the <code>SelectOption</code>.
     *           This might be used as a tooltip text.
     * @param options All possible values.
     * @param defaultOption The index of the default value.
     */
    protected SelectOption(String id, String name, String shortDescription, String[] options, int defaultOption, boolean doNotLocalize) {
        super(id, name, shortDescription);

        if (doNotLocalize) {
            this.options = options;
        } else {
            String[] localized = new String[options.length];
            for (int i = 0; i < options.length; i++) {
                localized[i] = Messages.message(options[i]);
            }        
            this.options = localized;
        }
        
        this.value = defaultOption;
    }

    /**
     * Gets the current value of this <code>SelectOption</code>.
     * @return The value.
     */
    public int getValue() {
        return value;
    }
 
    /**
     * Sets the value of this <code>SelectOption</code>.
     * @param value The value to be set.
     */
    public void setValue(int value) {
        final int oldValue = this.value;
        this.value = value;
        
        if (value != oldValue) {
            firePropertyChange("value", Integer.valueOf(oldValue), Integer.valueOf(value));
        }
    }

    /**
     * Gets the current options of this <code>SelectOption</code>.
     * @return The options.
     */
    public String[] getOptions() {
        return options;
    }

    
    /**
     * Sets the options of this <code>SelectOption</code>.
     * @param options The options to be set.
     */
    protected void setOptions(String[] options) {
        this.options = options;
    }

    /**
     * This method can be overwritten by subclasses to allow
     * a custom save value.
     * 
     * @return The String value of the Integer.
     * @see setValue(String)
     */
    protected String getStringValue() {
        return Integer.toString(value);
    }
    
    /**
     * This method can be overwritten by subclasses to allow
     * a custom save value.
     * 
     * @return The String value of the Integer.
     * @see getSaveValue()
     */
    protected void setValue(String value) {
        setValue(Integer.parseInt(value));
    }
    
    /**
     * This method writes an XML-representation of this object to
     * the given stream.
     *  
     * @param out The target stream.
     * @throws XMLStreamException if there are any problems writing
     *      to the stream.
     */
    protected void toXMLImpl(XMLStreamWriter out) throws XMLStreamException {
        // Start element:
        out.writeStartElement(getId());

        out.writeAttribute("value", getStringValue());

        out.writeEndElement();
    }
    
    /**
     * Initialize this object from an XML-representation of this object.
     * @param in The input stream with the XML.
     * @throws XMLStreamException if a problem was encountered
     *      during parsing.
     */
    protected void readFromXMLImpl(XMLStreamReader in) throws XMLStreamException {
        final int oldValue = this.value;
        
        setValue(in.getAttributeValue(null, "value"));
        in.nextTag();
        
        if (value != oldValue) {
            firePropertyChange("value", Integer.valueOf(oldValue), Integer.valueOf(value));
        }
    }


    /**
    * Gets the tag name of the root element representing this object.
    * @return "integerOption".
    */
    public static String getXMLElementTagName() {
        return "selectOption";
    }

}
