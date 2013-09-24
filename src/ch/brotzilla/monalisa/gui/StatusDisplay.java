package ch.brotzilla.monalisa.gui;

import java.text.DecimalFormat;

import javax.swing.JLabel;
import javax.swing.JPanel;

import ch.brotzilla.monalisa.genes.Genome;

@SuppressWarnings("serial")
public class StatusDisplay extends JPanel {

    protected final JLabel generatedName, generatedValue;
    protected final JLabel selectedName, selectedValue;
    protected final JLabel mutationsName, mutationsValue;
    protected final JLabel polygonsName, polygonsValue;
    protected final JLabel pointsName, pointsValue;
    protected final JLabel fitnessName, fitnessValue;
    
    protected final DecimalFormat ff = new DecimalFormat( "#,###,###,###,##0.00" );
    
    public StatusDisplay() {
        super();
        
        final MatrixLayout l = new MatrixLayout(
                "Pref Pref Pref", 
                "33% Pref Pref 34% Pref Pref 33%"
                );
        
        setLayout(l);
        
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
        
        add(generatedName, "row=1 col=2 hAlign=Right");
        add(selectedName, "row=Next col=Current hAlign=Right");
        add(mutationsName, "row=Next col=Current hAlign=Right");
        add(polygonsName, "row=1 col=5 hAlign=Right");
        add(pointsName, "row=Next col=Current hAlign=Right");
        add(fitnessName, "row=Next col=Current hAlign=Right");
        
        add(generatedValue, "row=1 col=3 hAlign=Right");
        add(selectedValue, "row=Next col=Current hAlign=Right");
        add(mutationsValue, "row=Next col=Current hAlign=Right");
        add(polygonsValue, "row=1 col=6 hAlign=Right");
        add(pointsValue, "row=Next col=Current hAlign=Right");
        add(fitnessValue, "row=Next col=Current hAlign=Right");
    }

    public void submit(Genome genome) {
        if (genome == null) {
            generatedValue.setText("0");
            selectedValue.setText("0");
            mutationsValue.setText("0");
            polygonsValue.setText("0");
            pointsValue.setText("0");
            fitnessValue.setText("0");
        } else {
            generatedValue.setText(genome.generated + "");
            selectedValue.setText(genome.selected + "");
            mutationsValue.setText(genome.mutations + "");
            polygonsValue.setText(genome.genes.length + "");
            pointsValue.setText(genome.countPoints() + "");
            fitnessValue.setText(ff.format(genome.fitness));
        }
    }
}
