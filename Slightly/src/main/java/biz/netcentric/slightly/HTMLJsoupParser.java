package biz.netcentric.slightly;

import org.jsoup.Jsoup;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Created by dev-camiloh on 2/21/17.
 */
public class HTMLJsoupParser {

    /**
     * Stores objects from javascript
     */
    private HashMap<String, Object> jsObjects;
    private ParserUtilities parserUtilities;

    /**
     * Constructor by default
     */
    public HTMLJsoupParser() {
        jsObjects = new HashMap<String, Object>();
        parserUtilities = new ParserUtilities();
    }

    /**
     * Parses HTML and Javascript code from a template
     * @return
     */
    public String doParse(String html){
        Document doc = null;
        try {
            doc = Jsoup.parse(html);
            parseJavascript(doc);
            parseIf(doc);
            parseForChild(doc);
            parseTextExpression(doc);
            parseAttributeExpression(doc);

        } catch (Exception e) {
            e.printStackTrace();
        }

        parserUtilities.printHTMLInConsole(doc);
        return doc.html();
    }

    /**
     * Parses javascript code using nashron from java 8
     * @param doc Document with code from template
     * @param url Page url to parse
     * @throws ScriptException
     * @throws FileNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private void parseJavascript(Document doc) throws ScriptException, FileNotFoundException, InstantiationException, IllegalAccessException{
        Elements elements = doc.getElementsByTag("script");
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine nashorn = manager.getEngineByName("nashorn");
        String js = "setUrl('"+url+"');";
        List<String> jsVars = new ArrayList<String>();

        for (Element element : elements) {
            if(element.attr("type").equals("server/javascript")){
                //loading js in order to use own method 'getParameterByName' from javascript code
                nashorn.eval(new FileReader(realPath.substring(0, realPath.lastIndexOf("/")+1) + "ntc.js"));
                nashorn.eval(js.toString());
                nashorn.eval(element.html());

                jsVars = parserUtilities.getJsVarNames(element.html());

                //Storing javascript variable values in jsObjects
                for (String jsVar : jsVars) {
                    if(nashorn.get(jsVar) != null){
                        Object jsObject = nashorn.get(jsVar).getClass().newInstance();
                        jsObject = nashorn.get(jsVar);
                        jsObjects.put(jsVar, jsObject);
                    }
                }
            }
            element.remove();
        }
    }

    /**
     * Parses tag with attribute 'data-if'
     * @param doc Document with code from template
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    private void parseIf(Document doc) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException{
        Elements elements = doc.select("[data-if]");

        for (Element element : elements) {
            String dataIfAttribute = element.attr("data-if");

            if(!Boolean.parseBoolean(parserUtilities.getExpressionValue(dataIfAttribute, jsObjects))){
                element.remove();
            }
        }
    }

    /**
     * Parses tags with data-for-child attribute, if data-for-child is false tag is deleted
     * @param doc Document with code from template
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    private void parseForChild(Document doc) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
        Elements elements = doc.select("[data-for-child]");
        List<String> result = null;

        for (Element element : elements) {

            String dataForChildAttribute = element.attr("data-for-child");
            Object[] attributeObject = parserUtilities.getAttributeObject(dataForChildAttribute, jsObjects);

            if(attributeObject[0] != null){
                Method method = attributeObject[0].getClass().getMethod("get"+attributeObject[1]);
                result = (List<String>)method.invoke(attributeObject[0]);

                if(result != null && !result.isEmpty()){
                    for(String child : result){
                        element.parent().append("<div>Child: "+child+"</div>");
                    }
                }
            }
            element.remove();
        }
    }

    /**
     * Parses the html text in order to show the values from java beans in text of tags
     * @param doc Document with code from template
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    private void parseTextExpression(Document doc) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
        //Select elements that contains expressions like ${}
        Elements elements = doc.select("*:matchesOwn(.*(\\$\\{).*(\\}).*)");
        StringBuilder finalText = new StringBuilder();
        String text = "";
        String result = "";

        for (Element element : elements) {
            text = element.text();
            String[] textArray = text.split("}");

            for(String expression: textArray){
                //objectAttribute is something like 'person.name'
                String objectAttribute = expression.substring(expression.indexOf("{")+1);

                //Replaces expression ${} by result from invoke set method
                result = parserUtilities.getExpressionValue(objectAttribute, jsObjects);
                expression = expression.replaceAll("(\\$\\{).*", result);

                finalText.append(expression);
            }
            element.text(finalText.toString());
            finalText.setLength(0);
        }
    }

    /**
     * Parses the attributes text in order to show the values from java beans in attributes of tags
     * @param doc Document with code from template
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    private void parseAttributeExpression(Document doc) throws SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException{
        Elements elements = doc.body().children().select("*");
        StringBuilder finalHtml = new StringBuilder();
        String htmlElement = "";
        String result = null;

        for (Element element : elements) {
            htmlElement = element.outerHtml();

            //Evaluate only html attributes like attr="whatever${person.spouse}" or attr="${person.spouse}"
            if(htmlElement.matches(".*=\"(.*\\$\\{).*(\\}).*")){

                String[] htmlElementArray = htmlElement.split(" ");

                for(String fragmentHtmlElement: htmlElementArray){

                    //After split is possible to get parts without some ${} expression
                    if(fragmentHtmlElement.indexOf("{") > -1){

                        String[] attributeToReplaceArray = fragmentHtmlElement.split("=");

                        if(attributeToReplaceArray != null && attributeToReplaceArray.length > 1){
                            String attributeToUpdate = attributeToReplaceArray[0];
                            //attributeToReplaceValue is something like 'person.name'
                            String attributeToReplaceValue = attributeToReplaceArray[1].substring(attributeToReplaceArray[1].indexOf("{")+1, attributeToReplaceArray[1].indexOf("}"));

                            result = parserUtilities.getExpressionValue(attributeToReplaceValue, jsObjects);
                            element.attr(attributeToUpdate, result);
                        }
                    }
                }
                finalHtml.setLength(0);
            }
        }
    }
}
