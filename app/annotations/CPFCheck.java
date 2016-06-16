package annotations;

import net.sf.oval.Validator;
import net.sf.oval.configuration.annotation.AbstractAnnotationCheck;
import net.sf.oval.context.OValContext;
import net.sf.oval.exception.OValException;
import org.apache.commons.lang.StringUtils;

public class CPFCheck extends AbstractAnnotationCheck<Cpf> {

    /**
     * Error Message Key
     **/

    private static final String CPF_PARTTERN = "(\\d{3}.?\\d{3}.?\\d{3}-?\\d{2})";
    public final static String message = "validation.cpf";

    @Override
    public void configure(Cpf cpf) {
        setMessage(cpf.message());
    }

    @Override
    public boolean isSatisfied(Object validatedObject, Object value, OValContext context, Validator validator) throws OValException {
        return isValidCPF(value.toString());
    }


    /**
     * Cálculo do dígito verificador
     * (Tanto o primeiro quanto o segundo, depedendo do tamanho do string passado
     *
     * @param cpf
     * @return
     */
    private static int calcDigit(String cpf) {
        int sum = 0;
        for (int i = 0, p = cpf.length() == 9 ? 10 : 11; i < cpf.length(); i++, p--) {
            sum += Integer.parseInt(cpf.charAt(i) + "") * p;
        }
        sum = 11 - sum % 11;
        return sum > 9 ? 0 : sum;
    }

    /**
     * Remove os caracteres que não forem números
     *
     * @param cpf
     * @return
     */
    private static String removeChars(String cpf) {
        String a = cpf.replace("-", "");
        a = a.replace(".", "");
        return a;
    }

    /**
     * Verificar se os dígitos são todos iguais
     * Há casos em que o 00000000000 é válido, outros não.
     * A _obs no código indica como fazer isso.
     *
     * @param cpf
     * @return
     */
    private static boolean digitsAreEquals(String cpf) {
        char firstDig = cpf.charAt(0);
//        char primDig = '0'; // Change for the line above to accept the 00000000000 which is really valid
        char[] charCpf = cpf.toCharArray();
        for (char c : charCpf) {
            if (c != firstDig) {
                return false;
            }
        }
        return true;
    }

    /**
     * Verifica se um CPF é válido
     * -> Cpf null é válido, para isso utiliza-se @Required ou @NotNull
     *
     * @param cpf
     * @return
     */
    public static boolean isValidCPF(String cpf) {


        if (StringUtils.isEmpty(cpf)) {
            return true;
        } else {
            if (((!cpf.matches(CPF_PARTTERN)) || digitsAreEquals(cpf))) {
                return false;
            }
        }
        cpf = removeChars(cpf);
        Integer firstDigit = calcDigit(cpf.substring(0, 9));
        Integer secondDigit = calcDigit(cpf.substring(0, 9) + firstDigit);
        return cpf.equals(cpf.substring(0, 9) + firstDigit.toString() + secondDigit.toString());
    }

}
