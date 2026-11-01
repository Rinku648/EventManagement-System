package com.eventmanagement.util;

import java.awt.Color;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.File;
import java.io.IOException;


public class PdfGenerator {

    // Aurora theme colors (matches UITheme.java in the Java Swing app)
    private static final Color PRIMARY = new Color(0x4F, 0x46, 0xE5);
    private static final Color TEXT_DARK = new Color(0x0F, 0x17, 0x2A);
    private static final Color TEXT_MUTED = new Color(0x64, 0x74, 0x8B);
    private static final Color BORDER = new Color(0xE2, 0xE8, 0xF0);
    private static final Color PAGE_BG = Color.WHITE;

    // Bezier constant for approximating a quarter-circle
    private static final float KAPPA = 0.5522848f;

    public static String generateTicketPdf(String fileNameWithoutExt, int bookingId, String eventName,
                                           String attendeeName, String ticketType, String venue, String hall,
                                           String qrImagePath) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A6);
            document.addPage(page);

            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();

            PDType1Font bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font regular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {

                float cardMargin = 12;
                float cardX = cardMargin;
                float cardWidth = pageWidth - (cardMargin * 2);

                // ---- Header band (indigo) ----
                float headerHeight = 68;
                float headerY = pageHeight - headerHeight;
                content.setNonStrokingColor(PRIMARY);
                content.addRect(0, headerY, pageWidth, headerHeight);
                content.fill();

                content.setNonStrokingColor(Color.WHITE);
                drawCentered(content, bold, 16, "EVENT TICKET", pageWidth, headerY + 38);
                drawCentered(content, regular, 9, "Booking #" + bookingId, pageWidth, headerY + 18);

                // ---- Event name (large, dark) ----
                float cursorY = headerY - 30;
                content.setNonStrokingColor(TEXT_DARK);
                drawCentered(content, bold, 14, eventName, pageWidth, cursorY);

                // ---- Divider line under event name ----
                cursorY -= 16;
                content.setStrokingColor(BORDER);
                content.setLineWidth(1f);
                content.moveTo(cardX + 8, cursorY);
                content.lineTo(pageWidth - cardX - 8, cursorY);
                content.stroke();

                // ---- Detail rows: bold label left, value right-aligned on same baseline ----
                cursorY -= 22;
                float labelX = cardX + 12;
                float rowSpacing = 21;

                cursorY = drawDetailRow(content, bold, regular, labelX, cursorY, pageWidth, cardX,
                        "Attendee", attendeeName);
                cursorY -= rowSpacing;
                cursorY = drawDetailRow(content, bold, regular, labelX, cursorY, pageWidth, cardX,
                        "Ticket Type", ticketType);
                cursorY -= rowSpacing;
                cursorY = drawDetailRow(content, bold, regular, labelX, cursorY, pageWidth, cardX,
                        "Venue", venue);

                if (hall != null && !hall.isBlank()) {
                    cursorY -= rowSpacing;
                    cursorY = drawDetailRow(content, bold, regular, labelX, cursorY, pageWidth, cardX,
                            "Hall", hall);
                }

                cursorY -= rowSpacing;
                drawDetailRow(content, bold, regular, labelX, cursorY, pageWidth, cardX,
                        "Booking ID", String.valueOf(bookingId));

                // ---- Perforated "ticket stub" divider ----
                float perfY = cursorY - 20;
                float notchRadius = 8;

                // Notch cutouts (matches page background so it looks punched through the card border)
                drawCircle(content, cardX, perfY, notchRadius, PAGE_BG);
                drawCircle(content, pageWidth - cardX, perfY, notchRadius, PAGE_BG);

                // Dashed line across the stub
                content.setStrokingColor(BORDER);
                content.setLineWidth(1.2f);
                content.setLineDashPattern(new float[]{4, 3}, 0);
                content.moveTo(cardX + notchRadius + 4, perfY);
                content.lineTo(pageWidth - cardX - notchRadius - 4, perfY);
                content.stroke();
                content.setLineDashPattern(new float[]{}, 0); // reset to solid for anything drawn after

                // ---- "SCAN TO CHECK-IN" label ----
                content.setNonStrokingColor(PRIMARY);
                drawCentered(content, bold, 8, "SCAN TO CHECK-IN", pageWidth, perfY - 18);

                // ---- QR code with bordered box, centered ----
                float qrSize = 108;
                float qrX = (pageWidth - qrSize) / 2;
                float qrY = 34;

                content.setStrokingColor(BORDER);
                content.setLineWidth(1f);
                content.addRect(qrX - 8, qrY - 8, qrSize + 16, qrSize + 16);
                content.stroke();

                PDImageXObject qrImage = PDImageXObject.createFromFile(qrImagePath, document);
                content.drawImage(qrImage, qrX, qrY, qrSize, qrSize);

                // ---- Outer card border (frames the whole ticket below the header) ----
                content.setStrokingColor(BORDER);
                content.setLineWidth(1f);
                content.addRect(cardX, 10, cardWidth, headerY - 10);
                content.stroke();

                // ---- Bottom accent bar ----
                content.setNonStrokingColor(PRIMARY);
                content.addRect(0, 0, pageWidth, 6);
                content.fill();
            }

            File dir = new File("generated/tickets");
            if (!dir.exists()) dir.mkdirs();
            String outputPath = dir.getPath() + File.separator + fileNameWithoutExt + ".pdf";
            document.save(outputPath);
            return outputPath;
        }
    }

    /** Draws a label (muted, bold) on the left and its value (dark, regular) right-aligned on the same line. */
    private static float drawDetailRow(
            PDPageContentStream content,
            PDFont bold,
            PDFont regular,
            float labelX,
            float y,
            float pageWidth,
            float cardX,
            String label,
            String value) throws IOException {

        content.setNonStrokingColor(TEXT_MUTED);

        content.beginText();
        content.setFont(bold, 9);
        content.newLineAtOffset(labelX, y);
        content.showText(label.toUpperCase());
        content.endText();

        content.setNonStrokingColor(TEXT_DARK);

        float valueFontSize = 10.5f;
        float valueWidth = regular.getStringWidth(value) / 1000 * valueFontSize;
        float valueX = pageWidth - cardX - 12 - valueWidth;

        content.beginText();
        content.setFont(regular, valueFontSize);
        content.newLineAtOffset(valueX, y - 1);
        content.showText(value);
        content.endText();

        return y;
    }

    /** Draws text horizontally centered on the page at the given baseline Y. */
    private static void drawCentered(PDPageContentStream content, PDFont font, float fontSize,
                                     String text, float pageWidth, float y) throws IOException {
        float textWidth = font.getStringWidth(text) / 1000 * fontSize;
        float x = (pageWidth - textWidth) / 2;
        content.beginText();
        content.setFont(font, fontSize);
        content.newLineAtOffset(x, y);
        content.showText(text);
        content.endText();
    }

    /** Draws a filled circle centered at (cx, cy) using a 4-curve Bezier approximation. */
    private static void drawCircle(PDPageContentStream content, float cx, float cy, float r, Color fillColor)
            throws IOException {
        float k = r * KAPPA;
        content.setNonStrokingColor(fillColor);
        content.moveTo(cx + r, cy);
        content.curveTo(cx + r, cy + k, cx + k, cy + r, cx, cy + r);
        content.curveTo(cx - k, cy + r, cx - r, cy + k, cx - r, cy);
        content.curveTo(cx - r, cy - k, cx - k, cy - r, cx, cy - r);
        content.curveTo(cx + k, cy - r, cx + r, cy - k, cx + r, cy);
        content.closePath();
        content.fill();
    }

    public static String generateCertificatePdf(String fileNameWithoutExt, String attendeeName,
                                                String eventName, String certificateNumber) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                PDType1Font bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                content.beginText();
                content.setFont(bold, 22);
                content.newLineAtOffset(150, 700);
                content.showText("Certificate of Participation");
                content.endText();

                PDType1Font regular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                content.beginText();
                content.setFont(regular, 14);
                content.newLineAtOffset(100, 620);
                content.showText("This certifies that " + attendeeName);
                content.newLineAtOffset(0, -25);
                content.showText("participated in " + eventName);
                content.newLineAtOffset(0, -25);
                content.showText("Certificate No: " + certificateNumber);
                content.endText();
            }
            File dir = new File("generated/certificates");
            if (!dir.exists()) dir.mkdirs();
            String outputPath = dir.getPath() + File.separator + fileNameWithoutExt + ".pdf";
            document.save(outputPath);
            return outputPath;
        }
    }
}