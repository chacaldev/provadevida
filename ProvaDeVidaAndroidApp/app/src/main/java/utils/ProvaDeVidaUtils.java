package utils;

/**
 * Created by josen on 25/10/2017.
 */
public class ProvaDeVidaUtils {

    public static String cpfToString(Long cpf){
        String retorno = cpf.toString();
        while(retorno.length() < 11){
            retorno = "0" + retorno;
        }
        return retorno;
    }
}
