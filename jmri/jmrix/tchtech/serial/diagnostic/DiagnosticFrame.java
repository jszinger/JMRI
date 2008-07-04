/*
 * DiagnosticFrame.java
 *
 * Created on August 18, 2007, 7:56 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jmri.jmrix.tchtech.serial.diagnostic;

/**
 *
 * @author tim
 */
import jmri.jmrix.tchtech.serial.SerialMessage;
import jmri.jmrix.tchtech.serial.SerialReply;
import jmri.jmrix.tchtech.serial.SerialTrafficController;
import jmri.jmrix.tchtech.serial.SerialNode;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.border.Border;
import javax.swing.*;

import java.lang.Integer;


public class DiagnosticFrame extends jmri.util.JmriJFrame implements jmri.jmrix.tchtech.serial.SerialListener {

    // member declarations
    protected boolean outTest = true;
    protected boolean wrapTest = false;
    protected boolean isMICRO = false;
    protected boolean isTERA = true;
    protected boolean isPICO = false;
    protected boolean isMEGA = false;
// Here add other node types
    protected int numOutputCards = 2;
    protected int numInputCards = 1;
    protected int numCards = 3;
    protected int na = 0;               // node address
    protected SerialNode node;
    protected int outCardNum = 0;
    protected int obsDelay = 500;     
    protected int inCardNum = 2;
    protected int filterDelay = 0;
   // Test running variables  
    protected boolean testRunning = false;
    protected boolean testSuspended = false;  // true when Wraparound is suspended by error 
    protected byte[] outBytes = new byte[256];
    protected int curOutByte = 0;   // current output byte in output test
    protected int curOutBit = 0;    // current on bit in current output byte in output test
    protected short curOutValue = 0;  // current ofoutput byte in wraparound test
    protected int nOutBytes = 6;    // number of output bytes for all cards of this node
    protected int begOutByte = 0;   // numbering from zero, subscript in outBytes
    protected int endOutByte = 2;
    protected byte[] inBytes = new byte[256];
    protected byte[] wrapBytes = new byte[4];
    protected int nInBytes = 3;    // number of input bytes for all cards of this node
    protected int begInByte = 0;   // numbering from zero, subscript in inBytes
    protected int endInByte = 2;
    protected int numErrors = 0;
    protected int numIterations = 0;    
    protected javax.swing.Timer outTimer;  
    protected javax.swing.Timer wrapTimer;
    protected boolean waitingOnInput = false;  
    protected boolean needInputTest = false;
    protected int count = 20;
    int debugCount = 0;
    javax.swing.ButtonGroup testGroup = new javax.swing.ButtonGroup();
    javax.swing.JRadioButton outputButton = new javax.swing.JRadioButton("Output Test   ",true);
    javax.swing.JRadioButton wrapButton = new javax.swing.JRadioButton("Wraparound Test",false);

    javax.swing.JTextField naAddrField = new javax.swing.JTextField(3);
    javax.swing.JTextField outCardField = new javax.swing.JTextField(3);
    javax.swing.JTextField inCardField = new javax.swing.JTextField(3);
    javax.swing.JTextField obsDelayField = new javax.swing.JTextField(5);
    javax.swing.JTextField filterDelayField = new javax.swing.JTextField(5);

    javax.swing.JButton runButton = new javax.swing.JButton("Run");
    javax.swing.JButton stopButton = new javax.swing.JButton("Stop");
    javax.swing.JButton continueButton = new javax.swing.JButton("Continue");
    
    javax.swing.JLabel statusText1 = new javax.swing.JLabel();
    javax.swing.JLabel statusText2 = new javax.swing.JLabel();
    
    DiagnosticFrame curFrame;
    
    public DiagnosticFrame() {
        curFrame = this;
    }

    public void initComponents() throws Exception {

        // set the frame's initial state
        setTitle("Run TCH Technology NICS Diagnostic");
        setSize(1000,1000);
        Container contentPane = getContentPane();        
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        // Set up the test type panel
        JPanel panel1 = new JPanel();
        testGroup.add(outputButton);
        testGroup.add(wrapButton);
        panel1.add(outputButton);
        panel1.add(wrapButton);
        Border panel1Border = BorderFactory.createEtchedBorder();
        Border panel1Titled = BorderFactory.createTitledBorder(panel1Border,"Test Type");
        panel1.setBorder(panel1Titled);                
        contentPane.add(panel1);
        
        // Set up the test setup panel
        JPanel panel2 = new JPanel();
        panel2.setLayout(new BoxLayout(panel2, BoxLayout.Y_AXIS));
        JPanel panel21 = new JPanel();
        panel21.setLayout(new FlowLayout());
        panel21.add(new JLabel("Node(NA):"));
        panel21.add(naAddrField);
        naAddrField.setToolTipText("Enter node address, numbering from 0.");
        naAddrField.setText("0");
        panel21.add(new JLabel("  Out Tab:"));
        panel21.add(outCardField);
        outCardField.setToolTipText("Enter output Tab number, numbering from 0.");
        outCardField.setText("0");
        JPanel panel22 = new JPanel();
        panel22.setLayout(new FlowLayout());
        panel22.add(new JLabel("Output Test Only - Observation Delay:"));
        panel22.add(obsDelayField);
        obsDelayField.setToolTipText("Enter delay (milliseconds) between changes of output led's.");
        obsDelayField.setText("500");        
        JPanel panel23 = new JPanel();
        panel23.setLayout(new FlowLayout());
        panel23.add(new JLabel("Wraparound Test Only - In Tab:"));
        panel23.add(inCardField);
        inCardField.setToolTipText("Enter input Tab number, numbering from 0.");
        inCardField.setText("2");
        panel23.add(new JLabel("   Filtering Delay:"));
        panel23.add(filterDelayField);
        filterDelayField.setToolTipText("Enter delay (milliseconds) if input card has filtering, else 0.");
        filterDelayField.setText("0");        
        panel2.add(panel21);
        panel2.add(panel22);
        panel2.add(panel23);
        Border panel2Border = BorderFactory.createEtchedBorder();
        Border panel2Titled = BorderFactory.createTitledBorder(panel2Border,"Test Set Up");
        panel2.setBorder(panel2Titled);                
        contentPane.add(panel2);

        // Set up the status panel
        JPanel panel3 = new JPanel();
        panel3.setLayout(new BoxLayout(panel3, BoxLayout.Y_AXIS));
        JPanel panel31 = new JPanel();
        panel31.setLayout(new FlowLayout());
        statusText1.setText("Please ensure test hardware is installed.");
        statusText1.setVisible(true);
        statusText1.setMaximumSize(new Dimension(statusText1.getMaximumSize().width,
                            statusText1.getPreferredSize().height) );
        panel31.add(statusText1);
        JPanel panel32 = new JPanel();
        panel32.setLayout(new FlowLayout());
        statusText2.setText("Select Test Type, enter Test Set Up information, then select Run below.");
        statusText2.setVisible(true);
        statusText2.setMaximumSize(new Dimension(statusText2.getMaximumSize().width,
                            statusText2.getPreferredSize().height) );
        panel32.add(statusText2);
        panel3.add(panel31);
        panel3.add(panel32);
        Border panel3Border = BorderFactory.createEtchedBorder();
        Border panel3Titled = BorderFactory.createTitledBorder(panel3Border,"Status");
        panel3.setBorder(panel3Titled);                
        contentPane.add(panel3);
        
        // Set up Continue, Stop, Run buttons
        JPanel panel4 = new JPanel();
        panel4.setLayout(new FlowLayout());
        continueButton.setText("Continue");
        continueButton.setVisible(true);
        continueButton.setToolTipText("Continue Current Test");
        continueButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                continueButtonActionPerformed(e);
            }
        });
        panel4.add(continueButton);
        stopButton.setText("Stop");
        stopButton.setVisible(true);
        stopButton.setToolTipText("Stop Test");
        panel4.add(stopButton);
        stopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                stopButtonActionPerformed(e);
            }
        });
        runButton.setText("Run");
        runButton.setVisible(true);
        runButton.setToolTipText("Run Test");
        panel4.add(runButton);
        runButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                runButtonActionPerformed(e);
            }
        });
        contentPane.add(panel4);

        addHelpMenu("package.jmri.jmrix.tchtech.serial.diagnostic.DiagnosticFrame", true);

        // pack for display
        pack();
    }

    /**
     * Method to handle run button in Diagnostic Frame
     */        
    public void runButtonActionPerformed(java.awt.event.ActionEvent e) {
        // Ignore button if test is already running
        if (!testRunning) {
            // Read the user entered data, and report any errors
            if ( readSetupData() ) {
                if (outTest) {
                    // Initialize output test
                    if (initializeOutputTest()) {
                        // Run output test
                        runOutputTest();
                    }
                }
                else if (wrapTest) {
                    // Initialize wraparound test
                    if (initializeWraparoundTest()) {
                        // Run wraparound test
                        runWraparoundTest();
                    }
                }
            }
        }
    }
    
    /**
     * Local method to read data in Diagnostic Frame, get node data, and test for consistency
     *    Returns 'true' if no errors are found
     *    Returns 'false' if errors are found
     *    If errors are found, the errors are noted in 
     *      the status panel of the Diagnostic Frame
     */        
    protected boolean readSetupData() {
        // determine test type
        outTest = outputButton.isSelected();
        wrapTest = wrapButton.isSelected();
        // read setup data - Node(NA) field
        try 
        {
            na = Integer.parseInt(naAddrField.getText());
        }
        catch (Exception e)
        {
            statusText1.setText("Error - Bad character in Node(NA) field, please try again.");
            statusText1.setVisible(true);
            return (false);
        }
        if ( (na < 0) || (na > 256) ) {
            statusText1.setText("Error - Node(NA) is not between 0 and 255, please try again.");
            statusText1.setVisible(true);
            return (false);
        }
        // get the SerialNode corresponding to this node address
        node = SerialTrafficController.instance().getNodeFromAddress(na);
        if (node == null) {
            statusText1.setText("Error - Unknown address in Node(NA) field, please try again.");
            statusText1.setVisible(true);
            return (false);
        }     
        // determine if node is MICRO, NIC, or 
        int type = node.getNodeType();
        isMICRO = (type==SerialNode.MICRO);
        isTERA = (type==SerialNode.TERA);
        isPICO = (type==SerialNode.PICO);
        isMEGA = (type==SerialNode.MEGA);
// Here insert code for other type nodes
        // initialize numInputCards, numOutputCards, and numCards
        numOutputCards = node.numOutputCards();
        numInputCards = node.numInputCards();
        numCards = numOutputCards + numInputCards;

        // read setup data - Out Card field
        try 
        {
            outCardNum = Integer.parseInt(outCardField.getText());
        }
        catch (Exception e)
        {
            statusText1.setText("Error - Bad character in Out Tab field, please try again.");
            statusText1.setVisible(true);
            return (false);
        }
        // Check for consistency with Node definition
        if ( isTERA ) {
            if ( (outCardNum < 0) || (outCardNum >= numCards) ) {
                statusText1.setText("Error - Out Tab is not between 0 and "+Integer.toString(numCards-1)+
                                                                        ", please try again.");
                statusText1.setVisible(true);
                return (false);
            }
            if (!node.isOutputCard(outCardNum)) {
                statusText1.setText("Error - Out Tab is not an Output Tab in your Node definition, "+
                                                                            "please try again.");
                statusText1.setVisible(true);
                return (false);
            }
        }
        if (isMICRO && ( (outCardNum < 0) || (outCardNum > 1) ) ) {
            statusText1.setText("Error - Out Tab is not 0 or 1, please try again.");
            statusText1.setVisible(true);
            return (false);
        }

        if (outTest) {
            // read setup data - Observation Delay field
            try 
            {
                obsDelay = Integer.parseInt(obsDelayField.getText());
            }
            catch (Exception e)
            {
                statusText1.setText("Error - Bad character in Observation Delay field, please try again.");
                statusText1.setVisible(true);
                return (false);
            }
        }

        if (wrapTest) {
            // read setup data - In Card field
            try 
            {
                inCardNum = Integer.parseInt(inCardField.getText());
            }
            catch (Exception e)
            {
                statusText1.setText("Error - Bad character in In Tab field, please try again.");
                statusText1.setVisible(true);
                return (false);
            }
            // Check for consistency with Node definition
            if (isTERA) {
                if ( (inCardNum < 0) || (inCardNum >= numCards) )  {
                    statusText1.setText("Error - In Tab is not between 0 and "+
                                            Integer.toString(numCards-1)+", please try again.");
                    statusText1.setVisible(true);
                    return (false);
                }
                if (!node.isInputCard(inCardNum)) {
                    statusText1.setText("Error - In Tab is not an Input Card in your Node definition, "+
                                                                            "please try again.");
                    statusText1.setVisible(true);
                    return (false);
                }
            }
            if (isMICRO && (inCardNum != 2) ) {
                statusText1.setText("Error - In Tab not 2 for MICRO, please try again.");
                statusText1.setVisible(true);
                return (false);
            }
            if (isPICO && (inCardNum != 2) ) {
                statusText1.setText("Error - In Tab not 2 for Pico, please try again.");
                statusText1.setVisible(true);
                return (false);
            }
            if (isMEGA && (inCardNum != 2) ) {
                statusText1.setText("Error - In Tab not 2 for Mega, please try again.");
                statusText1.setVisible(true);
                return (false);
            }



            // read setup data - Filtering Delay field
            try 
            {
                filterDelay = Integer.parseInt(filterDelayField.getText());
            }
            catch (Exception e)
            {
                statusText1.setText("Error - Bad character in Filtering Delay field, please try again.");
                statusText1.setVisible(true);
                return (false);
            }
        }
        
        // complete initialization of output card
       int portsPerCard = (node.getNumBitsPerCard())/8;
       begOutByte = (node.getOutputCardIndex(outCardNum)) * portsPerCard;
       endOutByte = begOutByte + portsPerCard - 1;
       nOutBytes = numOutputCards * portsPerCard;
       // if wraparound test, complete initialization of the input card
       if (wrapTest) {
           begInByte = (node.getInputCardIndex(inCardNum)) * portsPerCard;
           endInByte = begInByte + portsPerCard - 1;
           nInBytes = numInputCards * portsPerCard;
       }        
        return (true);
    }

    /**
     * Method to handle continue button in Diagnostic Frame
     */        
    public void continueButtonActionPerformed(java.awt.event.ActionEvent e) {
        if (testRunning && testSuspended) {
            testSuspended = false;
            if (wrapTest) {
                statusText1.setText("Running Wraparound Test");
                statusText1.setVisible(true);
            }
        }
    }

    /**
     * Method to handle Stop button in Diagnostic Frame
     */
    public void stopButtonActionPerformed(java.awt.event.ActionEvent e) {
        // Ignore button push if test is not running, else change flag
        if (testRunning) {
            if (outTest) {
                stopOutputTest();
            }
            else if (wrapTest) {
                stopWraparoundTest();
            }
            testRunning = false;
        }
   }
    
    /**
     * Local Method to initialize an Output Test
     *    Returns 'true' if successfully initialized
     *    Returns 'false' if errors are found
     *    If errors are found, the errors are noted in 
     *      the status panel of the Diagnostic Frame
     */
    protected boolean initializeOutputTest() {
        // clear all output bytes for this node
        for (int i=0;i<nOutBytes;i++) {
            outBytes[i] = 0;
        }
        // check the entered delay--if too short an overrun could occur
        //     where the computer program is ahead of buffered serial output
        if (obsDelay<400) obsDelay = 400;
        // Set up beginning LED on position
        curOutByte = begOutByte;
        curOutBit = 0;
        // Send initialization message                
        SerialTrafficController.instance().sendSerialMessage(node.createInitPacket(),curFrame);
        try {
            // Wait for initialization to complete
            wait (500);    
        }
        catch (Exception e) {
            // Ignore exception and continue
        }
        // Initialization was successful
        numIterations = 0;
        testRunning = true;
        return (true);
    }
    
    /**
     * Local Method to run an Output Test
     */
    protected void runOutputTest() {
        // Set up timer to update output pattern periodically
        outTimer = new Timer(obsDelay,new ActionListener() {
            public void actionPerformed(ActionEvent evnt) 
            {
                if (testRunning && outTest) {
                    short[] outBitPattern = {0x01,0x02,0x04,0x08,0x10,0x20,0x40,0x80};
                    String[] portID = {"0","1","2","3"};
                    // set new pattern
                    outBytes[curOutByte] = (byte)outBitPattern[curOutBit];                    
                    // send new pattern
                    SerialMessage m = createOutPacket();
                    m.setTimeout(50);
                    SerialTrafficController.instance().sendSerialMessage(m,curFrame);
                    // update status panel to show bit that is on
                    statusText1.setText("Port "+portID[curOutByte-begOutByte]+" Line "+
                            Integer.toString(curOutBit)+
                                    " is on - Compare LED's with the pattern below");
                    statusText1.setVisible(true);
                    String st = "";
                    for (int i = begOutByte;i<=endOutByte;i++) {
                        st = st + "  ";
                        for (int j = 0;j<8;j++) {
                            if ( (i==curOutByte) && (j==curOutBit) )
                                st = st + " X";
                            else
                                st = st + " O";
                        }
                    }                        
                    statusText2.setText(st);
                    statusText2.setVisible(true);                        
                    // update bit pattern for next entry
                    curOutBit ++;
                    if (curOutBit>7) {
                        // Move to the next byte
                        curOutBit = 0;
                        outBytes[curOutByte] = 0;
                        curOutByte ++;
                        if (curOutByte>endOutByte) {
                            // Pattern complete, recycle to first byte
                            curOutByte = begOutByte;
                            numIterations ++;
                        }
                    }
                }
            }
        });

        // start timer        
        outTimer.start();
    }
    
    /**
     * Local Method to stop an Output Test
     */
    protected void stopOutputTest() {
        if (testRunning && outTest) {
            // Stop the timer
            outTimer.stop();
            // Update the status
            statusText1.setText("Output Test stopped after "+
                                    Integer.toString(numIterations)+" Cycles");
            statusText1.setVisible(true);
            statusText2.setText("  ");
            statusText2.setVisible(true);
        }
    }
    
    /**
     * Local Method to initialize a Wraparound Test
     *    Returns 'true' if successfully initialized
     *    Returns 'false' if errors are found
     *    If errors are found, the errors are noted in 
     *      the status panel of the Diagnostic Frame
     */
    protected boolean initializeWraparoundTest() {
        // clear all output bytes for this node
        for (int i=0;i<nOutBytes;i++) {
            outBytes[i] = 0;
        }
        // Set up beginning output values
        curOutByte = begOutByte;
        curOutValue = 0;
        
        // Send initialization message                
        SerialTrafficController.instance().sendSerialMessage(node.createInitPacket(),curFrame);
        try {
            // Wait for initialization to complete
            wait (500);    
        }
        catch (Exception e) {
            // Ignore exception and continue
        }
        
        // Clear error count
        numErrors = 0;
        numIterations = 0;
        // Initialize running flags 
        testRunning = true;
        testSuspended = false;
        waitingOnInput = false;
        needInputTest = false;
        count = 50;
        return (true);
    }
    
    /**
     * Local Method to run a Wraparound Test
     */
    protected void runWraparoundTest() {
        // Display Status Message
        statusText1.setText("Running Wraparound Test");
        statusText1.setVisible(true);
        
        // Set up timer to update output pattern periodically
        wrapTimer = new Timer(100,new ActionListener() {
            public void actionPerformed(ActionEvent evnt) 
            {
                if (testRunning && !testSuspended) {
                    if (waitingOnInput) {
                        count --;
                        if (count==0) {
                            statusText2.setText("Time Out Error - no response after 5 seconds.");
                            statusText2.setVisible(true);
                        }
                    }
                    else {
                        // compare input with previous output if needed
                        if (needInputTest) {
                            needInputTest = false;
                            boolean comparisonError = false;
                            // compare input and output bytes
                            int j = 0;
                            for (int i = begInByte;i<=endInByte;i++,j++) {
                                if (inBytes[i] != wrapBytes[j]) {
                                    comparisonError = true;
                                }
                            }
                            if (comparisonError) {
                                // report error and suspend test
                                statusText1.setText
                                    ("Test Suspended for Error - Stop or Continue?");
                                statusText1.setVisible(true);
                                String st = "Compare Error - Out Bytes (hex):";
                                for (int i = begOutByte;i<=endOutByte;i++) {
                                    st += " " + Integer.toHexString(((int)outBytes[i])&0x000000ff);
                                }
                                st += "    In Bytes (hex):";
                                for (int i = begInByte;i<=endInByte;i++) {
                                    st += " " + Integer.toHexString(((int)inBytes[i])&0x000000ff);
                                }
                                statusText2.setText(st);
                                statusText2.setVisible(true);
                                numErrors ++;
                                testSuspended = true;
                                return;
                            }
                        }

                        // send next output pattern
                        outBytes[curOutByte] = (byte) curOutValue;
                        if (isMICRO)
                        if (isPICO)
                        if (isMEGA){
                            // If MICRO , send same pattern to both output cards
                            if (curOutByte > 2) {
                                outBytes[curOutByte-3] = (byte) curOutValue;
                            }
                            else {
                                outBytes[curOutByte+3] = (byte) curOutValue;
                            }
                        }
                        SerialMessage m = createOutPacket();
                        // wait for signal to settle down if filter delay
                        m.setTimeout(50 + filterDelay);
                        SerialTrafficController.instance().sendSerialMessage(m,curFrame);

                        // update Status area
                        short[] outBitPattern = {0x01,0x02,0x04,0x08,0x10,0x20,0x40,0x80};
                        String[] portID = {"A","B","C","D"};
                        String st = "Port: "+portID[curOutByte-begOutByte]+",  Pattern: ";
                        for (int j = 0;j < 8;j++) {
                            if ( (curOutValue&outBitPattern[j]) != 0 ) {
                                st = st + " X";
                            }
                            else {
                                st = st + " O";
                            }
                        }    
                        statusText2.setText(st);
                        statusText2.setVisible(true);

                        // set up for testing input returned
                        int k = 0;
                        for (int i = begOutByte;i<=endOutByte;i++, k++) {
                            wrapBytes[k] = outBytes[i];
                        }
                        waitingOnInput = true;
                        needInputTest = true;
                        count = 50;
                        // send poll
                        SerialTrafficController.instance().sendSerialMessage(
                                        SerialMessage.getPoll(na),curFrame);

                        // update output pattern for next entry
                        curOutValue ++;
                        if (curOutValue>255) {
                            // Move to the next byte
                            curOutValue = 0;
                            outBytes[curOutByte] = 0;
                            if (isMICRO)
                            if (isPICO)
                            if (isMEGA){
                                // If MICRO, PICO or MEGA clear ports of both output cards
                                if (curOutByte>2) {
                                    outBytes[curOutByte-3] = 0;
                                }
                                else {
                                    outBytes[curOutByte+3] = 0;
                                }
                            }
                            curOutByte ++;
                            if (curOutByte>endOutByte) {
                                // Pattern complete, recycle to first port (byte)
                                curOutByte = begOutByte;
                                numIterations ++;
                            }
                        }
                    }
                }
            }
        });

        // start timer        
        wrapTimer.start();
    }    
    
     /**
     * Local Method to stop a Wraparound Test
     */
    protected void stopWraparoundTest() {
        if (testRunning && wrapTest) {
            // Stop the timer
            wrapTimer.stop();
            // Update the status
            statusText1.setText("Wraparound Test Stopped, "+Integer.toString(numErrors)+
                                                                " Errors Found");
            statusText1.setVisible(true);
            statusText2.setText(Integer.toString(numIterations)+" Cycles Completed");
            statusText2.setVisible(true);
        }
    }
    
    /**
     * Local Method to create an Transmit packet (SerialMessage)
     */
    SerialMessage createOutPacket() {
        // Create a Serial message and add initial bytes
        SerialMessage m = new SerialMessage(nOutBytes + 2);
        m.setElement(0,na);  // node address
        m.setElement(1,0xE3);     // 'T'
        // Add nOutBytes output bytes to message
        int k = 2;
        for (int i=0; i<nOutBytes; i++) {
                m.setElement(k, outBytes[i]);
                k++;
        }
        return m;
    }

    /**
     * Message notification method to implement SerialListener interface
     */
   public void  message(SerialMessage m) {}  // Ignore for now 

    /**
     * Reply notification method to implement SerialListener interface
     */
    public synchronized void reply(SerialReply l) {  
        // Test if waiting on this input
        if ( waitingOnInput && (l.isRcv()) && (na == l.getNA()) ) {
            // This is a receive message for the node being tested
            for (int i = begInByte;i<=endInByte;i++) {
                // get data bytes, skipping over node address and 'R'
                inBytes[i] = (byte) l.getElement(i+2); // inBytes[i] = (byte) l.getElement(i+2);
            }
            waitingOnInput = false;
        }
    }

    /**
     * Stop operation when window closing
     */
    public void windowClosing(java.awt.event.WindowEvent e) {
        if (testRunning) {
            if (outTest) {
                stopOutputTest();
            }
            else if (wrapTest) {
                stopWraparoundTest();
            }
        }
        super.windowClosing(e);
    }
}
