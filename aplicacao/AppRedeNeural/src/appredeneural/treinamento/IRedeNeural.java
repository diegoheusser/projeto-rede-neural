package appredeneural.treinamento;

/**
 *
 * @author Diego Heusser
 */
public interface IRedeNeural {
    
    public int treinamento();
    
    public double[] predicao(double[] dados);
    
    public void close();
    
}
