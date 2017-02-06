package sample;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.event.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;

import java.awt.*;
import java.awt.Event;
import java.io.*;
import java.sql.Time;
import java.util.*;
import java.util.List;

public class Controller {

    public ChoiceBox nameCom;
    public Button stopCom;
    public Button ttmpBtn;
    public Label labelError;

    public Button btnDelete;
    private  SerialPort serialPort;

    public Button btnAdd;
    public Button btnRemove;
    public Button btnRemoveall;
    public Button btnWork;
    public ListView listCodes;
    public ListView listFile;
    public ListView listEror;
    public Label labelCHcod;
    public Button btnRead;
    public TextField markaTxt;
    public Button startCom;
    public String portData = "";

    ObservableList<File> listF = FXCollections.observableList(new ArrayList<>());
    ObservableList<String> listS =FXCollections.observableList(new ArrayList<>());
    ObservableList<String> listE =FXCollections.observableList(new ArrayList<>());
    TreeSet<String> strCode = new TreeSet();
    String[] serialPortList;
    @FXML
//private Button btnAdd;

    private  void initialize(){
        //listFile.setStyle("-fx-font:  bold italic 12pt Lucida Console;");
        listCodes.setStyle("-fx-font:  bold italic 8pt 'Lucida Console';");
        listEror.setStyle("-fx-font:  bold italic 8pt 'Lucida Console';");
        labelCHcod.setStyle("-fx-font:  bold italic 14pt 'Lucida Console';");
        labelCHcod.setText("");
        labelError.setStyle("-fx-font:  bold italic 14pt 'Lucida Console';");
        labelError.setText("");
        //markaTxt.setEditable(false);
        markaTxt.setDisable(true);
        serialPortList = SerialPortList.getPortNames();
        ObservableList<String> nC = FXCollections.observableList(Arrays.asList(serialPortList));
        nameCom.setItems(nC);
        nameCom.getSelectionModel().selectLast();
        listCodes.setItems(listS);
        listEror.setItems(listE);

    }

    public void btnAddClick(ActionEvent actionEvent) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));


        Stage primaryStage = new Stage();
        //File selectedFile = fileChooser.showOpenDialog(primaryStage);
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(primaryStage);


        if (selectedFiles.size()!=0) {
            ObservableList<File> tempFile = FXCollections.observableList(selectedFiles);
            listF.addAll(tempFile);
        }


        listFile.setItems(listF);


    }

    public void btnRemoveAll(ActionEvent actionEvent) {

        listF = FXCollections.observableList(new ArrayList<>());
        listFile.setItems(listF);
        listS.clear();

        strCode = new TreeSet<>();
        listE.clear();

        labelCHcod.setText(" ");
        labelError.setText(" ");
        //listFile.setItems(listF);
    }

    public void btnRemoveclick(ActionEvent actionEvent) {
        //File selectedFile = (File)listFile.getSelectionModel().getSelectedItems();
        File selectedFile =(File) listFile.getSelectionModel().getSelectedItem();
      listF.remove(selectedFile);
        listFile.setItems(listF);
    }

    public void btnWorkclick(ActionEvent actionEvent) throws Exception {

        BufferedReader reader;
        //strCode = new TreeSet<>();
        for (File file : listF) {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String temp = "";

            while((temp = reader.readLine())!= null){
                for (int i = 0; i <temp.length() ; i+=68) {
                    String temp1= temp.substring(i,i+68);
                    strCode.add(temp1);
                }
            }
            reader.close();

        }
        listS.clear();

        listS.addAll(strCode);

        labelCHcod.setText(String.valueOf(listS.size()));


        ProverkaPropuskov();
        labelError.setText(String.valueOf(listE.size()));

    }

    private void ProverkaPropuskov() {

            listE.clear();
            for (int i = 1; i < listS.size(); i++) {


                if (getCode(listS.get(i)) - getCode(listS.get(i - 1)) > 1) {

                    //String temp = getCode(listS.get(i-1))+"---" +getCode(listS.get(i));
                    int finish = getCode(listS.get(i));

                    int start = getCode(listS.get(i - 1));

                    for (int j = start + 1; j < finish; j++) {

                        listE.add(String.valueOf(j));
                    }


                }
            }


    }



    int getCode(String code){
        return Integer.parseInt(code.substring(31,37));
    }

    public void btnSaveClick(ActionEvent actionEvent) throws Exception {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Resource File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));

        Stage primaryStage = new Stage();
        File selectedFile = fileChooser.showSaveDialog(primaryStage);


        PrintWriter writer = null;
        if (selectedFile!=null) {
            writer = new PrintWriter(selectedFile);
            for (String s : listS) {
                writer.println(s);
            }
            writer.flush();
            writer.close();
        }


    }

   


    public void btnReadPress(ActionEvent actionEvent) {
       // markaTxt.setEditable(true);
        if (btnRead.getText().equals("Читать")){
            markaTxt.setDisable(false);
            markaTxt.requestFocus();
            markaTxt.selectAll();
            btnRead.setText("Стоп");
        }
        else{
            markaTxt.setDisable(true);
            btnRead.setText("Читать");
            ProverkaPropuskov();
        }


    }




    public void markaKeyEnter(ActionEvent actionEvent) {
        //labelCHcod.setText(markaTxt.getText());
        if (markaTxt.getText().startsWith("22N000")) {
            strCode.add(markaTxt.getText());
            listS.clear();
            //long  t1 = System.currentTimeMillis();
            listS.addAll(strCode);
            //long  t2 = System.currentTimeMillis();
            //System.out.println(t2-t1);
            labelCHcod.setText(String.valueOf(listS.size()));
            //t1 = System.currentTimeMillis();
            ProverkaPropuskov();
            //t2 = System.currentTimeMillis();
            //System.out.println(t2-t1);
            labelError.setText(String.valueOf(listE.size()));
            markaTxt.selectAll();
        }

    }

    public void startComClick(ActionEvent actionEvent) {
        stopCom.setDisable(false);
        startCom.setDisable(true);
        String nameComPort = nameCom.getValue().toString();
        //System.out.println(nameComPort);
        serialPort = new SerialPort(nameComPort);
        try {
            //Открываем порт
            serialPort.openPort();
            //Выставляем параметры
            serialPort.setParams(SerialPort.BAUDRATE_115200,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            //Включаем аппаратное управление потоком
            //serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN |
            //        SerialPort.FLOWCONTROL_RTSCTS_OUT);
            //Устанавливаем ивент лисенер и маску
            serialPort.addEventListener(new PortReader(), SerialPort.MASK_RXCHAR);
            //Отправляем запрос устройству
            //serialPort.writeString("Get data");
        }
        catch (SerialPortException ex) {
            System.out.println(ex);
        }
    }

    public void stopComClick(ActionEvent actionEvent) {
        stopCom.setDisable(true);
        startCom.setDisable(false);
        try {
            serialPort.closePort();
        } catch (SerialPortException e) {
            e.printStackTrace();
        }
    }

    public void tmpBtnClick(ActionEvent actionEvent) {
        if (portData.length()%68==0) {
            for (int i = 0; i < portData.length(); i += 68) {
                /*System.out.println(portData);
                System.out.println(portData.length());*/
                String temp1 = portData.substring(i, i + 68);

                if (temp1.substring(0,6).equals("22N000")) {
                    strCode.add(temp1);
                }
            }
            //strCode.add(portData);
            listS.clear();
            listS.addAll(strCode);

            listCodes.refresh();
            //listS.add(data);
            labelCHcod.setText(String.valueOf(listS.size()));

            ProverkaPropuskov();
            labelError.setText(String.valueOf(listE.size()));
        }

    }

    public void btnDeleteclick(ActionEvent actionEvent) {

        String item = (String) listCodes.getSelectionModel().getSelectedItem();
        //System.out.println(item);
        strCode.remove(item);
        listS.clear();
        listS.addAll(strCode);
        ProverkaPropuskov();
        labelCHcod.setText(String.valueOf(listS.size()));
        labelError.setText(String.valueOf(listE.size()));
    }

    private  class PortReader implements SerialPortEventListener {

        public void serialEvent(SerialPortEvent event) {
            if(event.isRXCHAR() && event.getEventValue() > 0){
                try {
                    //Получаем ответ от устройства, обрабатываем данные и т.д.
                    String data = serialPort.readString(event.getEventValue());
                    portData = data;
                    Platform.runLater(() -> javafx.event.Event.fireEvent(ttmpBtn, new ActionEvent()));

                     }
                catch (SerialPortException ex) {
                    System.out.println("Error read port");
                }
            }

        }
    }
}
