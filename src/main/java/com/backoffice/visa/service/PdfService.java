package com.backoffice.visa.service;

import com.backoffice.visa.entity.Demande;
import com.backoffice.visa.entity.Demandeur;
import com.backoffice.visa.entity.PieceDemande;
import com.backoffice.visa.entity.PieceDemandeSpecifique;
import com.backoffice.visa.repository.DemandeRepository;
import com.backoffice.visa.repository.PieceDemandeRepository;
import com.backoffice.visa.repository.PieceDemandeSpecifiqueRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class PdfService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.server.url:http://192.168.88.14:8080}")
    private String serverUrl;

    private final DemandeRepository demandeRepository;
    private final PieceDemandeRepository pieceDemandeRepository;
    private final PieceDemandeSpecifiqueRepository pieceDemandeSpecifiqueRepository;

    public PdfService(DemandeRepository demandeRepository,
                      PieceDemandeRepository pieceDemandeRepository,
                      PieceDemandeSpecifiqueRepository pieceDemandeSpecifiqueRepository) {
        this.demandeRepository = demandeRepository;
        this.pieceDemandeRepository = pieceDemandeRepository;
        this.pieceDemandeSpecifiqueRepository = pieceDemandeSpecifiqueRepository;
    }

    // ─────────────────────────────────────────────────────────────
    // ACCUSÉ DE RÉCEPTION avec QR code (condition : SCANNE ou +)
    // ─────────────────────────────────────────────────────────────
    public byte[] genererAccuseReception(Long demandeId) throws IOException {
        Demande demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new RuntimeException("Demande introuvable"));

        if (demande.getStatut() < Demande.STATUT_SCANNE) {
            throw new RuntimeException("Le dossier doit être au moins scanné pour générer l'accusé de réception.");
        }

        Demandeur dem = demande.getDemandeur();
        String reference = "DOS-" + String.format("%05d", demandeId);
        String nom = dem != null ? dem.getNom() + " " + dem.getPrenom() : "-";

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();

            // QR code — URL de suivi public
            String qrUrl = serverUrl + "/suivi.html?ref=" + reference;
            byte[] qrBytes = genererQRCode(qrUrl, 200);
            PDImageXObject qrImage = PDImageXObject.createFromByteArray(doc, qrBytes, "qr");

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                // ── En-tête ──
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 11);
                cs.setNonStrokingColor(0.1f, 0.32f, 0.47f); // #1a5276
                centerText(cs, "REPUBLIQUE DE MADAGASCAR", pageWidth, pageHeight - 50, 11, true);
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                cs.setNonStrokingColor(0f, 0f, 0f);
                centerText(cs, "Fitiavana - Tanindrazana - Fandrosoana", pageWidth, pageHeight - 65, 10, false);

                // ── Ligne de séparation ──
                cs.setStrokingColor(0.1f, 0.32f, 0.47f);
                cs.setLineWidth(1.5f);
                cs.moveTo(60, pageHeight - 75);
                cs.lineTo(pageWidth - 60, pageHeight - 75);
                cs.stroke();

                // ── Titre ──
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
                cs.setNonStrokingColor(0.1f, 0.32f, 0.47f);
                centerText(cs, "ACCUSÉ DE RÉCEPTION DE DOSSIER", pageWidth, pageHeight - 110, 16, true);

                // ── Corps ──
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                cs.setNonStrokingColor(0f, 0f, 0f);

                float y = pageHeight - 160;
                float margin = 80;

                drawText(cs, "Nous accusons réception du dossier de demande soumis par :", margin, y, 12, false);
                y -= 25;
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 13);
                drawText(cs, nom, margin + 20, y, 13, true);
                y -= 30;

                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                drawText(cs, "Le dossier a été enregistré sous la référence :", margin, y, 12, false);
                y -= 25;
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
                cs.setNonStrokingColor(0.1f, 0.32f, 0.47f);
                centerText(cs, reference, pageWidth, y, 14, true);
                y -= 35;

                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
                cs.setNonStrokingColor(0.3f, 0.3f, 0.3f);
                centerText(cs, "Date de la demande : " + (demande.getDateDemande() != null ? demande.getDateDemande().toString() : "-"), pageWidth, y, 11, false);
                y -= 40;

                // ── Message ──
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
                cs.setNonStrokingColor(0f, 0f, 0f);
                drawText(cs, "Veuillez conserver ce document comme justificatif de dépôt.", margin, y, 11, false);
                y -= 18;
                drawText(cs, "Il vous sera demandé lors du retrait de votre titre.", margin, y, 11, false);

                // ── QR Code ──
                float qrSize = 130;
                float qrX = (pageWidth - qrSize) / 2;
                float qrY = y - qrSize - 30;
                cs.drawImage(qrImage, qrX, qrY, qrSize, qrSize);

                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);
                cs.setNonStrokingColor(0.5f, 0.5f, 0.5f);
                centerText(cs, "Scanner ce QR code pour vérifier la référence du dossier", pageWidth, qrY - 15, 9, false);

                // ── Pied de page ──
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);
                cs.setNonStrokingColor(0.5f, 0.5f, 0.5f);
                centerText(cs, "Back Office Visa — Madagascar", pageWidth, 40, 9, false);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            return baos.toByteArray();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // APERÇU DES PIÈCES JUSTIFICATIVES (PDF inline)
    // ─────────────────────────────────────────────────────────────
    public byte[] genererApercuPieces(Long demandeId) throws IOException {
        Demande demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new RuntimeException("Demande introuvable"));

        List<String> fichiers = new ArrayList<>();

        List<PieceDemande> piecesCommunes = pieceDemandeRepository.findByDemandeId(demandeId);
        for (PieceDemande p : piecesCommunes) {
            if (p.getFichierPath() != null && !p.getFichierPath().isBlank()) {
                fichiers.add(p.getFichierPath());
            }
        }
        List<PieceDemandeSpecifique> piecesSpec = pieceDemandeSpecifiqueRepository.findByDemandeId(demandeId);
        for (PieceDemandeSpecifique p : piecesSpec) {
            if (p.getFichierPath() != null && !p.getFichierPath().isBlank()) {
                fichiers.add(p.getFichierPath());
            }
        }

        if (fichiers.isEmpty()) {
            throw new RuntimeException("Aucune pièce justificative disponible pour ce dossier.");
        }

        try (PDDocument doc = new PDDocument()) {
            // Page de couverture
            PDPage cover = new PDPage(PDRectangle.A4);
            doc.addPage(cover);
            float pw = cover.getMediaBox().getWidth();
            float ph = cover.getMediaBox().getHeight();
            String reference = "DOS-" + String.format("%05d", demandeId);

            try (PDPageContentStream cs = new PDPageContentStream(doc, cover)) {
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 11);
                cs.setNonStrokingColor(0.1f, 0.32f, 0.47f);
                centerText(cs, "REPUBLIQUE DE MADAGASCAR", pw, ph - 50, 11, true);
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                cs.setNonStrokingColor(0f, 0f, 0f);
                centerText(cs, "Fitiavana - Tanindrazana - Fandrosoana", pw, ph - 65, 10, false);

                cs.setStrokingColor(0.1f, 0.32f, 0.47f);
                cs.setLineWidth(1.5f);
                cs.moveTo(60, ph - 75);
                cs.lineTo(pw - 60, ph - 75);
                cs.stroke();

                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 15);
                cs.setNonStrokingColor(0.1f, 0.32f, 0.47f);
                centerText(cs, "APERÇU DES PIÈCES JUSTIFICATIVES", pw, ph - 115, 15, true);
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 13);
                centerText(cs, "Dossier : " + reference, pw, ph - 140, 13, true);

                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
                cs.setNonStrokingColor(0f, 0f, 0f);
                centerText(cs, fichiers.size() + " pièce(s) jointe(s)", pw, ph - 170, 11, false);
            }

            // Une page par fichier image
            for (String fichier : fichiers) {
                Path filePath = Paths.get(uploadDir).resolve(fichier).normalize();
                if (!Files.exists(filePath)) continue;

                String lowerName = fichier.toLowerCase();
                if (lowerName.endsWith(".pdf")) {
                    // Merge le PDF externe page par page
                    try (PDDocument externe = Loader.loadPDF(filePath.toFile())) {
                        for (PDPage exPage : externe.getPages()) {
                            doc.addPage(externe.getPage(externe.getPages().indexOf(exPage)));
                        }
                    } catch (Exception ignored) {
                        // Si le PDF ne peut pas être mergé, on l'ignore
                    }
                } else {
                    // Image → une nouvelle page A4
                    BufferedImage img;
                    try (InputStream is = Files.newInputStream(filePath)) {
                        img = ImageIO.read(is);
                    }
                    if (img == null) continue;

                    PDPage imgPage = new PDPage(PDRectangle.A4);
                    doc.addPage(imgPage);
                    PDImageXObject pdImg = PDImageXObject.createFromByteArray(doc,
                            Files.readAllBytes(filePath), fichier);

                    float maxW = imgPage.getMediaBox().getWidth() - 40;
                    float maxH = imgPage.getMediaBox().getHeight() - 60;
                    float[] dims = fitImage(img.getWidth(), img.getHeight(), maxW, maxH);
                    float x = (imgPage.getMediaBox().getWidth() - dims[0]) / 2;
                    float y = (imgPage.getMediaBox().getHeight() - dims[1]) / 2;

                    try (PDPageContentStream cs = new PDPageContentStream(doc, imgPage)) {
                        cs.drawImage(pdImg, x, y, dims[0], dims[1]);
                    }
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            return baos.toByteArray();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────
    private byte[] genererQRCode(String contenu, int taille) throws IOException {
        QRCodeWriter writer = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.MARGIN, 1);
        try {
            BitMatrix matrix = writer.encode(contenu, BarcodeFormat.QR_CODE, taille, taille, hints);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", baos);
            return baos.toByteArray();
        } catch (WriterException e) {
            throw new IOException("Impossible de générer le QR code", e);
        }
    }

    private void centerText(PDPageContentStream cs, String text, float pageWidth,
                             float y, float fontSize, boolean bold) throws IOException {
        PDType1Font font = bold
                ? new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD)
                : new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        cs.setFont(font, fontSize);
        float textWidth = font.getStringWidth(text) / 1000 * fontSize;
        float x = (pageWidth - textWidth) / 2;
        cs.beginText();
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
    }

    private void drawText(PDPageContentStream cs, String text, float x, float y,
                          float fontSize, boolean bold) throws IOException {
        PDType1Font font = bold
                ? new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD)
                : new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        cs.setFont(font, fontSize);
        cs.beginText();
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
    }

    private float[] fitImage(int imgW, int imgH, float maxW, float maxH) {
        float ratio = Math.min(maxW / imgW, maxH / imgH);
        return new float[]{imgW * ratio, imgH * ratio};
    }
}
