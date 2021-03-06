/*
 * VCVSCard.java
 * 
 * Created on Oct 14, 2007, 2:00:27 PM
 * 
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.pmedv.blackboard.spice.sim.spice.parse.source.vsrc;

import org.pmedv.blackboard.spice.sim.spice.model.sources.vsrcs.VCVSInstance;
import org.pmedv.blackboard.spice.sim.spice.parse.Deck;
import org.pmedv.blackboard.spice.sim.spice.parse.ParserException;
import org.pmedv.blackboard.spice.sim.spice.parse.source.SourceCard;

/**
 *
 * @author Kristopher T. Beck
 */
public class VCVSCard extends SourceCard {

    private String contPosName;
    private String contNegName;
    private String coeff;

    public VCVSCard(String cardString) throws ParserException {
        super(cardString);
        nodeCount = 4;
        prefix = "E";
    }

    public String getCoeff() {
        return coeff;
    }

    public void setCoeff(String coeff) {
        this.coeff = coeff;
    }

    public String getContNegName() {
        return contNegName;
    }

    public void setContNegName(String contNegName) {
        this.contNegName = contNegName;
    }

    public String getContPosName() {
        return contPosName;
    }

    public void setContPosName(String contPosName) {
        this.contPosName = contPosName;
    }

    public VCVSInstance createInstance(Deck deck) {
        VCVSInstance instance = new VCVSInstance();
        instance.setInstName(instName);
        instance.setPosIndex(deck.getNodeIndex(posNodeName));
        instance.setNegIndex(deck.getNodeIndex(negNodeName));
        instance.setContPosIndex(deck.getNodeIndex(contPosName));
        instance.setContNegIndex(deck.getNodeIndex(contNegName));
        instance.setCoeff(parseDouble(coeff));
        return instance;
    }

    @Override
    public void parse(String cardString) throws ParserException {
        String[] elements = cardString.replaceAll(",|=", " ").split(" ");
        instName = elements[0];
        posNodeName = elements[1];
        negNodeName = elements[2];
        contPosName = elements[3];
        contNegName = elements[4];
        coeff = elements[5];
    }
}
