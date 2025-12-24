package com.rut.booking.services;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.rut.booking.models.entities.Booking;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class PdfGenerationService {

    @Value("${app.pdf.storage-path:./pdf-storage}")
    private String pdfStoragePath;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    /**
     * Loads a font that supports Cyrillic characters.
     * First tries to load from classpath (bundled font), then tries system fonts.
     */
    private PdfFont loadCyrillicFont() throws IOException {
        // First try to load bundled font from classpath
        try {
            var fontStream = getClass().getResourceAsStream("/fonts/DejaVuSans.ttf");
            if (fontStream != null) {
                byte[] fontBytes = fontStream.readAllBytes();
                fontStream.close();
                return PdfFontFactory.createFont(fontBytes, "Identity-H", PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
            }
        } catch (Exception e) {
            System.err.println("Failed to load bundled font: " + e.getMessage());
        }

        // Fallback to system fonts
        String[] fontPaths = {
                "/usr/share/fonts/truetype/freefont/FreeSans.ttf",
                "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
                "/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf"
        };

        for (String fontPath : fontPaths) {
            try {
                if (Files.exists(Paths.get(fontPath))) {
                    return PdfFontFactory.createFont(fontPath, "Identity-H", PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
                }
            } catch (Exception e) {
                System.err.println("Failed to load font from " + fontPath + ": " + e.getMessage());
            }
        }

        // If all else fails, throw an exception instead of using Helvetica
        throw new IOException("Could not load any Cyrillic font. Please ensure DejaVuSans.ttf is in resources/fonts/ or install system fonts.");
    }

    public String generateBookingConfirmationPdf(Booking booking) {
        try {
            Path storagePath = Paths.get(pdfStoragePath);
            if (!Files.exists(storagePath)) {
                Files.createDirectories(storagePath);
            }

            String filename = "booking_" + booking.getId() + "_" + UUID.randomUUID().toString().substring(0, 8) + ".pdf";
            Path filePath = storagePath.resolve(filename);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, PageSize.A4);

            document.setMargins(50, 50, 50, 50);

            // Load Russian font - using system fonts that support Cyrillic
            PdfFont font = loadCyrillicFont();
            document.setFont(font);

            // Header
            DeviceRgb headerColor = new DeviceRgb(0, 51, 102);
            Paragraph header = new Paragraph("РОССИЙСКИЙ УНИВЕРСИТЕТ ТРАНСПОРТА")
                    .setFontSize(18)
                    .setBold()
                    .setFontColor(headerColor)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(header);

            Paragraph subHeader = new Paragraph("Подтверждение бронирования аудитории")
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(30);
            document.add(subHeader);

            // Booking ID
            Paragraph bookingId = new Paragraph("Бронирование №" + booking.getId())
                    .setFontSize(12)
                    .setBold()
                    .setTextAlignment(TextAlignment.RIGHT);
            document.add(bookingId);

            // Status
            DeviceRgb statusColor = new DeviceRgb(40, 167, 69);
            Paragraph status = new Paragraph("СТАТУС: ОДОБРЕНО")
                    .setFontSize(11)
                    .setFontColor(statusColor)
                    .setBold()
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginBottom(20);
            document.add(status);

            // Main info table
            Table mainTable = new Table(UnitValue.createPercentArray(new float[]{35, 65}));
            mainTable.setWidth(UnitValue.createPercentValue(100));

            addTableRow(mainTable, "Преподаватель:", booking.getTeacher().getFullName());
            addTableRow(mainTable, "Email:", booking.getTeacher().getEmail());
            addTableRow(mainTable, "Аудитория:", booking.getRoom().getNumber() + " (" + booking.getRoom().getRoomType().getDisplayName() + ")");
            addTableRow(mainTable, "Корпус:", booking.getRoom().getBuilding());
            addTableRow(mainTable, "Этаж:", booking.getRoom().getFloor().toString());
            addTableRow(mainTable, "Вместимость:", booking.getRoom().getCapacity() + " мест");
            addTableRow(mainTable, "Дата:", booking.getBookingDate().format(DATE_FORMATTER));
            addTableRow(mainTable, "Время:", booking.getStartTime().format(TIME_FORMATTER) + " - " + booking.getEndTime().format(TIME_FORMATTER));
            addTableRow(mainTable, "Пара:", booking.getClassPeriod().getDisplayName());
            addTableRow(mainTable, "Цель:", booking.getPurpose());

            if (booking.getNotes() != null && !booking.getNotes().isEmpty()) {
                addTableRow(mainTable, "Примечания:", booking.getNotes());
            }

            document.add(mainTable);

            // Footer info
            document.add(new Paragraph("")
                    .setMarginTop(30));

            Table footerTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}));
            footerTable.setWidth(UnitValue.createPercentValue(100));

            Cell leftCell = new Cell()
                    .setBorder(Border.NO_BORDER)
                    .add(new Paragraph("Документ сгенерирован:")
                            .setFontSize(9)
                            .setFontColor(ColorConstants.GRAY))
                    .add(new Paragraph(LocalDateTime.now().format(DATETIME_FORMATTER))
                            .setFontSize(9));
            footerTable.addCell(leftCell);

            Cell rightCell = new Cell()
                    .setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .add(new Paragraph("Заявка подана:")
                            .setFontSize(9)
                            .setFontColor(ColorConstants.GRAY))
                    .add(new Paragraph(booking.getCreatedAt().format(DATETIME_FORMATTER))
                            .setFontSize(9));
            footerTable.addCell(rightCell);

            document.add(footerTable);

            // Disclaimer
            document.add(new Paragraph("Данный документ служит официальным подтверждением бронирования аудитории. " +
                    "Пожалуйста, предъявите этот документ по требованию.")
                    .setFontSize(8)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(40));

            document.add(new Paragraph("© 2025 Российский университет транспорта")
                    .setFontSize(8)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER));

            document.close();

            // Write to file
            Files.write(filePath, baos.toByteArray());

            return filePath.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage(), e);
        }
    }

    private void addTableRow(Table table, String label, String value) {
        Cell labelCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .add(new Paragraph(label)
                        .setBold()
                        .setFontSize(10)
                        .setFontColor(ColorConstants.DARK_GRAY));
        table.addCell(labelCell);

        Cell valueCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .add(new Paragraph(value)
                        .setFontSize(10));
        table.addCell(valueCell);
    }

    public byte[] readPdfFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                throw new RuntimeException("PDF file not found: " + filePath);
            }
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read PDF file: " + e.getMessage(), e);
        }
    }

    public void deletePdfFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            System.err.println("Failed to delete PDF file: " + e.getMessage());
        }
    }
}
