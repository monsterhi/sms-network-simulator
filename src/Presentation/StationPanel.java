package Presentation;

import java.awt.Dimension;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import Common.BaseDeviceListener;
import Common.SMSEvent;
import Common.Station;

/**
 * The visual form of one station: its number, how many messages it has passed
 * on and how many are still waiting in its queue. Both numbers are refreshed
 * whenever the station receives or sends something.
 */
public class StationPanel extends JPanel{
    
    Station station;

    public StationPanel(Station station){

        this.station = station;

        SpringLayout layout = new SpringLayout();
        this.setLayout(layout);
        
        var dimension = new Dimension(150, 100);
        this.setPreferredSize(dimension);
        this.setMinimumSize(dimension);
        this.setMaximumSize(dimension);

        var numberLabel = new JLabel("Device Number: " + this.station.getNumber());
        var processedLabel = new JLabel("Processed: " + this.station.getProcessedCount());
        var waitingLabel = new JLabel("Waiting: " + this.station.getSMSQueueSize());

        this.add(numberLabel);
        this.add(processedLabel);
        this.add(waitingLabel);

        // The labels are stacked one under another, all of the same width.
        layout.putConstraint(SpringLayout.WEST, numberLabel, 5, SpringLayout.WEST, this);
        layout.putConstraint(SpringLayout.NORTH, numberLabel, 5, SpringLayout.NORTH, this);
        layout.putConstraint(SpringLayout.EAST, this, 5, SpringLayout.EAST, numberLabel);

        layout.putConstraint(SpringLayout.WEST, processedLabel, 0, SpringLayout.WEST, numberLabel);
        layout.putConstraint(SpringLayout.NORTH, processedLabel, 5, SpringLayout.SOUTH, numberLabel);
        layout.putConstraint(SpringLayout.EAST, processedLabel, 0, SpringLayout.EAST, numberLabel);

        layout.putConstraint(SpringLayout.WEST, waitingLabel, 0, SpringLayout.WEST, processedLabel);
        layout.putConstraint(SpringLayout.NORTH, waitingLabel, 5, SpringLayout.SOUTH, processedLabel);
        layout.putConstraint(SpringLayout.EAST, waitingLabel, 0, SpringLayout.EAST, processedLabel);
        
        this.station.addListener(new BaseDeviceListener(){

            @Override
            public void onReceiveSMS(SMSEvent evt) {
                waitingLabel.setText("Waiting: " + station.getSMSQueueSize());
                waitingLabel.revalidate();
                waitingLabel.repaint();
            }

            @Override
            public void onSendSMS(SMSEvent evt) {
                waitingLabel.setText("Waiting: " + station.getSMSQueueSize());
                waitingLabel.revalidate();
                waitingLabel.repaint();
                
                processedLabel.setText("Processed: " + station.getProcessedCount());
                processedLabel.revalidate();
                processedLabel.repaint();
            }
        });
    }

    public Station getStation() {
        return station;
    }
}
