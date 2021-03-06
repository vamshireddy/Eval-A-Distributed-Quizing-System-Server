/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package GUI;

import com.bluewalrus.bar.Axis;
import com.bluewalrus.bar.Bar;
import com.bluewalrus.bar.Line;
import com.bluewalrus.bar.MultiBar;
import com.bluewalrus.bar.Utils;
import com.bluewalrus.bar.XAxis;
import com.bluewalrus.bar.YAxis;
import com.bluewalrus.chart.BarChart;
import com.bluewalrus.chart.MultiBarChart;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.Tick;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.util.Rotation;

/**
 *
 * @author vamshi
 */
public class ResponseStatistics extends javax.swing.JFrame {

    PieDataset dataset;
    boolean wait;
    /**
     * Creates new form ResponseStatistics
     */
    public ResponseStatistics() {
        
        initComponents();
        
        wait = true;
        // based on the dataset we create the chart
        
        // we put the chart into a panel

    }

    
    public boolean getWaitStatus()
    {
        return wait;
    }
        
    public void reset()
    {
        wait = true;
    }
/** * Creates a sample dataset */
    
    
    private void InitPieChart(JFreeChart chart)
    {
        ChartPanel chartPanel = new ChartPanel(chart);
        // default size
        chartPanel.setSize(560,800);
        
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - chartPanel.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - chartPanel.getHeight()) / 2);
        
        
        chartPanel.setLocation(WIDTH, WIDTH);
        // add it to our application
        setContentPane(chartPanel);
        
        chartPanel.addChartMouseListener(new ChartMouseListener() {

        public void chartMouseClicked(ChartMouseEvent e) {
            
            wait = false;
            
        }

        public void chartMouseMoved(ChartMouseEvent e) {}

        });
    }

    private  PieDataset createDatasetMultipleChoice(HashMap<String,Double> hm) {
        DefaultPieDataset result = new DefaultPieDataset();
   
        Set<String> set = hm.keySet();
        
        for( String s : set )
        {
            result.setValue(s, hm.get(s));
        }
        
        return result;
        
    }
    
    private  PieDataset createDatasetOneWord(HashMap<String,Double> hm) {
        DefaultPieDataset result = new DefaultPieDataset();
        
        Set<String> set = hm.keySet();
        
        for( String s : set )
        {
            result.setValue(s, hm.get(s));
        }
        
        
        return result;
        
    }
    
    private  PieDataset createDatasetTrueOrFalse(HashMap<String,Double> hm) {
        DefaultPieDataset result = new DefaultPieDataset();

        Set<String> set = hm.keySet();
        
        for( String s : set )
        {
            /*
                Convert into %
            */
            
            result.setValue(s, hm.get(s));
        }
        
        return result;
        
    }
    
    public void InitChart(String question, HashMap<String,Double> hm, int type)
    {
        /*
            True or false or One word based on the flag
        */
        if( type == 1 )
        {
            /*
                True or false
            */
            dataset = createDatasetMultipleChoice(hm);
            
        }
        else if( type == 2 )
        {
            /*
                One word
            */
            dataset = createDatasetTrueOrFalse(hm);

        }
        else if( type == 3 )
        {
            /*
                 Multiple choice
            */
            dataset = createDatasetOneWord(hm);
        }
                
        JFreeChart chart = createChart(dataset, "Response for the Question : "+question);
        
        InitPieChart(chart);
        
    }
    
/** * Creates a chart */

    private JFreeChart createChart(PieDataset dataset, String title) {
        
        JFreeChart chart = ChartFactory.createPieChart3D(title,          // chart title
            dataset,                // data
            true,                   // include legend
            true,
            false);

        PiePlot3D plot = (PiePlot3D) chart.getPlot();
        plot.setStartAngle(290);
        plot.setDirection(Rotation.CLOCKWISE);
        plot.setForegroundAlpha(0.5f);
        return chart;
        
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 800, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 560, Short.MAX_VALUE)
        );

        setSize(new java.awt.Dimension(810, 560));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

}
