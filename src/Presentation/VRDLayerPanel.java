package Presentation;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import javax.swing.*;

import Common.BaseDeviceLayerListener;
import Common.DeviceLayerEvent;

import Models.VRD;
import Models.VRDLayer;

/**
 * The right panel of the window: a scrollable list of receiving devices with
 * an "Add" button below it. It follows the layer in the same way as
 * {@link VBDLayerPanel} does.
 */
public class VRDLayerPanel extends JPanel{

    VRDLayer vrdCollection;

    public VRDLayerPanel(VRDLayer vrdCollection){

        this.vrdCollection = vrdCollection;

        this.setLayout(new BorderLayout());

        this.setPreferredSize(new Dimension(220, 400));

        var contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        var scrollPane = new JScrollPane(contentPanel, 
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            
        var addButton = new JButton("Add");
        addButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                VRDLayerPanel.this.vrdCollection.addVRD();
            }

        });

        this.add(scrollPane, BorderLayout.CENTER);
        this.add(addButton, BorderLayout.PAGE_END);

        this.vrdCollection.addListener(new BaseDeviceLayerListener<VRD>() {

            @Override
            public void onDeviceAdded(DeviceLayerEvent<VRD> evt) {
                VRDPanel devicePanel = new VRDPanel(evt.getDevice());

                contentPanel.add(devicePanel);                    
                contentPanel.revalidate();
                contentPanel.repaint();
            }

            @Override
            public void onDeviceRemoved(DeviceLayerEvent<VRD> evt) {
                // Find the panel that belongs to the terminated device.
                var component = Arrays.stream(contentPanel.getComponents())
                    .filter(p -> p instanceof VRDPanel && ((VRDPanel)p).getVRD() == evt.getDevice())
                    .findFirst()
                    .get();

                contentPanel.remove(component);
                contentPanel.revalidate();
                contentPanel.repaint();
            }

        });
    }
}
