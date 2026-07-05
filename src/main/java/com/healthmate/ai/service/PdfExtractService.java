package com.healthmate.ai.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Extracts plain text from uploaded PDF medical reports using Apache PDFBox.
 * Extracted text is only ever held in memory for the duration of a single request;
 * it is never persisted to the database (only the AI-generated summary is stored).
 */
@Service
public class PdfExtractService {

    private static final Logger log = LoggerFactory.getLogger(PdfExtractService.class);
    private static final int MAX_CHARS = 15000;

    public String extractText(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("Uploaded file is empty.");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".pdf")) {
            throw new IOException("Only PDF files are supported.");
        }

        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            if (document.isEncrypted()) {
                throw new IOException("Encrypted PDFs are not supported.");
            }
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(document);
            if (text == null || text.isBlank()) {
                throw new IOException("No extractable text found in the PDF (it may be a scanned image).");
            }
            if (text.length() > MAX_CHARS) {
                log.info("Truncating extracted PDF text from {} to {} characters", text.length(), MAX_CHARS);
                text = text.substring(0, MAX_CHARS);
            }
            return text;
        }
    }
}
