package ro.mpp2024;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class MainController implements IMotociclismObserver {

    @FXML
    private TableView<Cursa> cursaTable;
    @FXML
    private TableColumn<Cursa, Long> colId;
    @FXML
    private TableColumn<Cursa, Integer> colCapacitate;
    @FXML
    private TableColumn<Cursa, Integer> colNrParticipanti;

    @FXML
    private TableView<Participant> participantTable;
    @FXML
    private TableColumn<Participant, Long> colParticipantId;
    @FXML
    private TableColumn<Participant, String> colNume;
    @FXML
    private TableColumn<Participant, String> colEchipa;

    @FXML
    private TextField echipaField;
    @FXML
    private TextField numeField;
    @FXML
    private TextField cnpField;
    @FXML
    private TextField echipaInscriereField;
    @FXML
    private TextField cursaIdField;
    @FXML
    private TextField participantIdField;
    @FXML
    private TextField echipaNovaField;
    @FXML
    private Label statusLabel;

    private IMotociclismServices service;
    private User currentUser;

    public void setService(IMotociclismServices service) {
        this.service = service;
    }

    public void setUser(User user) {
        this.currentUser = user;
        initTable();
    }

    private void initTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCapacitate.setCellValueFactory(new PropertyValueFactory<>("capacitateMotor"));
        colNrParticipanti.setCellValueFactory(new PropertyValueFactory<>("id"));
        colParticipantId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNume.setCellValueFactory(new PropertyValueFactory<>("nume"));
        colEchipa.setCellValueFactory(new PropertyValueFactory<>("echipa"));
        loadCurse();
    }

    private void loadCurse() {
        try {
            ObservableList<Cursa> curse = FXCollections.observableArrayList();
            service.getCurseDisponibile().forEach(curse::add);
            cursaTable.setItems(curse);
        } catch (MotociclismException e) {
            setStatus("Eroare la incarcarea curselor: " + e.getMessage(), false);
        }
    }

    @FXML
    private void handleCauta() {
        String echipa = echipaField.getText();
        if (echipa.isEmpty()) {
            setStatus("Introduceți numele echipei!", false);
            return;
        }
        try {
            ObservableList<Participant> participanti = FXCollections.observableArrayList();
            service.cautaParticipanti(echipa).forEach(participanti::add);
            participantTable.setItems(participanti);
            setStatus("Găsiți " + participanti.size() + " participanți.", true);
        } catch (MotociclismException e) {
            setStatus("Eroare: " + e.getMessage(), false);
        }
    }

    @FXML
    private void handleInscrie() {
        String nume = numeField.getText();
        String cnp = cnpField.getText();
        String echipa = echipaInscriereField.getText();
        String cursaIdText = cursaIdField.getText();

        if (nume.isEmpty() || cnp.isEmpty() || cursaIdText.isEmpty()) {
            setStatus("Completați toate câmpurile!", false);
            return;
        }

        try {
            Long cursaId = Long.parseLong(cursaIdText);
            Cursa cursa = service.getCursaById(cursaId);
            if (cursa == null) {
                setStatus("Cursa nu există!", false);
                return;
            }
            Participant participant = new Participant(nume, cnp, echipa);
            service.inscrie(participant, cursa);
            setStatus("Participant înscris cu succes!", true);
        } catch (NumberFormatException e) {
            setStatus("ID cursă invalid!", false);
        } catch (MotociclismException e) {
            setStatus("Eroare: " + e.getMessage(), false);
        }
    }

    @FXML
    private void handleModifica() {
        String idText = participantIdField.getText();
        String echipaNoua = echipaNovaField.getText();

        if (idText.isEmpty() || echipaNoua.isEmpty()) {
            setStatus("Completați ID-ul și echipa nouă!", false);
            return;
        }

        try {
            Long id = Long.parseLong(idText);
            service.modificaEchipa(id, echipaNoua);
            setStatus("Echipa modificată cu succes!", true);
        } catch (NumberFormatException e) {
            setStatus("ID participant invalid!", false);
        } catch (MotociclismException e) {
            setStatus("Eroare: " + e.getMessage(), false);
        }
    }

    @FXML
    private void handleLogout() {
        try {
            service.logout(currentUser, this);
        } catch (MotociclismException e) {
            setStatus("Eroare la logout: " + e.getMessage(), false);
        }
        Stage stage = (Stage) statusLabel.getScene().getWindow();
        stage.close();
    }

    @Override
    public void inscriereEfectuata(Inscriere inscriere) throws MotociclismException {
        Platform.runLater(() -> {
            loadCurse();
            // reincarca si participantii daca e o echipa cautata deja
            String echipa = echipaField.getText();
            if (!echipa.isEmpty()) {
                handleCauta();
            }
        });
    }

    @Override
    public void echipaModificata(Participant participant) throws MotociclismException {
        Platform.runLater(() -> {
            // reincarca participantii daca e o echipa cautata
            String echipa = echipaField.getText();
            if (!echipa.isEmpty()) {
                handleCauta();
            }
        });
    }

    private void setStatus(String message, boolean success) {
        statusLabel.setText(message);
        statusLabel.setStyle(success ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
    }
}