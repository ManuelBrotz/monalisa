package ch.brotzilla.monalisa.gui;

import java.text.DecimalFormat;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import ch.brotzilla.monalisa.evolution.genes.Genome;
import ch.brotzilla.monalisa.vectorizer.VectorizerConfig;
import ch.brotzilla.util.MatrixLayout;

@SuppressWarnings("serial")
public class StatusDisplay extends JPanel {

    protected final Orientation orientation;
    
    protected final JLabel generatedName, generatedValue;
    protected final JLabel selectedName, selectedValue;
    protected final JLabel polygonsName, polygonsValue;
    protected final JLabel pointsName, pointsValue;
    protected final JLabel fitnessName, fitnessValue;
    protected final JLabel rateName, rateValue;
    
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
        polygonsName = new JLabel("Polygons:");
        polygonsValue = new JLabel("0");
        pointsName = new JLabel("Points:");
        pointsValue = new JLabel("0");
        fitnessName = new JLabel("Fitness:");
        fitnessValue = new JLabel("0");
        rateName = new JLabel("Rate:");
        rateValue = new JLabel("0.00/s");
        
        switch (orientation) {
        case Horizontal:
            add(generatedName, "row=1 col=2 hAlign=Right");
            add(selectedName, "row=Next col=Current hAlign=Right");
            add(rateName, "row=Next col=Current hAlign=Right");
            add(polygonsName, "row=1 col=5 hAlign=Right");
            add(pointsName, "row=Next col=Current hAlign=Right");
            add(fitnessName, "row=Next col=Current hAlign=Right");
            add(generatedValue, "row=1 col=3 hAlign=Right");
            add(selectedValue, "row=Next col=Current hAlign=Right");
            add(rateValue, "row=Next col=Current hAlign=Right");
            add(polygonsValue, "row=1 col=6 hAlign=Right");
            add(pointsValue, "row=Next col=Current hAlign=Right");
            add(fitnessValue, "row=Next col=Current hAlign=Right");
            break;
        case Vertical:
            add(generatedName, "row=1 col=1 hAlign=Right");
            add(selectedName, "row=Next col=Current hAlign=Right");
            add(rateName, "row=Next col=Current hAlign=Right");
            add(polygonsName, "row=Next col=Current hAlign=Right");
            add(pointsName, "row=Next col=Current hAlign=Right");
            add(fitnessName, "row=Next col=Current hAlign=Right");
            add(generatedValue, "row=1 col=2 hAlign=Right");
            add(selectedValue, "row=Next col=Current hAlign=Right");
            add(rateValue, "row=Next col=Current hAlign=Right");
            add(polygonsValue, "row=Next col=Current hAlign=Right");
            add(pointsValue, "row=Next col=Current hAlign=Right");
            add(fitnessValue, "row=Next col=Current hAlign=Right");
            break;
        default:
            throw new IllegalArgumentException("Orientation not supported: " + orientation);
        }
    }

    public void submit(VectorizerConfig config, Genome genome) {
        if (genome == null) {
            generatedValue.setText("0");
            selectedValue.setText("0");
            polygonsValue.setText("0");
            pointsValue.setText("0 (0.0)");
            fitnessValue.setText("0");
        } else {
            Preconditions.checkNotNull(config, "The parameter 'config' must not be null");
            generatedValue.setText(genome.numberOfMutations + "");
            selectedValue.setText(genome.numberOfImprovements + "");
            polygonsValue.setText(genome.countPolygons() + "");
            final int cp = genome.countPoints();
            final double ppp = (double) cp / genome.countPolygons();
            pointsValue.setText(cp + " (" + pppf.format(ppp) + ")");
            fitnessValue.setText(config.getFitnessFunction().format(genome.fitness));
        }
    }
    
    public void update(VectorizerConfig config, double rate) {
        if (config == null) {
            generatedValue.setText("0");
            selectedValue.setText("0");
            rateValue.setText("0.00/s");
        } else {
            Preconditions.checkNotNull(config, "The parameter 'config' must not be null");
            generatedValue.setText(config.getVectorizerContext().getNumberOfMutations() + "");
            selectedValue.setText(config.getVectorizerContext().getNumberOfImprovements() + "");
            rateValue.setText(rf.format(rate) + "/s");
        }
    }
}
