//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.12.19 at 11:50:03 AM PST 
//


package com.pacificbiosciences.pacbiopipelinestats;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PkMidCV complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PkMidCV"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://pacificbiosciences.com/PacBioPipelineStats.xsd}PkMidCVValue"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="Channel" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PkMidCV", propOrder = {
    "pkMidCVValue"
})
public class PkMidCV {

    @XmlElement(name = "PkMidCVValue")
    protected double pkMidCVValue;
    @XmlAttribute(name = "Channel", required = true)
    protected String channel;

    /**
     * Gets the value of the pkMidCVValue property.
     * 
     */
    public double getPkMidCVValue() {
        return pkMidCVValue;
    }

    /**
     * Sets the value of the pkMidCVValue property.
     * 
     */
    public void setPkMidCVValue(double value) {
        this.pkMidCVValue = value;
    }

    /**
     * Gets the value of the channel property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChannel() {
        return channel;
    }

    /**
     * Sets the value of the channel property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChannel(String value) {
        this.channel = value;
    }

}
