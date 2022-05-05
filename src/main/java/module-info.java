module com.example.calculadorafx {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.calculadorafx to javafx.fxml;
    exports com.example.calculadorafx;
}