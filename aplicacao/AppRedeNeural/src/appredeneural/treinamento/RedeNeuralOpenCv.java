package appredeneural.treinamento;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.TermCriteria;
import org.opencv.ml.CvANN_MLP;
import org.opencv.ml.CvANN_MLP_TrainParams;

/**
 *
 * @author Diego Heusser
 */
public class RedeNeuralOpenCv implements IRedeNeural {

    private int entradasRede;
    private int saidasRede;
    private double pesoInicial;
    private Mat camadas;
    private CvANN_MLP mlp;
    private static RedeNeuralOpenCv instancia;

    private RedeNeuralOpenCv() {
        this.entradasRede = 7;
        this.saidasRede = 5;
        this.pesoInicial = 0.8;
        this.camadas = new Mat(3, 1, CvType.CV_32SC1);
        this.camadas.put(0, 0, entradasRede);
        this.camadas.put(1, 0, 20);
        this.camadas.put(2, 0, saidasRede);
        this.mlp = new CvANN_MLP(this.camadas, CvANN_MLP.SIGMOID_SYM, 1, 1);
    }

    public static RedeNeuralOpenCv getInstancia() {
        if (RedeNeuralOpenCv.instancia == null) {
            RedeNeuralOpenCv.instancia = new RedeNeuralOpenCv();
            String caminho = "rede_neural.txt";
            if (new File(caminho).exists()) {
                RedeNeuralOpenCv.instancia.mlp.load(caminho);
            }
        }
        return RedeNeuralOpenCv.instancia;
    }
    
    @Override
    public void close(){
        RedeNeuralOpenCv.instancia = null;
    }

    @Override
    public int treinamento() {
        double[][] dados = this.carregaDadosArquivoTreinamento();
        Mat entradasTreinamento = new Mat(this.getNumeroAmostrasTreinamento(), this.entradasRede, CvType.CV_32FC1);
        Mat saidasEsperadas = new Mat(this.getNumeroAmostrasTreinamento(), this.saidasRede, CvType.CV_32FC1);
        Mat pesos = new Mat(this.getNumeroAmostrasTreinamento(), 1, CvType.CV_32FC1);
        for (int i = 0; i < this.getNumeroAmostrasTreinamento(); i++) {
            entradasTreinamento.put(i, 0, dados[i][0]);
            entradasTreinamento.put(i, 1, dados[i][1]);
            entradasTreinamento.put(i, 2, dados[i][2]);
            entradasTreinamento.put(i, 3, dados[i][3]);
            entradasTreinamento.put(i, 4, dados[i][4]);
            entradasTreinamento.put(i, 5, dados[i][5]);
            entradasTreinamento.put(i, 6, dados[i][6]);

            saidasEsperadas.put(i, 0, dados[i][7]);
            saidasEsperadas.put(i, 1, dados[i][8]);
            saidasEsperadas.put(i, 2, dados[i][9]);
            saidasEsperadas.put(i, 3, dados[i][10]);
            saidasEsperadas.put(i, 4, dados[i][11]);
            pesos.put(i, 0, this.pesoInicial);
        }
        CvANN_MLP_TrainParams parametrosTreinamento = new CvANN_MLP_TrainParams();
        parametrosTreinamento.set_train_method(CvANN_MLP_TrainParams.BACKPROP);
        TermCriteria termC = new TermCriteria(TermCriteria.EPS + TermCriteria.COUNT, 10000, 0.01);
        parametrosTreinamento.set_term_crit(termC);
        int interacoes = this.mlp.train(entradasTreinamento, saidasEsperadas, pesos, new Mat(), parametrosTreinamento, 0);
        this.salvar("rede_neural.txt");
        return interacoes;
    }

    private void salvar(String caminho) {
        this.mlp.save(caminho);
    }

    private int getNumeroAmostrasTreinamento() {
        String arquivo = "arquivoTreinamento.txt";
        File arquivoTreinamento = new File(arquivo);
        LineNumberReader leituraLinha = null;
        try {
            leituraLinha = new LineNumberReader(new FileReader(arquivoTreinamento));
            leituraLinha.skip(arquivoTreinamento.length());
        } catch (Exception e) {
        }
        return leituraLinha.getLineNumber() + 1;
    }

    private double[][] carregaDadosArquivoTreinamento() {
        String arquivo = "arquivoTreinamento.txt";
        double[][] dadosTreinamento = null;
        FileReader reader = null;
        try {
            dadosTreinamento = new double[this.getNumeroAmostrasTreinamento()][this.entradasRede + this.saidasRede];
            reader = new FileReader(arquivo);
        } catch (Exception e) {
        }
        BufferedReader leitor = new BufferedReader(reader);
        String linha = "";
        int cont = 0;
        while (true) {
            try {
                linha = leitor.readLine();
            } catch (Exception e) {
            }
            if (linha == null) {
                break;
            } else {
                String[] array = linha.split(";");
                for (int i = 0; i < array.length; i++) {
                    dadosTreinamento[cont][i] = Double.parseDouble(array[i]);
                }
                cont++;
            }
        }
        return dadosTreinamento;
    }

    @Override
    public double[] predicao(double[] dados) {

        Mat entradas = new Mat(1, this.entradasRede, CvType.CV_32FC1);
        Mat saida = new Mat(1, this.saidasRede, CvType.CV_32FC1);

        for (int i = 0; i < dados.length; i++) {
            entradas.put(0, i, dados[i]);
        }

        this.mlp.predict(entradas, saida);

        double[] circulo = saida.get(0, 0);
        double[] quadrado = saida.get(0, 1);
        double[] retangulo = saida.get(0, 2);
        double[] triangulo = saida.get(0, 3);
        double[] hexagono = saida.get(0, 4);

        double[] resposta = new double[5];

        resposta[0] = circulo[0];
        resposta[1] = quadrado[0];
        resposta[2] = retangulo[0];
        resposta[3] = triangulo[0];
        resposta[4] = hexagono[0];
        
        return resposta;
    }
}
