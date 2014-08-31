package ch.brotzilla.monalisa.gui;

import java.text.DecimalFormat;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.vectorizer.Vectorizer;
import ch.brotzilla.util.MatrixLayout;

@SuppressWarnings("serial")
public class StatusDisplay extends JPanel {

    protected final Orientation orientation;
    
    protected final JLabel generatedName, generatedValue;
    protected final JLabel selectedName, selectedValue;
    protected final JLabel mutationsName, mutationsValue;
    protected final JLabel polygonsName, polygonsValue;
    protected final JLabel pointsName, pointsValue;
    protected final JLabel fitnessName, fitnessValue;
    protected final JLabel cacheName, cacheValue;
    protected final JLabel rateName, rateValue;
    
    protected final DecimalFormat ff = new DecimalFormat( "#,###,###,###,##0.00" );
    protected final DecimalFormat rf = new DecimalFormat("#,##0.00");
    protected final DecimalFormat pppf = new DecimalFormat("#0.00");
    
    public static enum Orientation {
        Horizontal,
        Vertical
    }
    
    public StatusDisplay(Orientation orientation) {
        super();
        
        Preconditions.checkNotNull(orientation, "The parameter 'orientation' must not be null");
        this.orientation = orientation;
        
        switch (orientation) {
        case Horizontal:
            setLayout(new MatrixLayout("Pref Pref Pref Pref", "33% Pref Pref 34% Pref Pref 33%"));
            break;
        case Vertical:
            setLayout(new MatrixLayout(Strings.repeat("Pref ", 8).trim(), "Pref Pref"));
            break;
        default:
            throw new IllegalArgumentException("Orientation not supported: " + orientation);
        }
        
        generatedName = new JLabel("Generated:");
        generatedValue = new JLabel("0");
        selectedName = new JLabel("Selected:");
        selectedValue = new JLabel("0");
        mutationsName = new JLabel("Mutations:");
        mutationsValue = new JLabel("0");
        polygonsName = new JLabel("Polygons:");
        polygonsValue = new JLabel("0");
        pointsName = new JLabel("Points:");
        pointsValue = new JLabel("0");
        fitnessName = new JLabel("Fitness:");
        fitnessValue = new JLabel("0");
        cacheName = new JLabel("Cached:");
        cacheValue = new JLabel("0");
        rateName = new JLabel("Rate:");
        rateValue = new JLabel("0.00/s");
        
        switch (orientation) {
        case Horizontal:
            add(generatedName, "row=1 col=2 hAlign=Right");
            add(selectedName, "row=Next col=Current hAlign=Right");
            add(mutationsName, "row=Next col=Current hAlign=Right");
            add(cacheName, "row=Next col=Current hAlign=Right");
            add(polygonsName, "row=1 col=5 hAlign=Right");
            add(pointsName, "row=Next col=Current hAlign=Right");
            add(fitnessName, "row=Next col=Current hAlign=Right");
            add(rateName, "row=Next col=Current hAlign=Right");
            add(generatedValue, "row=1 col=3 hAlign=Right");
            add(selectedValue, "row=Next col=Current hAlign=Right");
            add(mutationsValue, "row=Next col=Current hAlign=Right");
            add(cacheValue, "row=Next col=Current hAlign=Right");
            add(polygonsValue, "row=1 col=6 hAlign=Right");
            add(pointsValue, "row=Next col=Current hAlign=Right");
            add(fitnessValue, "row=Next col=Current hAlign=Right");
            add(rateValue, "row=Next col=Current hAlign=Right");
            break;
        case Vertical:
            add(generatedName, "row=1 col=1 hAlign=Right");
            add(selectedName, "row=Next col=Current hAlign=Right");
            add(mutationsName, "row=Next col=Current hAlign=Right");
            add(cacheName, "row=Next col=Current hAlign=Right");
            add(polygonsName, "row=Next col=Current hAlign=Right");
            add(pointsName, "row=Next col=Current hAlign=Right");
            add(fitnessName, "row=Next col=Current hAlign=Right");
            add(rateName, "row=Next col=Current hAlign=Right");
            add(generatedValue, "row=1 col=2 hAlign=Right");
            add(selectedValue, "row=Next col=Current hAlign=Right");
            add(mutationsValue, "row=Next col=Current hAlign=Right");
            add(cacheValue, "row=Next col=Current hAlign=Right");
            add(polygonsValue, "row=Next col=Current hAlign=Right");
            add(pointsValue, "row=Next col=Current hAlign=Right");
            add(fitnessValue, "row=Next col=Current hAlign=Right");
            add(rateValue, "row=Next col=Current hAlign=Right");
            break;
        default:
            throw new IllegalArgumentException("Orientation not supported: " + orientation);
        }
    }

    public void submit(Genome genome) {
        if (genome == null) {
            generatedValue.setText("0");
            selectedValue.setText("0");
            mutationsValue.setText("0");
            polygonsValue.setText("0");
            pointsValue.setText("0 (0.0)");
            fitnessValue.setText("0");
        } else {
            generatedValue.setText(genome.numberOfMutations + "");
            selectedValue.setText(genome.numberOfImprovements + "");
            polygonsValue.setText(genome.countPolygons() + "");
            final int cp = genome.countPoints();
            final double ppp = (double) cp / genome.countPolygons();
            pointsValue.setText(cp + " (" + pppf.format(ppp) + ")");
            fitnessValue.setText(ff.format(genome.fitness));
        }
    }
    
    public void update(Vectorizer v) {
        if (v == null) {
            generatedValue.setText("0");
            selectedValue.setText("0");
            rateValue.setText("0.00/s");
        } else {
            generatedValue.setText(v.getVectorizerContext().getNumberOfMutations() + "");
            selectedValue.setText(v.getVectorizerContext().getNumberOfImprovements() + "");
            rateValue.setText(rf.format(v.getTickRate().getTickRate()) + "/s");
        }
    }
}
