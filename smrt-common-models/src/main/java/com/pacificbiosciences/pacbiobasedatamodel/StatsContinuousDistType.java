//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: XXX
//


package com.pacificbiosciences.pacbiobasedatamodel;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Continuous distribution class
 * 
 * <p>Java class for StatsContinuousDistType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="StatsContinuousDistType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://pacificbiosciences.com/PacBioBaseDataModel.xsd}BaseEntityType">
 *       &lt;sequence>
 *         &lt;element name="SampleSize" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="SampleMean" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *         &lt;element name="SampleMed" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *         &lt;element name="SampleStd" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *         &lt;element name="Sample95thPct" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *         &lt;element name="NumBins" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="BinCounts">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="BinCount" type="{http://www.w3.org/2001/XMLSchema}int" maxOccurs="unbounded"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="BinWidth" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *         &lt;element name="MinOutlierValue" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
 *         &lt;element name="MinBinValue" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
 *         &lt;element name="MaxBinValue" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
 *         &lt;element name="MaxOutlierValue" type="{http://www.w3.org/2001/XMLSchema}float" minOccurs="0"/>
 *         &lt;element name="MetricDescription" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *       &lt;attribute name="Channel" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StatsContinuousDistType", propOrder = {
    "sampleSize",
    "sampleMean",
    "sampleMed",
    "sampleStd",
    "sample95ThPct",
    "numBins",
    "binCounts",
    "binWidth",
    "minOutlierValue",
    "minBinValue",
    "maxBinValue",
    "maxOutlierValue",
    "metricDescription"
})
public class StatsContinuousDistType
    extends BaseEntityType
{

    @XmlElement(name = "SampleSize")
    protected int sampleSize;
    @XmlElement(name = "SampleMean")
    protected float sampleMean;
    @XmlElement(name = "SampleMed")
    protected float sampleMed;
    @XmlElement(name = "SampleStd")
    protected float sampleStd;
    @XmlElement(name = "Sample95thPct")
    protected float sample95ThPct;
    @XmlElement(name = "NumBins")
    protected int numBins;
    @XmlElement(name = "BinCounts", required = true)
    protected StatsContinuousDistType.BinCounts binCounts;
    @XmlElement(name = "BinWidth")
    protected float binWidth;
    @XmlElement(name = "MinOutlierValue")
    protected Float minOutlierValue;
    @XmlElement(name = "MinBinValue")
    protected Float minBinValue;
    @XmlElement(name = "MaxBinValue")
    protected Float maxBinValue;
    @XmlElement(name = "MaxOutlierValue")
    protected Float maxOutlierValue;
    @XmlElement(name = "MetricDescription", required = true)
    protected String metricDescription;
    @XmlAttribute(name = "Channel")
    protected String channel;

    /**
     * Gets the value of the sampleSize property.
     * 
     */
    public int getSampleSize() {
        return sampleSize;
    }

    /**
     * Sets the value of the sampleSize property.
     * 
     */
    public void setSampleSize(int value) {
        this.sampleSize = value;
    }

    /**
     * Gets the value of the sampleMean property.
     * 
     */
    public float getSampleMean() {
        return sampleMean;
    }

    /**
     * Sets the value of the sampleMean property.
     * 
     */
    public void setSampleMean(float value) {
        this.sampleMean = value;
    }

    /**
     * Gets the value of the sampleMed property.
     * 
     */
    public float getSampleMed() {
        return sampleMed;
    }

    /**
     * Sets the value of the sampleMed property.
     * 
     */
    public void setSampleMed(float value) {
        this.sampleMed = value;
    }

    /**
     * Gets the value of the sampleStd property.
     * 
     */
    public float getSampleStd() {
        return sampleStd;
    }

    /**
     * Sets the value of the sampleStd property.
     * 
     */
    public void setSampleStd(float value) {
        this.sampleStd = value;
    }

    /**
     * Gets the value of the sample95ThPct property.
     * 
     */
    public float getSample95ThPct() {
        return sample95ThPct;
    }

    /**
     * Sets the value of the sample95ThPct property.
     * 
     */
    public void setSample95ThPct(float value) {
        this.sample95ThPct = value;
    }

    /**
     * Gets the value of the numBins property.
     * 
     */
    public int getNumBins() {
        return numBins;
    }

    /**
     * Sets the value of the numBins property.
     * 
     */
    public void setNumBins(int value) {
        this.numBins = value;
    }

    /**
     * Gets the value of the binCounts property.
     * 
     * @return
     *     possible object is
     *     {@link StatsContinuousDistType.BinCounts }
     *     
     */
    public StatsContinuousDistType.BinCounts getBinCounts() {
        return binCounts;
    }

    /**
     * Sets the value of the binCounts property.
     * 
     * @param value
     *     allowed object is
     *     {@link StatsContinuousDistType.BinCounts }
     *     
     */
    public void setBinCounts(StatsContinuousDistType.BinCounts value) {
        this.binCounts = value;
    }

    /**
     * Gets the value of the binWidth property.
     * 
     */
    public float getBinWidth() {
        return binWidth;
    }

    /**
     * Sets the value of the binWidth property.
     * 
     */
    public void setBinWidth(float value) {
        this.binWidth = value;
    }

    /**
     * Gets the value of the minOutlierValue property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getMinOutlierValue() {
        return minOutlierValue;
    }

    /**
     * Sets the value of the minOutlierValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setMinOutlierValue(Float value) {
        this.minOutlierValue = value;
    }

    /**
     * Gets the value of the minBinValue property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getMinBinValue() {
        return minBinValue;
    }

    /**
     * Sets the value of the minBinValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setMinBinValue(Float value) {
        this.minBinValue = value;
    }

    /**
     * Gets the value of the maxBinValue property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getMaxBinValue() {
        return maxBinValue;
    }

    /**
     * Sets the value of the maxBinValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setMaxBinValue(Float value) {
        this.maxBinValue = value;
    }

    /**
     * Gets the value of the maxOutlierValue property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getMaxOutlierValue() {
        return maxOutlierValue;
    }

    /**
     * Sets the value of the maxOutlierValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setMaxOutlierValue(Float value) {
        this.maxOutlierValue = value;
    }

    /**
     * Gets the value of the metricDescription property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMetricDescription() {
        return metricDescription;
    }

    /**
     * Sets the value of the metricDescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMetricDescription(String value) {
        this.metricDescription = value;
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


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="BinCount" type="{http://www.w3.org/2001/XMLSchema}int" maxOccurs="unbounded"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "binCount"
    })
    public static class BinCounts {

        @XmlElement(name = "BinCount", type = Integer.class)
        protected List<Integer> binCount;

        /**
         * Gets the value of the binCount property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the binCount property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getBinCount().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Integer }
         * 
         * 
         */
        public List<Integer> getBinCount() {
            if (binCount == null) {
                binCount = new ArrayList<Integer>();
            }
            return this.binCount;
        }

    }

}
