package coat.view.vcf;

import coat.CoatView;
import coat.model.vcf.Variant;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class Phenolyzer extends VBox {

    private ObservableList<Variant> inputVariants;
    private final ObservableList<Variant> outputVariants = FXCollections.observableArrayList();

    private final TextArea textArea = new TextArea();
    private final Button button = new Button("Go");

    private final static String PHENLOYZER = new File(System.getProperty("user.dir"), "Coat/software/phenolyzer/disease_annotation.pl").getAbsolutePath();
    private final static String POIROT_SCORE = "poirot_score";

    public Phenolyzer() {
        textArea.setPromptText("Enter some phenotyes");
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        button.setOnAction(event -> start());
        getChildren().setAll(textArea, button);
        setSpacing(5);
        setAlignment(Pos.CENTER);
        VBox.setVgrow(textArea, Priority.ALWAYS);
    }

    public void setInputVariants(ObservableList<Variant> inputVariants) {
        this.inputVariants = inputVariants;
        outputVariants.setAll(inputVariants);
        inputVariants.addListener((ListChangeListener<Variant>) change -> {
            change.next();
            if (change.wasAdded()) varianstAdded();
            else if (change.wasRemoved()) varianstRemoved();
        });
    }

    public ObservableList<Variant> getOutputVariants() {
        return outputVariants;
    }

    private void varianstRemoved() {
        outputVariants.setAll(inputVariants);
    }

    private void varianstAdded() {
        outputVariants.setAll(inputVariants);
    }

    private void start() {
        runPhenolyzer();
        Map<String, Double> scores = new TreeMap<>();
        File output = new File("out.final_gene_list");
        try (BufferedReader reader = new BufferedReader(new FileReader(output))) {
            reader.readLine();
            reader.lines().forEach(line -> {
                String[] row = line.split("\t");
                String gene = row[1];
                double score = Double.valueOf(row[3]);
                scores.put(gene, score);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(scores);
        final File file = new File(".");
        Arrays.stream(file.listFiles(pathname -> pathname.getName().startsWith("out"))).forEach(File::delete);
        outputVariants.forEach(variant -> {
                    String gene = (String) variant.getInfos().get("GNAME");
                    double score = (gene == null) ? 0 : scores.getOrDefault(gene, 0.0);
                    variant.getInfos().put(POIROT_SCORE, score);
                }
        );
        Collections.sort(outputVariants, (v1, v2) -> {
            double v1Score = (double) v1.getInfos().getOrDefault(POIROT_SCORE, 0.0);
            double v2Score = (double) v2.getInfos().getOrDefault(POIROT_SCORE, 0.0);
            int comp = Double.compare(v2Score, v1Score);
            return comp == 0 ? v1.compareTo(v2) : comp;
        });
    }

    private void runPhenolyzer() {
        createGeneList();
        createPhenotypeList();
        final String diseases = textArea.getText().replace("\n", "_");
        System.out.println(diseases);
        ProcessBuilder processBuilder = new ProcessBuilder("perl", PHENLOYZER, "-p", "-ph", "-f", "--gene", "genes.list", "phenotypes.list");
        processBuilder.redirectErrorStream(true);
        try {
            Process process = processBuilder.start();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            reader.lines().forEach(line -> CoatView.printMessage(line, "info"));
            reader.close();
            System.out.println("Return code: " + process.exitValue());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createGeneList() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("genes.list"))) {
            inputVariants.stream().map(variant -> (String) variant.getInfos().get("GNAME")).
                    distinct().filter(s -> s != null).forEach(gene -> {
                try {
                    writer.write(gene);
                    writer.newLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createPhenotypeList() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("phenotypes.list"))) {
            Arrays.stream(textArea.getText().split("\n")).forEach(phenotype -> {
                try {
                    if (!phenotype.isEmpty()){
                        writer.write(phenotype);
                        writer.newLine();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
