package com.projekt.services;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.projekt.models.Category;
import com.projekt.models.Ticket;
import com.projekt.models.TicketReply;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
public class PDFService {
    @Autowired
    private TicketService ticketService;

    public void createPDF(Integer ticketID) throws IOException, DocumentException {
        if(new File("src/main/resources/pdf/"+ticketID+".pdf").exists()){
            File file = new File("src/main/resources/pdf/"+ticketID+".pdf");
            file.delete();
        }

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream("src/main/resources/pdf/"+ticketID+".pdf"));
        document.open();
        document.addTitle("Zgłoszenie nr "+ticketID);

        var myFont = FontFactory.getFont(BaseFont.COURIER, BaseFont.CP1250, BaseFont.EMBEDDED,16);
        var myFont1 = FontFactory.getFont(BaseFont.COURIER, BaseFont.CP1250, BaseFont.EMBEDDED,12);

        Ticket ticket = ticketService.loadTicketById(ticketID);
        Chunk chunk = new Chunk("Zgłoszenie id "+ticketID, myFont);

        Paragraph paragraph = new Paragraph("Tytuł: "+ticket.getTicketTitle(),myFont1);
        Paragraph paragraph1 = new Paragraph("Użytkownik: "+ticket.getUser().getUsername() + " (" + ticket.getUser().getName()
                + " " + ticket.getUser().getSurname() + ")" ,myFont1);
        Paragraph paragraph2 = new Paragraph("Data: "+ticket.getTicketDate(),myFont1);
        Paragraph paragraph3 = new Paragraph("Status: "+ticket.getStatus().getStatusName(),myFont1);

        String kategorie = "";
        ArrayList<Category> arrayList = new ArrayList<>(ticket.getCategories());
        for(int i=0; i<arrayList.size(); i++){
           kategorie += arrayList.get(i).getCategoryName() + " ";
        }
        Paragraph paragraph4 = new Paragraph("Kategorie: "+kategorie,myFont1);

        Paragraph paragraph5 = new Paragraph("Priorytet: "+ticket.getPriority().getPriorityName(),myFont1);
        Paragraph paragraph6 = new Paragraph("Opis: "+ticket.getTicketDescription(),myFont1);
        Paragraph paragraph7 = new Paragraph("Oprogramowanie i wersja: "+ticket.getVersion().toString() + " ("
                + ticket.getVersion().getSoftware().getSoftwareName() + ")",myFont1);

        document.add(chunk);
        document.add(paragraph);
        document.add(paragraph1);
        document.add(paragraph2);
        document.add(paragraph3);
        document.add(paragraph4);
        document.add(paragraph5);
        document.add(paragraph6);
        document.add(paragraph7);

        document.add(Chunk.NEWLINE);
        Paragraph paragraph10 = new Paragraph("Odpowiedzi",myFont1);
        document.add(paragraph10);
        document.add(Chunk.NEWLINE);
        if(ticket.getTicketReplies().size() > 0) {
            PdfPTable table = new PdfPTable(3);
            addTableHeader(table);
            addRows(table, ticket.getTicketReplies(),myFont1);
            document.add(table);
        }else{
            Paragraph paragraph11 = new Paragraph("Brak odpowiedzi",myFont1);
            document.add(paragraph11);
        }

        document.add(Chunk.NEXTPAGE);
        Paragraph paragraph8 = new Paragraph("Zrzuty ekranu",myFont1);
        document.add(paragraph8);
        document.add(Chunk.NEWLINE);
        if(ticket.getImages().size() > 0){
            for(int i=0; i<ticket.getImages().size(); i++){
                Image img = Image.getInstance(ticket.getImages().get(i).getFileContent());
                img.scaleAbsoluteWidth(600);
                img.scaleAbsoluteHeight(600);
                document.add(img);
            }
        }else{
            Paragraph paragraph9 = new Paragraph("Brak zrzutów ekranu",myFont1);
            document.add(paragraph9);
        }

        document.close();
    }

    private void addRows(PdfPTable table, List<TicketReply> ticketReplies, Font myFont1) {
        for(int i=0; i<ticketReplies.size(); i++){
            table.addCell(new Paragraph(ticketReplies.get(i).getUser().getName() + " " + ticketReplies.get(i).getUser().getSurname(),myFont1));
            table.addCell(ticketReplies.get(i).getReplyDate().toString());
            table.addCell(new Paragraph(ticketReplies.get(i).getReplyContent(),myFont1));
        }
    }

    private void addTableHeader(PdfPTable table) {
        Stream.of("Autor", "Data", "Tresc")
                .forEach(columnTitle -> {
                    PdfPCell header = new PdfPCell();
                    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    header.setBorderWidth(2);
                    header.setPhrase(new Phrase(columnTitle));
                    table.addCell(header);
                });
    }
}
