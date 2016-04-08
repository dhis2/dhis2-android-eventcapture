package org.hisp.dhis.android.eventcapture.model;

/**
 * A class to store states of the fragments.
 */
public class SessionManager {
    private static SessionManager instance;
    private boolean selectorSynced = false;

    private SessionManager() {

    }

    public static SessionManager getInstance() {
        if(instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public boolean isSelectorSynced() {
        return selectorSynced;
    }

    public void setSelectorSynced(Boolean state) {
        selectorSynced = state;
    }

}
