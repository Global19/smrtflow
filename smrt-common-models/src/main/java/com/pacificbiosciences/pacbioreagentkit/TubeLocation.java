//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.06.09 at 04:52:36 PM PDT 
//


package com.pacificbiosciences.pacbioreagentkit;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for TubeLocation.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="TubeLocation">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="ReagentTube0"/>
 *     &lt;enumeration value="ReagentTube1"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "TubeLocation")
@XmlEnum
public enum TubeLocation {

    @XmlEnumValue("ReagentTube0")
    REAGENT_TUBE_0("ReagentTube0"),
    @XmlEnumValue("ReagentTube1")
    REAGENT_TUBE_1("ReagentTube1");
    private final String value;

    TubeLocation(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static TubeLocation fromValue(String v) {
        for (TubeLocation c: TubeLocation.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
