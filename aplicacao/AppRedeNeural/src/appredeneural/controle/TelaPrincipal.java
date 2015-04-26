package appredeneural.controle;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;

/**
 * FXML Controller class
 *
 * @author Diego Heusser
 */
public class TelaPrincipal implements Initializable {

    @FXML
    private Canvas canvas;

    @FXML
    private Button btIdentificar;

    @FXML
    private Button btLimpar;

    private GraphicsContext gc;

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        gc = canvas.getGraphicsContext2D();
        iniciarEventos();
    }

    /**
     * Método que inicia os eventos dos componentes visuais
     */
    private void iniciarEventos() {
        
        //Adicionando o evento para o canvas quando o mouse for pressionado
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, (MouseEvent event) -> {
            gc.beginPath();
            gc.moveTo(event.getX(), event.getY());
            gc.stroke();
        });

        //Adicionando o evento para o canvas quando o mouse for arrastado
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, (MouseEvent event) -> {
            gc.lineTo(event.getX(), event.getY());
            gc.stroke();
        });

        //Adicionando a ação do botão btLimpar
        btLimpar.setOnAction((ActionEvent event) -> {
            //Limpa a região de desenho
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        });

        //Adicionando a ação do botão btIdentificar
        btIdentificar.setOnAction((ActionEvent event) -> {
            //Implementar
        });
    }

}
