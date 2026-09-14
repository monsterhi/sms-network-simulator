package Presentation;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import Models.VBD;
import Models.VBDStateEnum;

/**
 * The visual form of one sending device: a slider for the sending frequency,
 * the device number, a state combo box and a button that shuts the device
 * down.
 *
 * The controls write straight into the model, and the device thread picks the
 * new values up on its next pass.
 */
public class VBDPanel extends JPanel{
    
    VBD vbd;

    public VBDPanel(VBD vbd){

        this.vbd = vbd;

        SpringLayout layout = new SpringLayout();
        this.setLayout(layout);
        
        var dimension = new Dimension(300, 150);
        this.setPreferredSize(dimension);
        this.setMinimumSize(dimension);
        this.setMaximumSize(dimension);

        var frequencySlider = new JSlider(JSlider.HORIZONTAL, 100, 500, this.vbd.getFrequency());
        frequencySlider.setMajorTickSpacing(100);
        frequencySlider.setMinorTickSpacing(10);
        frequencySlider.setPaintTicks(true);
        frequencySlider.setPaintLabels(true);
        frequencySlider.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                vbd.setFrequency(frequencySlider.getValue());
            }

        });

        var numberLabel = new JLabel("Device Number: ");
        var numberTextField = new JTextField(20);
        numberTextField.setEditable(false);
        numberTextField.setText("+" + this.vbd.getNumber());

        var stateLabel = new JLabel("State: ");
        var stateComboBox = new JComboBox<VBDStateEnum>(VBDStateEnum.values());
        stateComboBox.setSelectedItem(this.vbd.getState());
        stateComboBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                vbd.setState((VBDStateEnum)stateComboBox.getSelectedItem());
            }

        });

        var terminateButton = new JButton("Terminate");
        terminateButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                vbd.terminate();
            }

        });

        this.add(frequencySlider);
        this.add(numberLabel);
        this.add(numberTextField);
        this.add(stateLabel);
        this.add(stateComboBox);
        this.add(terminateButton);

        // The controls are stacked one under another, all of the same width.
        layout.putConstraint(SpringLayout.WEST, frequencySlider, 5, SpringLayout.WEST, this);
        layout.putConstraint(SpringLayout.NORTH, frequencySlider, 5, SpringLayout.NORTH, this);
        layout.putConstraint(SpringLayout.EAST, this, 5, SpringLayout.EAST, frequencySlider);

        layout.putConstraint(SpringLayout.WEST, numberLabel, 5, SpringLayout.WEST, this);
        layout.putConstraint(SpringLayout.NORTH, numberLabel, 5, SpringLayout.SOUTH, frequencySlider);
        layout.putConstraint(SpringLayout.WEST, numberTextField, 5, SpringLayout.EAST, numberLabel);
        layout.putConstraint(SpringLayout.NORTH, numberTextField, 5, SpringLayout.SOUTH, frequencySlider);
        layout.putConstraint(SpringLayout.EAST, numberTextField, 0, SpringLayout.EAST, frequencySlider);

        layout.putConstraint(SpringLayout.WEST, stateLabel, 5, SpringLayout.WEST, this);
        layout.putConstraint(SpringLayout.NORTH, stateLabel, 5, SpringLayout.SOUTH, numberTextField);
        layout.putConstraint(SpringLayout.WEST, stateComboBox, 0, SpringLayout.WEST, numberTextField);
        layout.putConstraint(SpringLayout.NORTH, stateComboBox, 5, SpringLayout.SOUTH, numberTextField);
        layout.putConstraint(SpringLayout.EAST, stateComboBox, 0, SpringLayout.EAST, numberTextField);

        layout.putConstraint(SpringLayout.WEST, terminateButton, 0, SpringLayout.WEST, stateComboBox);
        layout.putConstraint(SpringLayout.NORTH, terminateButton, 5, SpringLayout.SOUTH, stateComboBox);
        layout.putConstraint(SpringLayout.EAST, terminateButton, 0, SpringLayout.EAST, stateComboBox);
    }

    public VBD getVBD() {
        return this.vbd;
    }
}