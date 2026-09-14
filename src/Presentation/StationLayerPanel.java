package Presentation;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Arrays;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import Common.BaseDeviceLayerListener;
import Common.DeviceLayerEvent;
import Common.Station;
import Common.StationLayer;

/**
 * One column of stations drawn vertically. The same panel serves both BTS
 * layers and every BSC layer, because all of them add and remove stations in
 * the same way.
 *
 * @param <T> the concrete station type, BTS or BSC
 */
public class StationLayerPanel<T extends Station> extends JPanel{

    StationLayer<T> stationLayer;

    public StationLayerPanel(StationLayer<T> stationLayer){

        this.stationLayer = stationLayer;

        this.setLayout(new BorderLayout());

        this.setPreferredSize(new Dimension(150, 400));

        var contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        var scrollPane = new JScrollPane(contentPanel, 
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            
        this.add(scrollPane, BorderLayout.CENTER);

        this.stationLayer.addListener(new BaseDeviceLayerListener<T>() {

            @Override
            public void onDeviceAdded(DeviceLayerEvent<T> evt) {
                StationPanel devicePanel = new StationPanel(evt.getDevice());
                
                contentPanel.add(devicePanel);
                contentPanel.revalidate();
                contentPanel.repaint();
            }

            @Override
            public void onDeviceRemoved(DeviceLayerEvent<T> evt) {
                // Find the panel that belongs to the removed station.
                var component = Arrays.stream(contentPanel.getComponents())
                    .filter(p -> p instanceof StationPanel && ((StationPanel)p).getStation() == evt.getDevice())
                    .findFirst()
                    .get();
                
                contentPanel.remove(component);
                contentPanel.revalidate();
                contentPanel.repaint();
            }
        });
    }

    public StationLayer<T> getLayer() {
        return stationLayer;
    }
}
