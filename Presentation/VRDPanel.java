package Presentation;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

import Common.BaseDeviceListener;
import Common.SMSEvent;

import Models.VRD;

/**
 * The visual form of one receiving device: its number, how many messages it
 * has received, a check box that clears that counter every ten seconds, and a
 * button that shuts the device down.
 */
public class VRDPanel extends JPanel{

    VRD vrd;

    public VRDPanel(VRD vrd){
        this.vrd = vrd;

        SpringLayout layout = new SpringLayout();
        this.setLayout(layout);
        
        var dimension = new Dimension(200, 105);
        this.setPreferredSize(dimension);
        this.setMinimumSize(dimension);
        this.setMaximumSize(dimension);

        var numberLabel = new JLabel("Device Number: ");
        var numberTextField = new JTextField(20);
        numberTextField.setEditable(false);
        numberTextField.setText("+" + this.vrd.getNumber());

        var receivedCountLabel = new JLabel("Received count:   " + this.vrd.getReceivedCount());

        var clearReceivedCountCheckBox = new JCheckBox("Clear received count");
        clearReceivedCountCheckBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                vrd.setClearReceivedCount(clearReceivedCountCheckBox.isSelected());
            }
            
        });

        var terminateButton = new JButton("Terminate");
        terminateButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                vrd.terminate();
            }

        });

        this.add(numberLabel);
        this.add(numberTextField);
        this.add(terminateButton);
        this.add(receivedCountLabel);
        this.add(clearReceivedCountCheckBox);

        // The controls are stacked one under another, all of the same width.
        layout.putConstraint(SpringLayout.WEST, numberLabel, 5, SpringLayout.WEST, this);
        layout.putConstraint(SpringLayout.NORTH, numberLabel, 5, SpringLayout.NORTH, this);
        layout.putConstraint(SpringLayout.WEST, numberTextField, 5, SpringLayout.EAST, numberLabel);
        layout.putConstraint(SpringLayout.NORTH, numberTextField, 5, SpringLayout.NORTH, this);
        layout.putConstraint(SpringLayout.EAST, this, 5, SpringLayout.EAST, numberTextField);
        
        layout.putConstraint(SpringLayout.WEST, receivedCountLabel, 0, SpringLayout.WEST, numberLabel);
        layout.putConstraint(SpringLayout.NORTH, receivedCountLabel, 5, SpringLayout.SOUTH, numberLabel);
        layout.putConstraint(SpringLayout.EAST, receivedCountLabel, 0, SpringLayout.EAST, numberTextField);
        
        layout.putConstraint(SpringLayout.WEST, clearReceivedCountCheckBox, 0, SpringLayout.WEST, numberLabel);
        layout.putConstraint(SpringLayout.NORTH, clearReceivedCountCheckBox, 5, SpringLayout.SOUTH, receivedCountLabel);
        layout.putConstraint(SpringLayout.EAST, clearReceivedCountCheckBox, 0, SpringLayout.EAST, numberTextField);
    
        layout.putConstraint(SpringLayout.WEST, terminateButton, 0, SpringLayout.WEST, numberTextField);
        layout.putConstraint(SpringLayout.NORTH, terminateButton, 5, SpringLayout.SOUTH, clearReceivedCountCheckBox);
        layout.putConstraint(SpringLayout.EAST, terminateButton, 0, SpringLayout.EAST, numberTextField);
        
        // Keep the label in step with the device as messages arrive.
        vrd.addListener(new BaseDeviceListener() {

            @Override
            public void onReceiveSMS(SMSEvent evt) {
                receivedCountLabel.setText("Received count:   " + vrd.getReceivedCount());
                receivedCountLabel.revalidate();
                receivedCountLabel.repaint();
            }

        });
    }

    public VRD getVRD() {
        return this.vrd;
    }
}
