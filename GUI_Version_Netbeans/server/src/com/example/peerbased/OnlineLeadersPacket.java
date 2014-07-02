/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.example.peerbased;

import java.io.Serializable;

/**
 *
 * @author vamshi
 */
public class OnlineLeadersPacket implements Serializable {
    
    public static final long serialVersionUID = 537L;
    
    public String[] leaders;
    
    public OnlineLeadersPacket(String[] leaders)
    {
        this.leaders = leaders;
    }
}
