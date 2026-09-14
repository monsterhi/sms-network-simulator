package Presentation;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import javax.swing.*;

import Common.SMSEvent;

import Models.BSC;
import Models.BSCLayers;
import Models.BSCLayersEvent;
import Models.BSCLayersListener;

/**
 * The middle part of the window: all BSC layers side by side, with the
 * buttons that add a layer or remove the last one.
 *
 * Every layer is drawn by its own {@link StationLayerPanel}, arranged
 * horizontally in the order the messages pass through them.
 */
public class BSCLayersPanel extends JPanel{

    BSCLayers bscLayers;

    public BSCLayersPanel(BSCLayers bscLayers){
        
        this.bscLayers = bscLayers;

        this.setLayout(new BorderLayout());

        this.setPreferredSize(new Dimension(150, 400));

        var contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.X_AXIS));

        var scrollPane = new JScrollPane(contentPanel, 
            JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
            
        var addButton = new JButton("Add");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bscLayers.addLayer();
            }
        });

        var removeButton = new JButton("Remove");
        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bscLayers.removeLayer();
            }
        });

        var buttonsPanel = new JPanel();
        buttonsPanel.add(addButton);
        buttonsPanel.add(removeButton);

        this.add(scrollPane, BorderLayout.CENTER);
        this.add(buttonsPanel, BorderLayout.PAGE_END);

        this.bscLayers.addListener(new BSCLayersListener () {

            @Override
            public void onLayerAdded(BSCLayersEvent evt) {
                StationLayerPanel<BSC> layerPanel = new StationLayerPanel<BSC>(evt.getLayer());
                
                contentPanel.add(layerPanel);
                contentPanel.revalidate();
                contentPanel.repaint();
            }

            @Override
            public void onLayerRemoved(BSCLayersEvent evt) {
                // Find the panel that belongs to the removed layer.
                var component = Arrays.stream(contentPanel.getComponents())
                    .filter(p -> p instanceof StationLayerPanel && ((StationLayerPanel<BSC>)p).getLayer() == evt.getLayer())
                    .findFirst()
                    .get();
                
                contentPanel.remove(component);
                contentPanel.revalidate();
                contentPanel.repaint();
            }

            @Override
            public void onDeviceSendSMS(SMSEvent evt) {
            }
        });
    }
}
