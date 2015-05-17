package appredeneural.controle;

import appredeneural.treinamento.IRedeNeural;
import appredeneural.treinamento.RedeNeuralOpenCv;
import java.awt.image.RenderedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javax.imageio.ImageIO;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

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
    private double[] inputs;
    private String name;

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        gc = canvas.getGraphicsContext2D();
        gc.setLineWidth(6);
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
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

    private void writeTrainFile(String content) throws IOException {
        File arquivoTreinamento = new File("arquivoTreinamento.txt");
        FileWriter fw = new FileWriter(arquivoTreinamento, true);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(content);
        bw.close();
        fw.close();
    }

    private void salvar() {

        File file = new File("imagemDesenhada.png");

        if (file != null) {
            try {
                int width = (int) canvas.getWidth();
                int height = (int) canvas.getHeight();
                WritableImage writableImage = new WritableImage(width, height);
                canvas.snapshot(null, writableImage);
                RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
                ImageIO.write(renderedImage, "png", file);
            } catch (IOException ex) {
            }
        }
        //Pegando o caminho da imagem
        String caminhoImagem = file.getPath();

        Mat img = Highgui.imread(caminhoImagem);
        Mat imgFiltrada = new Mat();
        Mat imgCinza = new Mat();
        Mat imgSegmentada = new Mat();
        Imgproc.blur(img, imgFiltrada, new Size(1, 1));
        Imgproc.cvtColor(imgFiltrada, imgCinza, Imgproc.COLOR_RGB2GRAY);
        Imgproc.threshold(imgCinza, imgSegmentada, 10, 245, Imgproc.THRESH_BINARY_INV);
        Highgui.imwrite("imagemSegmentada.jpg", imgSegmentada);
        java.util.List<MatOfPoint> contornos = new ArrayList<>();
        Imgproc.findContours(imgSegmentada, contornos, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));
        List<Moments> m = new ArrayList<>();
        for (int i = 0; i < contornos.size(); i++) {
            m.add(Imgproc.moments(contornos.get(i)));
        }
        List<MatOfDouble> momentosHu = new ArrayList<>();
        for (int i = 0; i < m.size(); i++) {
            MatOfDouble mHu = new MatOfDouble();
            Imgproc.HuMoments(m.get(i), mHu);
            momentosHu.add(mHu);
        }
        double[] mMedia = new double[7];
        for (int i = 0; i < momentosHu.size(); i++) {
            double[] moms = momentosHu.get(i).toArray();
            for (int j = 0; j < moms.length; j++) {

                mMedia[j] += moms[j];
            }
        }
        for (int i = 0; i < mMedia.length; i++) {
            mMedia[i] = mMedia[i] / momentosHu.size();
        }
        this.inputs = mMedia;

        IRedeNeural redeNeural = RedeNeuralOpenCv.getInstancia();
        int it = redeNeural.treinamento();
        double[] resposta = redeNeural.predicao(mMedia);

        int maior = 0;
        for (int i = 0; i < resposta.length; i++) {
            if (resposta[i] > resposta[maior]) {
                maior = i;
            }
        }

        switch (maior) {
            case 0: {
                this.name = "Circulo";
                break;
            }
            case 1: {
                this.name = "Quadrado";
                break;
            }
            case 2: {
                this.name = "Retangulo";
                break;
            }
            case 3: {
                this.name = "Triângulo";
                break;
            }
            case 4: {
                this.name = "Hexagono";
                break;
            }
        }

        DecimalFormat df = new DecimalFormat("#,00");
        String percentualCirculo = df.format(resposta[0] * 100);
        String percentualQuadrado = df.format(resposta[1] * 100);
        String percentualRetangulo = df.format(resposta[2] * 100);
        String percentualTriangulo = df.format(resposta[3] * 100);
        String percentualHexagono = df.format(resposta[4] * 100);

        Alert dialogoExe = new Alert(Alert.AlertType.CONFIRMATION);
        ButtonType btnSim = new ButtonType("Sim");
        ButtonType btnNao = new ButtonType("Não");

        dialogoExe.setTitle("Resposta");
        dialogoExe.setHeaderText("Acho que é um " + name);
        dialogoExe.setContentText("Circulo: " + percentualCirculo + "%\n"
                + "Quadrado: " + percentualQuadrado + "%\n"
                + "Retângulo: " + percentualRetangulo + "%\n"
                + "Triângulo: " + percentualTriangulo + "%\n"
                + "Hexagono: " + percentualHexagono + "%\n\n"
                + "Está certo?");
        dialogoExe.getButtonTypes().setAll(btnSim, btnNao);
        dialogoExe.showAndWait().ifPresent(b -> {
            if (b == btnSim) {
                saveNewTrainTXT();
            } else if (b == btnNao) {
                teach();
            }
        });
        redeNeural.close();
        File redeNeuralTXT = new File("rede_neural.txt");
        redeNeuralTXT.delete();
    }

    private String nameToOutput(String name) {
        switch (name) {
            case "Circulo": {
                return "1;0;0;0;0";
            }
            case "Quadrado": {
                return "0;1;0;0;0";
            }
            case "Retangulo": {
                return "0;0;1;0;0";
            }
            case "Triângulo": {
                return "0;0;0;1;0";
            }
            case "Hexagono": {
                return "0;0;0;0;1";
            }
        }
        return "";
    }

    private void teach() {
        this.name = ask();
        this.saveNewTrainTXT();
    }

    private String ask() {
        List<String> formasGeometricas = new ArrayList<>();
        formasGeometricas.add("Circulo");
        formasGeometricas.add("Quadrado");
        formasGeometricas.add("Retangulo");
        formasGeometricas.add("Triângulo");
        formasGeometricas.add("Hexagono");

        ChoiceDialog<String> dialog = new ChoiceDialog<>("Circulo", formasGeometricas);
        dialog.setTitle("Responda");
        dialog.setHeaderText("Que tipo de forma geométrica é?");
        dialog.setContentText("Selecione:");

        Optional<String> result = dialog.showAndWait();

        String option = "";

        if (result.isPresent()) {
            option = result.get();
        }
        return option;
    }

    private void saveNewTrainTXT() {
        String record = "";
        for (int i = 0; i < this.inputs.length; i++) {
            record += String.valueOf(this.inputs[i]) + ";";
        }
        String newLine = System.getProperty("line.separator");
        record += this.nameToOutput(this.name);
        try {
            this.writeTrainFile(record + newLine);
        } catch (IOException ex) {
            Logger.getLogger(TelaPrincipal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
