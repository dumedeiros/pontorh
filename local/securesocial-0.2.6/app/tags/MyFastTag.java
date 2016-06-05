package tags;

import groovy.lang.Closure;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Validation;
import play.mvc.Scope;
import play.templates.FastTags;
import play.templates.GroovyTemplate;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;


//OBS Eduardo Medeiros

/**
 * Tag Criada para usar ao inves da tag field padrao do play que usa por padrao
 * o ${field.errorClass} -> "hasError" substituindo-a por "error"
 * Pois o twitter bootstrap utiliza a class "error" para ressaltar erros.
 */

@FastTags.Namespace(value = "mytags")
public class MyFastTag extends FastTags {
    /**
     * The field tag is a helper, based on the spirit of Don't Repeat Yourself.
     *
     * @param args     tag attributes
     * @param body     tag inner body
     * @param out      the output writer
     * @param template enclosing template
     * @param fromLine template line number where the tag is defined
     * @author Eduardo Medeiros
     */
    public static void _field(Map<?, ?> args, Closure body, PrintWriter out, GroovyTemplate.ExecutableTemplate template, int fromLine) {

        Map<String, Object> field = new HashMap<String, Object>();
        String _arg = args.get("arg").toString();
        field.put("name", _arg);
        field.put("id", _arg.replace('.', '_'));
        field.put("flash", Scope.Flash.current().get(_arg));
        field.put("flashArray", field.get("flash") != null && !StringUtils.isEmpty(field.get("flash").toString()) ? field.get("flash")
                .toString().split(",") : new String[0]);
        field.put("error", Validation.error(_arg));
        field.put("errorClass", field.get("error") != null ? "error" : "");
        String[] pieces = _arg.split("\\.");
        Object obj = body.getProperty(pieces[0]);
        if (obj != null) {
            if (pieces.length > 1) {
                try {
                    String path = _arg.substring(_arg.indexOf(".") + 1);
                    Object value = PropertyUtils.getProperty(obj, path);
                    field.put("value", value);
                } catch (Exception e) {
                    // if there is a problem reading the field we dont set any
                    // value
                }
            } else {
                field.put("value", obj);
            }
        }
        body.setProperty("field", field);
        body.call();
    }

    /**
     * Discarta o flash para quando se usa render num erro, ao inves de redirecionamento
     * (Pq existem duas formas de redirecionamento quando há erro:
     * 1 -> render(@page) 2 -> chamar método que possua render, tipo index()
     * Na view utilizar
     * <p/>
     * #{mytags.discardFlash /}
     *
     * @param args
     * @param body
     * @param out
     * @param template
     * @param fromLine
     */
    public static void _discardFlash(Map<?, ?> args, Closure body, PrintWriter out, GroovyTemplate.ExecutableTemplate template, int fromLine) {
        Scope.Flash.current().discard();
//        Scope.Flash.current().discard("error");
    }
}
