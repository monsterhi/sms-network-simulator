package Presentation;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import javax.swing.*;

import Common.BaseDeviceLayerListener;
import Common.DeviceLayerEvent;

import Models.VBD;
import Models.VBDLayer;

/**
 * The left panel of the window: a scrollable list of sending devices with an
 * "Add" button below it.
 *
 * The panel only reacts to the layer. When a device is added or terminated,
 * the matching small panel appears or disappears here.
 */
public class VBDLayerPanel extends JPanel{

    VBDLayer vbdCollection;

    public VBDLayerPanel(VBDLayer vbdCollection){

        this.vbdCollection = vbdCollection;

        this.setLayout(new BorderLayout());

        this.setPreferredSize(new Dimension(320, 400));

        var contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        var scrollPane = new JScrollPane(contentPanel, 
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            
        var addButton = new JButton("Add");
        addButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // The text entered here is what the new device will keep sending.
                String message = (String)JOptionPane.showInputDialog("Enter message");
                if (message != null && !message.isEmpty()) {
                    VBDLayerPanel.this.vbdCollection.addVBD(message);
                }
            }

        });

        this.add(scrollPane, BorderLayout.CENTER);
        this.add(addButton, BorderLayout.PAGE_END);

        this.vbdCollection.addListener(new BaseDeviceLayerListener<VBD>() {

            @Override
            public void onDeviceAdded(DeviceLayerEvent<VBD> evt) {
                VBDPanel devicePanel = new VBDPanel(evt.getDevice());
                
                contentPanel.add(devicePanel);
                contentPanel.revalidate();
                contentPanel.repaint();
            }

            @Override
            public void onDeviceRemoved(DeviceLayerEvent<VBD> evt) {
                // Find the panel that belongs to the terminated device.
                var component = Arrays.stream(contentPanel.getComponents())
                    .filter(p -> p instanceof VBDPanel && ((VBDPanel)p).getVBD() == evt.getDevice())
                    .findFirst()
                    .get();
                
                contentPanel.remove(component);
                contentPanel.revalidate();
                contentPanel.repaint();
            }

        });
    }
}
