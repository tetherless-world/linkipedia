/**
 * Linkipedia, Copyright (c) 2015 Tetherless World Constellation 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */



import bioportal.BioPortalService;
import linkipedia.EsorService;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import utils.Attribute;
import utils.ConceptItem;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;


import java.io.File;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;



/**
 * Created by xixiluo on 3/16/15.
 */
public class testEntitySearchServlet {
    Document dom;
    DocumentBuilderFactory dbf;
    DocumentBuilder db;

    EsorService esorS;
    BioPortalService bioS;

    int total;
    int total_bio;
    int naTotal;
    int naTotal_bio;

    ArrayList<String> totalList;
    ArrayList<String> totalList_bio;

    HashMap<String, HashSet<String>> naTotalList;
    HashMap<String, HashSet<String>> naTotalList_bio;



    PrintStream ps;
    PrintStream ps_bio;

    //list of results
    List myAttribute;
    List myKeyword;
    HashSet<String> differentRes;

    //WritableWorkbook wworkbook;
    //WritableSheet wsheet;
    //int row;

    HashMap<String, List<List<String>>> excelData;

    public testEntitySearchServlet(){
        myAttribute = new ArrayList<String>();
        myKeyword = new ArrayList<String>();
        differentRes = new HashSet<String>();

        dbf = DocumentBuilderFactory.newInstance();
        esorS = new EsorService();
        bioS = new BioPortalService();
        total = 0;
        total_bio = 0;
        naTotal = 0;
        naTotal_bio = 0;
        totalList = new ArrayList<String>();
        totalList_bio = new ArrayList<String>();
        naTotalList = new HashMap<String, HashSet<String>>();
        naTotalList_bio = new HashMap<String, HashSet<String>>();

        excelData = new HashMap<String, List<List<String>>>();
        //row = 0;
    }

    public void run(){
        //parse the xml file and get the dom object
        //parseXmlFile();
        //parseKeyword();
        //parseAttribute();
    }

    private void parseXmlFile(String url){
        //get the factory

        try{
            //Using factory get an instance of document builder
            db = dbf.newDocumentBuilder();

            //parse using builder to get DOM representation of the XML file
            dom = db.parse(url);


        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (SAXException se) {
            se.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

    }

    private void parseKeyword() throws Exception{

        Element docEle = dom.getDocumentElement();
        NodeList nl = docEle.getElementsByTagName("keyword");

        if(nl != null && nl.getLength() > 0) {
            for(int i = 0 ; i < nl.getLength();i++) {

                //get the employee element
                Element el = (Element)nl.item(i);

                //System.out.println(el.getElementsByTagName("keyword"));
                String textVal = null;
                textVal = el.getFirstChild().getNodeValue();

                //System.out.println(textVal);
                //ps.println(textVal);


                annotateKeyword(textVal);


            }
        }
    }

    private void parseAttribute() throws Exception {
        Element docEle = dom.getDocumentElement();
        NodeList nl = docEle.getElementsByTagName("attribute");

        if(nl != null && nl.getLength() > 0) {
            for(int i = 0 ; i < nl.getLength();i++) {

                //get the employee element
                Element el = (Element)nl.item(i);

                //get the Employee object
                Attribute a = getAttribute(el);

                //add it to list
                myAttribute.add(a);

                annotateAttribute(a);

            }
        }
    }


    private Attribute getAttribute(Element attEl){
        //for each <attribute> element get text of id, name, and definition
        String name = getTextValue(attEl, "attributeName");
        String label = getTextValue(attEl, "attributeLabel");
        String definition = getTextValue(attEl, "attributeDefinition");

        String id = attEl.getAttribute("id");
        //System.out.println("id" + ":" + id);
        //System.out.println("attributeName" + ":" + name);
        //System.out.println("attributeLabel" + ":" + label);
        //System.out.println("attributeDefinition" + ":" + definition);


        //ps.println("name" + ":" + name);
        //ps.println("label" + ":" + label);

        Attribute a = new Attribute(id, name, label, definition);

        return a;
    }

    private String getTextValue(Element ele, String tagName) {
        String textVal = null;
        NodeList nl = ele.getElementsByTagName(tagName);
        if(nl != null && nl.getLength() > 0) {
            Element el = (Element)nl.item(0);
            textVal = el.getFirstChild().getNodeValue();
        }

        //System.out.println(tagName + ":" + textVal);
        //ps.println(tagName + ":" + textVal);
        return textVal;
    }



    private int getIntValue(Element ele, String tagName) {
        //in production application you would catch the exception
        return Integer.parseInt(getTextValue(ele,tagName));
    }

    private void annotateKeyword(String keyword) throws Exception {
        annotate(keyword);
    }


    private void old_annotateKeyword(String keyword) throws Exception {
        //keyword = replaceFromDashToSpace(keyword);
        ps.print(keyword);
        ps_bio.println(keyword);

        System.out.print(keyword);

        keyword = parseTerm(keyword);
        ps.print("|"+keyword);
        System.out.print("|"+keyword);

        String escapedKeyword;
        escapedKeyword = escapeToSpace(keyword);
        ps.print("|" + escapedKeyword);
        System.out.print("|" + escapedKeyword);

        String escapedToCommaKeyword = escapeToComma(keyword);
        ps.println("|" + escapedToCommaKeyword);
        System.out.println("|" + escapedToCommaKeyword);


        //escapedKeyword = escapeToComma(keyword);

        List<ConceptItem> res = esorS.getConcepts(escapedKeyword);


        if(res.size()==0){
            escapedKeyword = escapeToComma(keyword);
            res = esorS.getConcepts(escapedKeyword);
        }else{
            List<ConceptItem> resEscapeToComma = esorS.getConcepts(escapeToComma(keyword));
            compareResult(keyword, res, resEscapeToComma);

            /*if(res.size()!=resEscapeToComma.size()) {
                differentRes.add(keyword);
            }else{
                //
            }*/
        }



        //statistic analysis

        staticAnalysis(keyword, res);
        printResult(ps, res);

        List<ConceptItem> res_bio = bioS.getConcepts(keyword);
        staticAnalysis_bio(keyword, res_bio);
        printResult(ps_bio, res_bio);
    }

    private void compareResult(String term, List<ConceptItem> res_bySpace, List<ConceptItem> res_byComma){
        if(res_bySpace.size()!=0 && res_bySpace.size()!= res_byComma.size()){
            differentRes.add(term);
        }
    }


    private void staticAnalysis(String str, List<ConceptItem> res){
        total++;
        if(!totalList.contains(str)) totalList.add(str);

        if(res.size()==0){
            ps.println("NA");
            naTotal++;
            if(!naTotalList.containsKey(str)) {
                HashSet<String> set = new HashSet<String>();
                set.add(dom.getDocumentURI());
                naTotalList.put(str,set);
            }else{
                naTotalList.get(str).add(dom.getDocumentURI());
            }
        }
    }

    private void staticAnalysis_bio(String str, List<ConceptItem> res){
        total_bio++;
        if(!totalList_bio.contains(str)) totalList_bio.add(str);

        if(res.size()==0){
            ps_bio.println("NA");
            naTotal_bio++;
            if(!naTotalList_bio.containsKey(str)) {
                HashSet<String> set = new HashSet<String>();
                set.add(dom.getDocumentURI());
                naTotalList_bio.put(str,set);
            }else{
                naTotalList_bio.get(str).add(dom.getDocumentURI());
            }
        }
    }

    private void addDataToExcel(String str, List<ConceptItem> res_esor, List<ConceptItem> res_bio) {

        if(!excelData.containsKey(str)){

            List<List<String>> items = new ArrayList<List<String>>();


            int min = Math.min(res_esor.size(), res_bio.size());
            List<String> list_esor = new ArrayList<String>();
            List<String> list_bio = new ArrayList<String>();

            for(int i = 0; i < min; i++){

                String str_esor =  res_esor.get(i).getUri().toString();
                list_esor.add(str_esor);

                String str_bio = res_bio.get(i).getUri().toString();
                list_bio.add(str_bio);

                // new Label(2, row+1, res_esor.get(i).getUri().toString()+"\r");


            }

            items.add(list_bio);
            items.add(list_esor);

            excelData.put(str, items);
            //excelData.add(str, new ArrayList<>())
        }

        /*System.out.println("-------------------------"+str+"-------------------");

        //int row = wsheet.getRows();
        //int col = wsheet.getColumns();

        Label label = new Label(0, row, str);
        wsheet.addCell(label);

        int min = Math.min(res_esor.size(), res_bio.size());
        String str_esor = "";
        String str_bio = "";
        for(int i = 0; i < min; i++){
            str_esor = str_esor + res_esor.get(i).getUri().toString()+ "\n";
            str_bio = str_bio + res_bio.get(i).getUri().toString()+ "\n";
            // new Label(2, row+1, res_esor.get(i).getUri().toString()+"\r");
        }

        Label label_esor = new Label(1, row, str_esor);
        Label label_bio = new Label(2, row, str_bio);

        wsheet.addCell(label_esor);
        wsheet.addCell(label_bio);

        row++;
        */
    }

    private void generateExcelFile(){
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("bio vs esor");


        CreationHelper createHelper = workbook.getCreationHelper();

        //cell style for hyperlinks
        //by default hyperlinks are blue and underlined
        CellStyle hlink_style = workbook.createCellStyle();
        Font hlink_font = workbook.createFont();
        hlink_font.setUnderline(Font.U_SINGLE);
        hlink_font.setColor(IndexedColors.BLUE.getIndex());
        hlink_style.setFont(hlink_font);



        CellStyle cs = workbook.createCellStyle();
        cs.setWrapText(true);

        Set<String> keyset = excelData.keySet();
        int rownum = 0;
        for(String key:keyset){

            List<List<String>> items = excelData.get(key);

            List<String> list_bio = items.get(0);
            List<String> list_esor = items.get(1);

            int height = Math.max(list_bio.size(), list_esor.size());

            rownum++;
            Row row = sheet.createRow(rownum);
            //row.setHeightInPoints(4*sheet.getDefaultRowHeightInPoints());

            Cell cell_0 = row.createCell(0);
            cell_0.setCellValue(key);
            cell_0.setCellStyle(cs);
            sheet.addMergedRegion(new CellRangeAddress(rownum, rownum+height-1, 0, 0));

            sheet.addMergedRegion(new CellRangeAddress(rownum, rownum+height-1, 3, 3));
            sheet.addMergedRegion(new CellRangeAddress(rownum, rownum+height-1, 4, 4));



            for(int i = 0 ; i < height; i++){
                String str_bio = list_bio.get(i);
                String str_esor = list_esor.get(i);

                Cell cell_1 = row.createCell(1);
                cell_1.setCellValue(str_bio);
                Hyperlink link_bio = createHelper.createHyperlink(Hyperlink.LINK_URL);
                link_bio.setAddress(str_bio);
                cell_1.setHyperlink(link_bio);
                cell_1.setCellStyle(hlink_style);
                cell_1.setCellStyle(cs);


                Cell cell_2 = row.createCell(2);
                cell_2.setCellValue(str_esor);
                Hyperlink link_esor = createHelper.createHyperlink(Hyperlink.LINK_URL);
                link_esor.setAddress(str_esor);
                cell_2.setHyperlink(link_esor);
                cell_2.setCellStyle(hlink_style);
                cell_2.setCellStyle(cs);

                rownum++;
                row = sheet.createRow(rownum);
            }
        }

        try {
            FileOutputStream out =
                    new FileOutputStream(new File("output.xls"));
            workbook.write(out);
            out.close();
            System.out.println("Excel written successfully..");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private void annotate(String term)  throws Exception{

        ps.println(term);
        ps_bio.println(term);

        System.out.println(term);

        List<ConceptItem> res = esorS.getConcepts(term);
        List<ConceptItem> res_bio = bioS.getConcepts(term);


        staticAnalysis(term, res);
        staticAnalysis_bio(term, res_bio);

        printResult(ps, res);
        printResult(ps_bio, res_bio);

        if(res_bio.size()>0 && res.size()>0){
            addDataToExcel(term, res, res_bio);
        }
    }

    private void annotateAttribute(Attribute attr) throws Exception {
        annotate(attr.getName());

        if(attr.getLabel()!=null) {
            annotate(attr.getLabel());
        }
    }


    private void old_annotateAttribute(Attribute attr) throws Exception {

        String name = attr.getName();
        System.out.print(name);

        name = parseTerm(name);
        System.out.print("|" + name);

        String escapedNameBySpace = escapeToSpace(name);
        System.out.print("|" + escapedNameBySpace);

        List<ConceptItem> res_name_bySpace= esorS.getConcepts(escapedNameBySpace);

        String escapedNameByComma = escapeToComma(name);
        System.out.println("|" + escapedNameByComma);

        List<ConceptItem> res_name_byComma = esorS.getConcepts(escapedNameByComma);


        ps.println("nameBySpace" + ":" + escapedNameBySpace);
        printResult(ps, res_name_bySpace);
        staticAnalysis(escapedNameBySpace, res_name_bySpace);

        ps.println("nameByComma" + ":" + escapedNameByComma);
        printResult(ps, res_name_byComma);
        staticAnalysis(escapedNameByComma, res_name_byComma);

        List<ConceptItem> res_bio = bioS.getConcepts(name);
        staticAnalysis_bio(name, res_bio);
        ps_bio.println(name);
        printResult(ps_bio, res_bio);

        /*
        if(res_name_bySpace.size()!=res_name_byComma.size()){
            differentRes.add(name);
            System.out.println(escapedNameBySpace+ "---------" + escapedNameByComma);
        }*/
        compareResult(name, res_name_bySpace, res_name_byComma);


        if(attr.getLabel()!=null){
            String label = parseTerm(attr.getLabel());
            System.out.print(label);
            String escapedLabelBySpace = escapeToSpace(label);
            System.out.print("|" + escapedLabelBySpace);

            String escapedLabelByComma = escapeToComma(label);
            System.out.println("|" + escapedLabelByComma);



            if(!escapedLabelBySpace.equalsIgnoreCase(escapedNameBySpace)){
                List<ConceptItem> res_label_bySpace = esorS.getConcepts(escapedLabelBySpace);
                List<ConceptItem> res_label_byComma = esorS.getConcepts(escapedLabelByComma);

                ps.println("labelBySpace" + ":" + label);
                printResult(ps, res_label_bySpace);
                staticAnalysis(escapedLabelBySpace, res_label_bySpace);

                ps.println("labelByComma" + ":" + label);
                printResult(ps, res_label_byComma);
                staticAnalysis(escapedLabelByComma, res_label_byComma);

                List<ConceptItem> res_label_bio = bioS.getConcepts(label);
                staticAnalysis_bio(label, res_label_bio);
                ps_bio.println(label);
                printResult(ps_bio, res_label_bio);


                /*if(res_label_bySpace.size()!=res_label_byComma.size()){
                    differentRes.add(label);
                    System.out.println(escapedLabelBySpace+ "---------" + escapedLabelByComma);
                }*/
                compareResult(label, res_label_bySpace, res_label_byComma);
            }
        }
    }

    private void printResult(PrintStream ps, List<ConceptItem> res){
        for(int i = 0 ; i < res.size(); i++){
            ConceptItem c = res.get(i);

            //System.out.println(c.getUri());
            ps.println(c.getUri());

            //System.out.println(c.getWeight());
            ps.println(c.getWeight());
        }
    }

    private String parseTerm(String str) throws Exception{

        if(str.contains("(")){
            str =  str.substring(0, str.indexOf("("));
        }

        str = str.replaceAll("\\s+$", "");

        str = replaceFromSlashToSpace(replaceFromDotToSpace(replaceFromDashToSpace(str)));
        str = str.replace("%", " percent");
        str = insertSpaceBeforeCapital(str);
        str = URLEncoder.encode(str, "UTF-8").replaceAll("\\+", "%20");
        return str;
    }

    private String replaceFromDotToSpace(String str) {
        return str.replace(".", " ");
    }

    private String replaceFromSlashToSpace(String str){
        return str.replace("/", " ");
    }

    private String insertSpaceBeforeCapital(String str){
        char[] charArr = str.toCharArray();
        StringBuilder sb = new StringBuilder();
        for(int i = 0 ; i < charArr.length; i++){
            if(i>0 && charArr[i] >= 'A' && charArr[i] <= 'Z' && charArr[i-1] >= 'a'&& charArr[i-1] <= 'z')
                sb.append(" ");
            sb.append(charArr[i]);
        }
        return sb.toString();
    }

    private String replaceFromDashToSpace(String original){
        return original.replace("_", " ");
    }

    private String escapeToSpace(String original){
        return original.replace(" ", "%20");
    }

    private String escapeToComma(String original){
        return original.replace("%20", ",");
    }

    private void setOutputPath(File file) throws FileNotFoundException{
        ps = new PrintStream(new FileOutputStream(file));

    }

    private void setBioOutputPath(File file) throws FileNotFoundException{
        ps_bio = new PrintStream(new FileOutputStream(file));

    }

    private void printStatData() {

        System.out.println("NA keywords list:");

        /*
        for(int i = 0; i < naTotalList.size(); i++){
            System.out.println(naTotalList.get(i));
        }*/

        for(String key:naTotalList.keySet()){
            System.out.println(key);
            HashSet<String> set = naTotalList.get(key);
            for(String url:set){
                System.out.println(url);
            }
            System.out.println();
        }

        System.out.println("total:" + total);
        System.out.println("naTotal:"+ naTotal);

        System.out.println("distinct total:" + totalList.size());
        System.out.println("distinct NA total:" + naTotalList.size());

        System.out.println("keywords has different result:");

        for(String str:differentRes) {
            System.out.println(str);
        }
    }

    private void printStatData_bio() {

        System.out.println("NA keywords list:");

        /*
        for(int i = 0; i < naTotalList.size(); i++){
            System.out.println(naTotalList.get(i));
        }*/

        for(String key:naTotalList_bio.keySet()){
            System.out.println(key);
            HashSet<String> set = naTotalList_bio.get(key);
            for(String url:set){
                System.out.println(url);
            }
            System.out.println();
        }

        System.out.println("total_bio:" + total_bio);
        System.out.println("naTotal_bio:"+ naTotal_bio);

        System.out.println("distinct total_bio:" + totalList_bio.size());
        System.out.println("distinct NA total_bio:" + naTotalList_bio.size());

        /*
        System.out.println("keywords has different result:");

        for(String str:differentRes) {
            System.out.println(str);
        }
        */
    }


    public static void main (String [] args) throws Exception {

        testEntitySearchServlet test = new testEntitySearchServlet();


        //read xml url from test_corpus_B.txt
        String fileName = "test_corpus_C.txt";
        File dir = new File(".");
        File fin = new File(dir.getCanonicalPath() + File.separator + fileName);

        File output = new File(dir.getCanonicalPath() + File.separator + fileName.substring(0, fileName.indexOf("."))+"_output.txt");
        File output_bio = new File(dir.getCanonicalPath() + File.separator + fileName.substring(0, fileName.indexOf("."))+"_bio_output.txt");

        test.setOutputPath(output);

        test.setBioOutputPath(output_bio);


        BufferedReader br = new BufferedReader(new FileReader(fin));

        //test.wworkbook = Workbook.createWorkbook(new File("output.xls"));
        //test.wsheet = test.wworkbook.createSheet("First Sheet", 0);


        String line = null;

        try {
            //get the factory
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            //Using factory get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();

            //parse using builder to get DOM representation of the XML filea

            while ((line = br.readLine()) != null) {
                System.out.println(line);
                test.ps.println(line);

                test.parseXmlFile(line);
                test.parseKeyword();
                test.parseAttribute();
                //System.out.println("-----------------------------");
                test.ps.println("-----------------------------");
            };
            test.printStatData();
            test.printStatData_bio();

        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }


        br.close();
        test.generateExcelFile();
        //test.wworkbook.write();
        //test.wworkbook.close();

    }
}
