import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class Controller {
    public TextField Q1;
    public TextField Q2;
    public Button btnLoadFromFile;
    public Button btnProcess;

    public TextArea taCodeField;
    public TableView<TableViewChapin> tvFullChapin;
    public TableView<TableViewChapin> tvIOChapin;
    public TableColumn<TableViewChapin, String> fM;
    public TableColumn<TableViewChapin, String> fP;
    public TableColumn<TableViewChapin, String> fC;
    public TableColumn<TableViewChapin, String> fT;
    public TableColumn<TableViewChapin, String> iP;
    public TableColumn<TableViewChapin, String> iM;
    public TableColumn<TableViewChapin, String> iC;
    public TableColumn<TableViewChapin, String> iT;
    public TableColumn<TokenEl,String> sV;
    public TableColumn<TokenEl,Integer> sA;
    public TableView<TokenEl> tvSpen;
    public TextField p1;
    public TextField m1;
    public TextField c1;
    public TextField t1;
    public TextField p2;
    public TextField m2;
    public TextField c2;
    public TextField t2;
    public TextField sSum;

    private File dlgToOpenFile(boolean isSave){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите файл");
        fileChooser.setInitialDirectory(new File("C:\\Users\\Aleksej\\Desktop\\Лабораторные\\МСИС\\3"));
//        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Текстовые файлы (*.txt)", "*.txt"));
        return isSave ? fileChooser.showSaveDialog(View.Interface) : fileChooser.showOpenDialog(View.Interface);
    }

    @FXML
    private void btnLoadFromFileOnClick(MouseEvent e) {
        File selectedFile = dlgToOpenFile(false);

        if (selectedFile == null) return;
        Charset charset = StandardCharsets.UTF_8;
        StringBuilder fileInformation = new StringBuilder();
        try(BufferedReader reader = Files.newBufferedReader(selectedFile.toPath(), charset)) {
            String line;
            while ((line = reader.readLine()) != null) fileInformation.append(line).append('\n');
            taCodeField.setText(fileInformation.toString());
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }
    }

    private TableCell<TableViewChapin,String> cellFactory(){
        TableCell<TableViewChapin,String> cell = new TableCell<>();
        Text text = new Text();
        cell.setGraphic(text);
        cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
        text.wrappingWidthProperty().bind(cell.widthProperty());
        text.textProperty().bind(cell.itemProperty());
        return cell;
    }

    @FXML
    private void btnCount(MouseEvent e){
        int P1,M1,C1,T1,P2,M2,C2,T2;
        double q1,q2;

        PythonTokenizer.tokenFlow = PythonTokenizer.tokenize(taCodeField.getText());
        PythonTokenizer.count();

        ObservableList<TableViewChapin> fullChapin = FXCollections.observableArrayList();
        ObservableList<TableViewChapin> ioChapin = FXCollections.observableArrayList();
        ObservableList<TokenEl> spenTable = FXCollections.observableArrayList();

        fullChapin.add(new TableViewChapin(PythonTokenizer.fullChapinMetric));
        fP.setCellValueFactory(new PropertyValueFactory<TableViewChapin, String>("P"));
        fM.setCellValueFactory(new PropertyValueFactory<TableViewChapin, String>("M"));
        fC.setCellValueFactory(new PropertyValueFactory<TableViewChapin, String>("C"));
        fT.setCellValueFactory(new PropertyValueFactory<TableViewChapin, String>("T"));
        fP.setCellFactory(tc->cellFactory());
        fM.setCellFactory(tc->cellFactory());
        fC.setCellFactory(tc->cellFactory());
        fT.setCellFactory(tc->cellFactory());
        tvFullChapin.setItems(fullChapin);

        ioChapin.add(new TableViewChapin(PythonTokenizer.ioChapinMetric));
        iP.setCellValueFactory(new PropertyValueFactory<TableViewChapin, String>("P"));
        iM.setCellValueFactory(new PropertyValueFactory<TableViewChapin, String>("M"));
        iC.setCellValueFactory(new PropertyValueFactory<TableViewChapin, String>("C"));
        iT.setCellValueFactory(new PropertyValueFactory<TableViewChapin, String>("T"));
        iP.setCellFactory(tc->cellFactory());
        iM.setCellFactory(tc->cellFactory());
        iC.setCellFactory(tc->cellFactory());
        iT.setCellFactory(tc->cellFactory());
        tvIOChapin.setItems(ioChapin);

        PythonTokenizer.fullChapinMetric.forEach((key,value) ->{
            spenTable.add(new TokenEl(key,value.count));
        });
        sV.setCellValueFactory(new PropertyValueFactory<>("token"));
        sA.setCellValueFactory(new PropertyValueFactory<>("count"));
        tvSpen.setItems(spenTable);


        P1 = PythonTokenizer.countByType(PythonTokenizer.fullChapinMetric,EVariableGroup.PROCESSING);
        M1 = PythonTokenizer.countByType(PythonTokenizer.fullChapinMetric,EVariableGroup.MODIFIED);
        C1 = PythonTokenizer.countByType(PythonTokenizer.fullChapinMetric,EVariableGroup.CHOICE);
        T1 = PythonTokenizer.countByType(PythonTokenizer.fullChapinMetric,EVariableGroup.TROPHIC);

        p1.setText(String.valueOf(P1));
        m1.setText(String.valueOf(M1));
        c1.setText(String.valueOf(C1));
        t1.setText(String.valueOf(T1));

        P2 = PythonTokenizer.countByType(PythonTokenizer.ioChapinMetric,EVariableGroup.PROCESSING);
        M2 = PythonTokenizer.countByType(PythonTokenizer.ioChapinMetric,EVariableGroup.MODIFIED);
        C2 = PythonTokenizer.countByType(PythonTokenizer.ioChapinMetric,EVariableGroup.CHOICE);
        T2 = PythonTokenizer.countByType(PythonTokenizer.ioChapinMetric,EVariableGroup.TROPHIC);

        p2.setText(String.valueOf(P2));
        m2.setText(String.valueOf(M2));
        c2.setText(String.valueOf(C2));
        t2.setText(String.valueOf(T2));

        q1 = 1*P1 + 2*M1 + 3*C1 + 0.5*T1;
        q2 = 1*P2 + 2*M2 + 3*C2 + 0.5*T2;

        Q1.setText(String.format("1*%d + 2*%d + 3*%d + 0.5*%d = %.1f",P1,M1,C1,T1,q1));
        Q2.setText(String.format("1*%d + 2*%d + 3*%d + 0.5*%d = %.1f",P2,M2,C2,T2,q2));
        sSum.setText(String.valueOf(PythonTokenizer.getSum(PythonTokenizer.fullChapinMetric)));
    }
}
