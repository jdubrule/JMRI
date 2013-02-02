// TransitManager.java

package jmri;

import org.apache.log4j.Logger;
import jmri.managers.AbstractManager;

import java.util.ArrayList;
import java.util.List;
import java.text.DecimalFormat;

/**
 * Implementation of a Transit Manager
 * <P>
 * This doesn't need an interface, since Transits are 
 * globaly implemented, instead of being system-specific.
 * <P>
 * Note that Transit system names must begin with IZ, and be followed by a 
 * string, usually, but not always, a number. All alphabetic characters
 * in a Transit system name must be upper case. This is enforced when a Transit
 * is created.
 * <P>
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <P>
 * @author      Dave Duchamp Copyright (C) 2008, 2011
 * @version	$Revision$
 */
public class TransitManager extends AbstractManager
    implements java.beans.PropertyChangeListener {

    public TransitManager() {
        super();
    }

    public int getXMLOrder(){
        return Manager.TRANSITS;
    }
    
    public String getSystemPrefix() { return "I"; }
    public char typeLetter() { return 'Z'; }
    
    /**
     * Method to create a new Transit if the Transit does not exist
     *   Returns null if a Transit with the same systemName or userName
     *       already exists, or if there is trouble creating a new Transit.
     */
    public Transit createNewTransit(String systemName, String userName) {
		// check system name
		if ( (systemName==null) || (systemName.length()<1) ) {
			// no valid system name entered, return without creating
			return null;
		}
		String sysName = systemName;
		if ( (sysName.length()<2) || (!sysName.substring(0,2).equals("IZ")) ) {
			sysName = "IZ"+sysName;
		}
        // Check that Transit does not already exist
        Transit z;
        if (userName!= null && !userName.equals("")) {
            z = getByUserName(userName);
            if (z!=null) return null;
        }
		String sName = sysName.toUpperCase().trim();
        z = getBySystemName(sysName);
		if (z==null) z = getBySystemName(sName);
        if (z!=null) return null;
        // Transit does not exist, create a new Transit
        z = new Transit(sName,userName);
        // save in the maps
        register(z);
        return z;
    }
    /**
     * For use with User GUI, to allow the auto generation of systemNames,
     * where the user can optionally supply a username.
     * Note: Since system names should be kept short for use in Dispatcher, the :AUTO:000 has been removed
     *    from automatically generated system names.
     *    Autogenerated system names will use IZnn, where nn is the first available number.
     */
    public Transit createNewTransit(String userName) {
    	boolean found =false;
    	String testName = "";
    	Transit z;
    	while (!found) {
    		int nextAutoTransitRef = lastAutoTransitRef+1;
    		testName = "IZ"+nextAutoTransitRef;
    		z = getBySystemName(testName);
    		if (z==null) found = true;        
    		lastAutoTransitRef = nextAutoTransitRef;
    	}
        return createNewTransit(testName, userName);
    }
    
    DecimalFormat paddedNumber = new DecimalFormat("0000");

    int lastAutoTransitRef = 0;

    /** 
     * Method to get an existing Transit.  First looks up assuming that
     *      name is a User Name.  If this fails looks up assuming
     *      that name is a System Name.  If both fail, returns null.
     *
     * @param name
     * @return null if no match found
     */
    public Transit getTransit(String name) {
        Transit z = getByUserName(name);
        if (z!=null) return z;
        return getBySystemName(name);
    }

    public Transit getBySystemName(String name) {
		String key = name.toUpperCase();
        return (Transit)_tsys.get(key);
    }

    public Transit getByUserName(String key) {
        return (Transit)_tuser.get(key);
    }

    /**
     * Remove an existing Transit
	 */
    public void deleteTransit(Transit z) {
		// delete the Transit				
        deregister(z);
		z.dispose();
    }
	/**
	 * Returns a list of Transits which use a specified Section
	 */
	public ArrayList<Transit> getListUsingSection(Section s) {
		ArrayList<Transit> list = new ArrayList<Transit>();
		List<String> tList = getSystemNameList();
		for (int i = 0; i < tList.size(); i++) {
			String tName = tList.get(i);
			if ( (tName!=null) && (tName.length()>0) ) {
				Transit tTransit = getTransit(tName);
				if (tTransit!=null) {
					if (tTransit.containsSection(s)) {
						// this Transit uses the specified Section
						list.add(tTransit);
					}
				}
			}
		}
		return list;
	}
	
    static TransitManager _instance = null;
    static public TransitManager instance() {
        if (_instance == null) {
            _instance = new TransitManager();
        }
        return (_instance);
    }
	
    static Logger log = Logger.getLogger(TransitManager.class.getName());
}


/* @(#)TransitManager.java */
