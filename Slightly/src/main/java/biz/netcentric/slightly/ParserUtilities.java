package biz.netcentric.slightly;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jsoup.nodes.Document;

/**
 * Created by dev-camiloh on 2/21/17.
 */
public class ParserUtilities {

    /**
     * Gets variable names from javascript
     * @param html
     * @return List with variable names from javascript
     */
    public List<String> getJsVarNames(String html) {
        List<String> jsVars = new ArrayList<String>();
        String[] jsVarsArray = html.split(";");

        for (String jsVar : jsVarsArray) {
            //Variables that contain import java classes are not consider
            if(!jsVar.contains("Java.type")){
                jsVar = jsVar.substring(jsVar.indexOf("var")+3, jsVar.indexOf("=")).trim();
                jsVars.add(jsVar);
            }
        }

        return jsVars;
    }

    /**
     * Get value from indicated javascript attribute using reflection in order to get values from java beans
     * @param dataAttribute Value from expression like 'object.attribute'
     * @return Value from java bean indicate by 'object.attribute'
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    public String getExpressionValue(String dataAttribute, HashMap<String, Object> jsObjects)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        String result = "";
        Object[] attributeObject = getAttributeObject(dataAttribute, jsObjects);
        Object jsObject = attributeObject[0];
        String attribute = attributeObject[1].toString();

        if(jsObject != null){
            try{
                Method method = jsObject.getClass().getMethod("get"+attribute);
                result = (String) method.invoke(jsObject);
            }catch(NoSuchMethodException ex){
                Method method = jsObject.getClass().getMethod("is"+attribute);
                result = method.invoke(jsObject).toString();
            }
        }
        return result;
    }

    /**
     * Splits object and attribute
     * @param dataAttribute
     * @return Array storing object and name of variable that store the object
     */
    public Object[] getAttributeObject(String dataAttribute, HashMap<String, Object> jsObjects) {
        String[] dataExpression = dataAttribute.split("\\.");
        Object jsObject = jsObjects.get(dataExpression[0]);
        String attribute = (dataExpression[1] != null)?dataExpression[1].substring(0, 1).toUpperCase() + dataExpression[1].substring(1) : "";
        Object[] attributeObject = {jsObject, attribute};

        return attributeObject;
    }

    /**
     * Method in order to show the final html in console
     * @param doc Document with code from template
     */
    public void printHTMLInConsole(Document doc){
        System.out.println("------------- HTML --------------");
        System.out.println(doc.html());
        System.out.println("---------------------------");
    }
}
