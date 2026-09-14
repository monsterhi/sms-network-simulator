package Presentation;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import javax.swing.*;

import Common.BaseDeviceLayerListener;
import Common.SMSEvent;

import Models.BSCLayers;
import Models.BSCLayersEvent;
import Models.BSCLayersListener;
import Models.BTS;
import Models.BTSLayer;
import Models.VBD;
import Models.VBDLayer;
import Models.VRDLayer;

/**
 * The main window, and the place where the whole network is put together.
 *
 * The three panels of the BorderLayout are the senders on the left, the
 * stations in the middle and the receivers on the right. The constructor also
 * connects the layers to each other, so that a message travels along this
 * path:
 *
 * VBD -> BTS -> BSC ... BSC -> BTS -> VRD
 *
 * Each link is one listener: a layer hears that its devices have sent
 * something and passes it to the next layer.
 */
public class MainFrame extends JFrame{

    public MainFrame(){

        // The five layers the messages travel through, from left to right.
        BTSLayer btsLayerLeft = new BTSLayer();
        BSCLayers bscLayers = new BSCLayers();
        BTSLayer btsLayerRight = new BTSLayer();
        VRDLayer vrdLayer = new VRDLayer();
        VBDLayer vbdLayer = new VBDLayer(() -> {return vrdLayer.getRandomNumber();});

        vbdLayer.addListener(new BaseDeviceLayerListener<VBD>() {

            @Override
            public void onDeviceSendSMS(SMSEvent evt) {
                btsLayerLeft.receiveSMS(evt.getSMS());
            }
            
        });

        btsLayerLeft.addListener(new BaseDeviceLayerListener<BTS>() {

            @Override
            public void onDeviceSendSMS(SMSEvent evt) {
                bscLayers.receiveSMS(evt.getSMS());
            }
            
        });

        bscLayers.addListener(new BSCLayersListener() {

            @Override
            public void onDeviceSendSMS(SMSEvent evt) {
                btsLayerRight.receiveSMS(evt.getSMS());
            }

            @Override
            public void onLayerRemoved(BSCLayersEvent evt) {
            }

            @Override
            public void onLayerAdded(BSCLayersEvent evt) {
            }
            
        });
        
        btsLayerRight.addListener(new BaseDeviceLayerListener<BTS>() {

            @Override
            public void onDeviceSendSMS(SMSEvent evt) {
                vrdLayer.receiveSMS(evt.getSMS());
            }
        });

        // Each layer gets its own panel, which follows it through events.
        VBDLayerPanel vbdLayerPanel = new VBDLayerPanel(vbdLayer);
        StationLayerPanel<BTS> btsLeftLayerPanel = new StationLayerPanel<>(btsLayerLeft);
        BSCLayersPanel bscLayersPanel = new BSCLayersPanel(bscLayers);
        StationLayerPanel<BTS> btsRightLayerPanel = new StationLayerPanel<>(btsLayerRight);
        VRDLayerPanel vrdLayerPanel = new VRDLayerPanel(vrdLayer);

        JPanel stationsPanel = new JPanel(new BorderLayout());
        stationsPanel.add(btsLeftLayerPanel, BorderLayout.LINE_START);
        stationsPanel.add(bscLayersPanel, BorderLayout.CENTER);
        stationsPanel.add(btsRightLayerPanel, BorderLayout.LINE_END);

        this.getContentPane().add(vbdLayerPanel, BorderLayout.LINE_START);
        this.getContentPane().add(stationsPanel, BorderLayout.CENTER);
        this.getContentPane().add(vrdLayerPanel, BorderLayout.LINE_END);

        // The simulation always needs at least one intermediate BSC layer.
        bscLayers.ensureFirstLayer();

        // On exit the senders write their statistics into a binary file.
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    vbdLayer.saveInfo();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } 
            
        });

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(1283, 600);
        this.setVisible(true);
    }
}
