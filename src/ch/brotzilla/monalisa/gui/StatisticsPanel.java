package ch.brotzilla.monalisa.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.LinkedList;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.google.common.base.Preconditions;

import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.io.SessionManager;

@SuppressWarnings("serial")
public class StatisticsPanel extends JPanel {

    protected final SessionManager sessionManager;
    protected final LinkedList<Genome> genomes = new LinkedList<Genome>();
    
    public StatisticsPanel(SessionManager sessionManager) {
        super();
        
        this.sessionManager = Preconditions.checkNotNull(sessionManager, "The parameter 'sessionManager' must not be null");
        
//        System.out.println("Loading stored genomes...");
//        final int loaded = sessionManager.loadGenomes(genomes);
//        System.out.println(loaded + " genomes found.");
        
        final XYDataset dataset = createDataset();
        final JFreeChart chart = createChart(dataset);
        final ChartPanel chartPanel = new ChartPanel(chart);
        
        setLayout(new BorderLayout());
        add(chartPanel, BorderLayout.CENTER);
    }

    private JFreeChart createChart(XYDataset dataset) {
        final JFreeChart chart = ChartFactory.createXYLineChart("Fitness", "Time", "Fitness Value", dataset, PlotOrientation.VERTICAL, true, true, false);

        chart.setBackgroundPaint(Color.white);

        final XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);

        final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, true);
        renderer.setSeriesShapesVisible(0, false);
        plot.setRenderer(renderer);

        return chart;
    }

    private XYDataset createDataset() {
        
        final XYSeries series1 = new XYSeries("Fitness");

        for (final Genome g : genomes) {
            series1.add(g.selected, g.fitness);
        }

        final XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series1);

        return dataset;
    }

}
