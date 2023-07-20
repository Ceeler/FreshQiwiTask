import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


public class CurrencyChecker {

    public static void main(String[] args) throws IOException, InterruptedException, ParserConfigurationException, SAXException {

        //Проверяем что количество аргументов
        if (args.length != 2) {
            System.out.println("Не верный набор параметров");
            return;
        }
        //Берем необходимые параметры и проверяем на корректность
        String charCode = args[0].split("=")[1].toUpperCase();

        String date = args[1].split("=")[1];

        if (charCode.length() != 3) {
            System.out.println("Не верный формат кода валюты");
            return;
        }

        //Создаём клиент и отправляем запрос
        HttpClient client = HttpClient.newHttpClient();

        HttpResponse<String> response = getXmlCurrenciesData(client, date);

        //Проверяем запрос
        if (response == null) {
            return;
        }
        //Пытаемся найти нужную валюту, если не получится возвращаем ошибку
        if (parseCurrencies(response, charCode) == false) {
            System.out.println("Не верные параметры");
        }

    }

    public static HttpResponse<String> getXmlCurrenciesData(HttpClient client, String date) throws IOException, InterruptedException {

        //Проверяем дату в нужном формате, и разбиваем её по "-"
        String[] dateArray = date.split("-");
        if (dateArray.length != 3) {
            System.out.println("Не верный формат даты");
            return null;
        }

        //Строим запрос на нужную дату
        HttpRequest request = HttpRequest.newBuilder(
                        URI.create("http://www.cbr.ru/scripts/XML_daily.asp?date_req=" + dateArray[2] + "/" + dateArray[1] + "/" + dateArray[0]))
                .build();

        // Отправляем запрос и возвращаем его
        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        return response;
    }

    public static boolean parseCurrencies(HttpResponse<String> response, String charCode) throws ParserConfigurationException, IOException, SAXException {

        //Создаем объекты для обработки XML запроса
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        DocumentBuilder builder = factory.newDocumentBuilder();

        Document document = builder.parse(new InputSource(new StringReader(response.body())));

        //Получем список всех элементов в XML
        NodeList nodeList = document.getDocumentElement().getChildNodes();

        for (int i = 0; i < nodeList.getLength(); i++) {

            Node node = nodeList.item(i);

            //Получем родительский элемент и записываем его ID
            if (node instanceof Element) {
                Valute valute = new Valute();

                valute.id = node.getAttributes().getNamedItem("ID").getNodeValue();

                NodeList childNodes = node.getChildNodes();

                for (int j = 0; j < childNodes.getLength(); j++) {
                    Node cNode = childNodes.item(j);

                    //Получем дочерниие элементы Valute и записываем необходимые
                    if (cNode instanceof Element) {
                        String content = cNode.getLastChild().
                                getTextContent().trim();
                        switch (cNode.getNodeName()) {
                            case "Name":
                                valute.name = content;
                                break;
                            case "Nominal":
                                valute.nominal = Integer.parseInt(content);
                                break;
                            case "CharCode":
                                valute.charCode = content;
                                break;
                            case "Value":
                                valute.value = Double.parseDouble(content.replace(',', '.'));
                        }
                    }
                }
                //Проверяем код, если совпадает то печатаем и выходим
                if (valute.charCode.equals(charCode)) {
                    System.out.println(valute);
                    return true;
                }

            }

        }
        return false;
    }


}
