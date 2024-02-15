import javax.swing.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;
import org.apache.commons.compress.utils.IOUtils;

public class ShortDemoReport {

    private static InputStream candara;
    private static InputStream candarabold;

    private static InputStream lato;
    private static InputStream latobold;
    private static InputStream latoblack;
    private static BaseFont CANDARA_REGULAR;
    private static BaseFont LATO_BOLD;
    private static String datadir = "";
    private static String[] filesindatadir = new String[0];
    private static String indesignfile = "";
    private static LinkedHashMap<String, LinkedHashMap<String,String>> data = new LinkedHashMap<>();
    private static File outputFolderfile = new File("");
    private static boolean isDemo = true;

    public static void main(String[] args) throws Exception {
        if(isDemo){
            datadir = "E:/PCC/R Scripts/Vaginal_Microbiome/Output/PCC_Vaginal_2024-02-15-13-22-32"; // Do not change this as it is specific to demo report.
        }else{
            datadir = "E:/PCC/R Scripts/Vaginal_Microbiome/Output/PCC_Vaginal_2024-02-15-13-22-32";
        }
        //datadir = args[0];
        filesindatadir = new File(datadir).list();

        for(int i=0;i<filesindatadir.length;i++){
            if(filesindatadir[i].startsWith("InDesignInput_English_Vaginal")){
                indesignfile = filesindatadir[i];
                break;
            }
        }

        if(indesignfile.equals("")){
            JOptionPane.showMessageDialog(null,"The input data file cannot be found in the input folder path you have provided.");
            System.exit(1);
        }

        data = readResultsData();
        ArrayList<String> barcodes = new ArrayList<>(data.keySet());

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yy:MM:dd:HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String date = dtf.format(now);
        String[] dates = date.split(":");
        String outputcode = dates[0] +"-"+ dates[1] +"-"+ dates[2] +"-"+ dates[3] +"-"+ dates[4] +"-"+ dates[5];
        String outputFolder = "Vaginal_Reports_English_"+outputcode;
        String outputPath = "E:/PCC/Report Outputs/Vaginal_Reports/"+outputFolder;
        //String outputPath = args[1]+"/"+outputFolder;
        outputFolderfile = new File(outputPath);
        outputFolderfile.mkdir();

        for(int i=0;i<barcodes.size();i++) {
            generateReport(data.get(barcodes.get(i)));
        }
    }

    public static void generateReport(LinkedHashMap<String,String> dat) throws Exception{
        String barcode = dat.get("1.1_Text_Barcode");
        String coldate = dat.get("1.1_Date_samplearrivaldate");

        if(isDemo){
            barcode = "vaNGSXX";
            coldate = "DD.MM.YYYY";
        }

        ArrayList<String> keys = new ArrayList<>(dat.keySet());

        candara = new Main().getClass().getResourceAsStream("CenturyGothicPaneuropeanRegular.ttf");
        candarabold = new Main().getClass().getResourceAsStream("CenturyGothicPaneuropeanBold.ttf");

        lato = new Main().getClass().getResourceAsStream("Lato-Regular.ttf");
        latobold = new Main().getClass().getResourceAsStream("Lato-Bold.ttf");
        latoblack = new Main().getClass().getResourceAsStream("Lato-Black.ttf");

        File out = new File(outputFolderfile.getAbsolutePath()+"/"+barcode+"_English"+ (isDemo ? "_short_demo":"") +".pdf");

        com.itextpdf.text.Document document = new com.itextpdf.text.Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(out));
        document.open();
        PdfContentByte cb = writer.getDirectContent();
        byte[] bytes = IOUtils.toByteArray(candara);
        CANDARA_REGULAR = BaseFont.createFont("CenturyGothicPaneuropeanRegular.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, bytes, null);

        byte[] bytes2 = IOUtils.toByteArray(candarabold);
        BaseFont CANDARA_BOLD = BaseFont.createFont("CenturyGothicPaneuropeanBold.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, bytes2, null);

        byte[] bytes3 = IOUtils.toByteArray(lato);
        BaseFont LATO_REGULAR = BaseFont.createFont("Lato-Regular.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, bytes3, null);

        byte[] bytes4 = IOUtils.toByteArray(latobold);
        LATO_BOLD = BaseFont.createFont("Lato-Bold.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, bytes4, null);

        byte[] bytes5 = IOUtils.toByteArray(latoblack);
        BaseFont LATO_BLACK = BaseFont.createFont("Lato-Black.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, bytes5, null);

        // Load existing PDF

        PdfReader reader = new PdfReader(new Main().getClass().getResource("Vagina_English.pdf"));
        PdfImportedPage page;

        page = writer.getImportedPage(reader, 1);
        document.newPage();
        cb.addTemplate(page, 0, 0);

        cb.beginText();
        cb.setFontAndSize(CANDARA_BOLD, 12.5f);
        cb.setColorFill(new BaseColor(255, 255, 255));
        cb.showTextAligned(PdfContentByte.ALIGN_RIGHT, barcode, 570f, 58.8f, 0);
        cb.showTextAligned(PdfContentByte.ALIGN_RIGHT, coldate, 570f, 37f, 0);

        cb.setFontAndSize(LATO_BLACK, 29f);
        cb.setColorFill(new BaseColor(255, 255, 255));
        cb.showTextAligned(PdfContentByte.ALIGN_LEFT, isDemo ? "DEMO REPORT" : "Sample Report", 44f, 390f, 0);
        cb.endText();

        page = writer.getImportedPage(reader, 2);
        document.newPage();
        cb.addTemplate(page, 0, 0);
        addWatermark(cb,CANDARA_BOLD);

        page = writer.getImportedPage(reader, 3);
        document.newPage();
        cb.addTemplate(page, 0, 0);

        float xpos = 440f;
        float ypos = 677f;
        float coef = 0.5f;
        float width = 155f*coef;
        float height = 20f*coef;
        float radius = height/2f;
        float boxwidth = 505f;
        //These values are for large graph
        xpos = 45f;
        ypos = 750f;
        coef = 0.75f;
        width = 480f*coef;
        height = 17f*coef;


        int item = 2;

        String[] chapters = new String[]{"Diversity of the microbiome (Shannon-Index)",
                "Balance of the microbiome (Dysbiosis-Index)",
                "Endometriosis",
                "Fertility",
                "HPV Infection",
                "Fungal Infection"};

        for(int i=2;i<14;i=i+2){
            Image img = Image.getInstance(datadir+"/"+dat.get(keys.get(i)));
            String text = dat.get(keys.get(i+1));
            String section = chapters[(i/2)-1];
            img.setAbsolutePosition(xpos, ypos);

            ypos = drawSummary(cb,section,text,img,xpos,ypos,width,height,boxwidth);
            item=i+2;
        }
        addWatermark(cb,CANDARA_BOLD);

        for(int i=19;i<21;i++){
            page = writer.getImportedPage(reader, i);
            document.newPage();
            cb.addTemplate(page, 0, 0);
            addWatermark(cb,CANDARA_BOLD);
        }

        document.close();
    }

    public static void drawGraphSmall(PdfContentByte cb,Image img,float xpos,float ypos,float width, float height,
                                      float radius) throws Exception{
        cb.saveState();
        cb.roundRectangle(xpos, ypos,
                width, height, radius);
        cb.clip();
        cb.newPath(); // Clear the path for future drawings
        cb.setColorFill(new BaseColor(245,245,245,255));
        cb.rectangle(xpos, ypos,
                width, height);
        cb.fill();
        cb.addImage(img, width*1.004f, 0, 0, height*1.02f,
                xpos-0.002f*width, ypos-0.01f*height);
        cb.restoreState();
    }

    public static void drawGraphLarge(PdfContentByte cb,Image img,float xpos,float ypos,float width, float height,
                                      float radius) throws Exception{
        cb.saveState();
        cb.roundRectangle(xpos, ypos,
                width, height, radius);
        cb.clip();
        cb.newPath(); // Clear the path for future drawings
        cb.setColorFill(new BaseColor(245,245,245,255));
        cb.rectangle(xpos, ypos,
                width, height);
        cb.fill();
        cb.addImage(img, width*1.002f, 0, 0, height*1.02f,
                xpos-0.001f*width, ypos-0.01f*height);
        cb.restoreState();
    }

    public static LinkedHashMap<String, LinkedHashMap<String,String>> readResultsData() throws Exception{
        LinkedHashMap<String, LinkedHashMap<String,String>> res = new LinkedHashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(datadir+"/"+indesignfile));
        String s = br.readLine();
        String[] headers = s.split("\t",-1);
        int barcodepos = Arrays.asList(headers).indexOf("1.1_Text_Barcode");

        while((s=br.readLine())!=null){
            String[] s2 = s.split("\t",-1);
            LinkedHashMap<String,String> sample = new LinkedHashMap<>();
            for(int i=0;i<s2.length;i++){
                sample.put(headers[i],s2[i]);
            }
            res.put(s2[barcodepos],sample);
        }

        br.close();
        return(res);
    }

    public static void addParagraph(String text, PdfContentByte cb,float x1,float y1, float x2, float y2,
                                    BaseFont font,float size) throws Exception{
        Rectangle rect = new Rectangle(x1, y1, x2, y2);

        ColumnText columnText = new ColumnText(cb);

        columnText.setSimpleColumn(rect);

        Paragraph paragraph = new Paragraph();
        paragraph.setAlignment(Element.ALIGN_LEFT);
        paragraph.setFont(new Font(font,size));
        paragraph.setLeading(18f);
        paragraph.add(text);

        columnText.addElement(paragraph);
        columnText.go();
    }

    public static void addWatermark(PdfContentByte cb, BaseFont font){
        if(isDemo){
            cb.beginText();
            cb.setFontAndSize(font, 90f);
            cb.setColorFill(new BaseColor(0, 0, 0,70));
            cb.showTextAligned(PdfContentByte.ALIGN_CENTER, "DEMO REPORT", 325f, 400f, 45);
            cb.endText();
        }
    }

    public static float drawSummary(PdfContentByte cb, String section, String text, Image img,float xpos,float ypos,float width, float height,float boxwidth) throws Exception{
        byte[] bytes = IOUtils.toByteArray(candara);
        BaseFont CANDARA_REGULAR = BaseFont.createFont("CenturyGothicPaneuropeanRegular.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, bytes, null);
        byte[] bytes2 = IOUtils.toByteArray(candarabold);
        BaseFont CANDARA_BOLD = BaseFont.createFont("CenturyGothicPaneuropeanBold.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, bytes2, null);

        byte[] bytes4 = IOUtils.toByteArray(latobold);
        BaseFont LATO_BOLD = BaseFont.createFont("Lato-Bold.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, bytes4, null);

        cb.setColorFill(BaseColor.BLACK);
        addParagraph(section,cb,xpos,ypos-1000f,xpos+width,ypos,LATO_BOLD,12f);

        float bottom = addParagraphTemp(text,xpos+10f,ypos-50f,xpos+boxwidth-20f,ypos-1000f,CANDARA_REGULAR,10f);

        cb.setColorFill(new BaseColor(240, 240, 240));
        cb.rectangle(xpos,ypos-25f,boxwidth,-(ypos-10f-bottom));
        cb.fill();

        cb.setColorFill(BaseColor.BLACK);
        addParagraph("Your Result:",cb,xpos+10f,ypos-30f,xpos+boxwidth-20f,ypos-1000f,CANDARA_BOLD,10f);

        drawGraphLarge(cb,img,xpos+(boxwidth-width)/2+20f,(section.equals("Enterotyp 1, 2, oder 3?")? ypos-55f:ypos-50f),width,height,height/2);

        cb.setColorFill(BaseColor.BLACK);
        addParagraph(text,cb,xpos+10f,ypos-50,xpos+boxwidth-20f,ypos-1000f,CANDARA_REGULAR,10f);

        return(bottom-30f);
    }

    public static float addParagraphTemp(String text, float x1,float y1, float x2, float y2,
                                         BaseFont font,float size) throws Exception{
        Rectangle rect = new Rectangle(x1, y1, x2, y2);

        File tempFile = File.createTempFile("output", ".pdf");

        com.itextpdf.text.Document document = new com.itextpdf.text.Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(tempFile));
        document.open();
        PdfContentByte cb = writer.getDirectContent();

        ColumnText columnText = new ColumnText(cb);
        columnText.setSimpleColumn(rect);

        Paragraph paragraph = new Paragraph();
        paragraph.setAlignment(Element.ALIGN_LEFT);
        paragraph.setFont(new Font(font,size));
        paragraph.setLeading(18f);
        paragraph.add(text);

        columnText.addElement(paragraph);
        columnText.go();

        document.close();

        return(columnText.getYLine());
    }

    public static void drawBox(PdfContentByte cb,float xpos, float ypos,float width,float height,float radius,Image img,String text, float... plotyadj) throws Exception{
        float xpad = 20f;
        float recbot = addParagraphTemp(text,xpos+xpad,ypos-195f, xpos+width-2*xpad, ypos-5f,CANDARA_REGULAR,10f);

        cb.setColorFill(new BaseColor(240, 240, 240));
        cb.rectangle(xpos,ypos+60f,width,-(ypos+70f-recbot+10f));
        cb.fill();

        cb.setColorFill(BaseColor.BLACK);
        addParagraph("Your Result:", cb,xpos+xpad,ypos, xpos+width-2*xpad, ypos+50f,LATO_BOLD,15f);

        drawGraphLarge(cb,img,xpos+xpad,ypos+(plotyadj.length==1?plotyadj[0]:0f),width-2*xpad,height*(1-2*xpad/width),radius*(1-2*xpad/width));

        cb.setColorFill(BaseColor.BLACK);
        addParagraph(text, cb,xpos+xpad,ypos-195f, xpos+width-2*xpad, ypos-5f,CANDARA_REGULAR,10f);
    }

}
