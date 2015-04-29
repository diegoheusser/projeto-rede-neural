package appredeneural.controle;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javax.imageio.ImageIO;

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
            salvar();
        });
    }

    private void salvar() {
        FileChooser fileChooser = new FileChooser();

        //Set extension filter
        FileChooser.ExtensionFilter extFilter
                = new FileChooser.ExtensionFilter("png files (*.png)", "*.png");
        fileChooser.getExtensionFilters().add(extFilter);

        //Show save file dialog
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try {
                int width = (int)canvas.getWidth();
                int height = (int) canvas.getHeight();
                WritableImage writableImage = new WritableImage(width,height);
                canvas.snapshot(null, writableImage);
                RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
                ImageIO.write(renderedImage, "png", file);
            } catch (IOException ex) {
            }
        }
    }
}


