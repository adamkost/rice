package org.kuali.rice.core.api.uif.control.widget;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = Datepicker.Constants.TYPE_NAME)
public class Datepicker extends AbstractWidget {

    /**
     * Defines some internal constants used on this class.
     */
    static final class Constants {
        static final String TYPE_NAME = "DatepickerType";
    }
}
