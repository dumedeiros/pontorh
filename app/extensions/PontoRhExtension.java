package extensions;

import play.templates.JavaExtensions;

public class PontoRhExtension extends JavaExtensions {


    //OBS: Suporte a Internalization, como ficaria?
    public static String sexualize(Integer sex) {
        switch (sex) {
            case 0:
                return "";
            case 1:
                return "Masculino";
            case 2:
                return "Feminino";
            default:
                return "";

        }
    }

}