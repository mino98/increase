//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.3-hudson-jaxb-ri-2.2.3-3- 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.05.08 at 06:16:56 PM CEST 
//


package it.minux.increase.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for panelType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="panelType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="height" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="mintilt" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="maxtilt" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="minazimuth" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="maxazimuth" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="maxdistance" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "panelType", propOrder = {
    "height",
    "mintilt",
    "maxtilt",
    "minazimuth",
    "maxazimuth",
    "maxdistance"
})
public class PanelType {

    protected double height;
    protected double mintilt;
    protected double maxtilt;
    protected double minazimuth;
    protected double maxazimuth;
    protected double maxdistance;

    /**
     * Gets the value of the height property.
     * 
     */
    public double getHeight() {
        return height;
    }

    /**
     * Sets the value of the height property.
     * 
     */
    public void setHeight(double value) {
        this.height = value;
    }

    /**
     * Gets the value of the mintilt property.
     * 
     */
    public double getMintilt() {
        return mintilt;
    }

    /**
     * Sets the value of the mintilt property.
     * 
     */
    public void setMintilt(double value) {
        this.mintilt = value;
    }

    /**
     * Gets the value of the maxtilt property.
     * 
     */
    public double getMaxtilt() {
        return maxtilt;
    }

    /**
     * Sets the value of the maxtilt property.
     * 
     */
    public void setMaxtilt(double value) {
        this.maxtilt = value;
    }

    /**
     * Gets the value of the minazimuth property.
     * 
     */
    public double getMinazimuth() {
        return minazimuth;
    }

    /**
     * Sets the value of the minazimuth property.
     * 
     */
    public void setMinazimuth(double value) {
        this.minazimuth = value;
    }

    /**
     * Gets the value of the maxazimuth property.
     * 
     */
    public double getMaxazimuth() {
        return maxazimuth;
    }

    /**
     * Sets the value of the maxazimuth property.
     * 
     */
    public void setMaxazimuth(double value) {
        this.maxazimuth = value;
    }

    /**
     * Gets the value of the maxdistance property.
     * 
     */
    public double getMaxdistance() {
        return maxdistance;
    }

    /**
     * Sets the value of the maxdistance property.
     * 
     */
    public void setMaxdistance(double value) {
        this.maxdistance = value;
    }

}
