// ProgModePane.java

package jmri;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import jmri.Programmer;
import jmri.ProgListener;

/**
 * Provide a JPanel to configure the programming mode.
 * <P>
 * The using code should get a configured programmer with getProgrammer.
 * <P>
 * This pane will only display ops mode options if ops mode is available,
 * as evidenced by an attempt to get an ops mode programmer at startup time.
 * <P>
 * For service mode, you can get the programmer either from the JPanel
 * or direct from the instance manager.  For ops mode, you have to
 * get it from here.
 * <P>
 * Note that you should call the dispose() method when you're really done, so that
 * a ProgModePane object can disconnect its listeners.
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Revision: 1.7 $
 */
public class ProgModePane extends javax.swing.JPanel {

    // GUI member declarations
    ProgOpsModePane     mOpsPane;
    ProgServiceModePane mServicePane;
    ButtonGroup group = new ButtonGroup();

    /**
     * @param direction controls layout, either BoxLayout.X_AXIS or BoxLayout.Y_AXIS
     */
    public ProgModePane(int direction) {

        // general GUI config
        setLayout(new BoxLayout(this, direction));

        // service mode support, always present
        mServicePane = new ProgServiceModePane(direction, group);
        add(mServicePane);

        // ops mode support
        if (InstanceManager.programmerManagerInstance()!=null &&
            InstanceManager.programmerManagerInstance().isOpsModePossible()) {

            add(new JSeparator());
            mOpsPane = new ProgOpsModePane(direction, group);
            add(mOpsPane);
        }

    }

    /**
     * Get the configured programmer
     */
    public Programmer getProgrammer() {
        if (InstanceManager.programmerManagerInstance()==null) {
            log.warn("request for programmer with no ProgrammerManager configured");
            return null;
        } else if (mServicePane.isSelected()) {
            return mServicePane.getProgrammer();
        } else if (mOpsPane.isSelected()) {
            return mOpsPane.getProgrammer();
        } else return null;
    }

    public void dispose() {
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ProgModePane.class.getName());

}
