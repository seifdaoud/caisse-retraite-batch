package com.example.caisseretraitebatch.writer;

import com.example.caisseretraitebatch.model.CaisseRetraiteRecord;
import lombok.Setter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * MultiSheetExcelItemWriter :
 * - Ecrit jusqu'à ROW_LIMIT lignes par onglet ("sheet"),
 *   puis crée un nouvel onglet (Data_1, Data_2, etc.)
 * - Génère un seul fichier de sortie (ex: output.xlsx)
 * - Gère 11 colonnes selon la classe CaisseRetraiteRecord.
 */
public class MultiSheetExcelItemWriter implements ItemWriter<CaisseRetraiteRecord>, ItemStream {

    private static final Logger logger = LoggerFactory.getLogger(MultiSheetExcelItemWriter.class);

    // Permet d’injecter le chemin final de sortie via la config (ex: "output.xlsx")
    @Setter
    private String baseOutputPath;

    // Limite de lignes par onglet avant d'en créer un nouveau
    private static final int ROW_LIMIT = 100_000;

    // Workbook unique pour tout le job
    private SXSSFWorkbook workbook;

    // Feuille Excel courante
    private Sheet currentSheet;

    // Nombre de lignes écrites dans la feuille courante
    private int rowCount = 0;

    // Index pour nommer les feuilles : Data_1, Data_2, etc.
    private int sheetIndex = 1;

    private CellStyle dateStyle;
    private CellStyle numericStyle;

    // Flux unique de sortie
    private FileOutputStream fileOutputStream;

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        logger.info(">>> [open] Initialisation du Writer multi-onglets pour : {}", baseOutputPath);
        // 1) Créer le workbook
        workbook = new SXSSFWorkbook();

        // 2) Créer le flux de sortie vers baseOutputPath
        try {
            fileOutputStream = new FileOutputStream(baseOutputPath);
        } catch (IOException e) {
            throw new ItemStreamException("Impossible d'ouvrir le fichier de sortie " + baseOutputPath, e);
        }

        // 3) Initialiser la première feuille
        createNewSheet();
        logger.info(">>> [open] Le Writer est prêt à écrire dans la feuille '{}' du workbook.", currentSheet.getSheetName());
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        // Pas de mise à jour particulière
        // Vous pouvez loguer si vous souhaitez tracer des checkpoints:
        // logger.debug(">>> [update] checkpoint ...");
    }

    @Override
    public void close() throws ItemStreamException {
        logger.info(">>> [close] Fermeture du Writer multi-onglets.");

        if (workbook != null && fileOutputStream != null) {
            try (FileOutputStream fos = fileOutputStream) {
                // Ecrit en dur tous les onglets dans le fichier
                workbook.write(fos);
                logger.info(">>> [close] Fichier Excel écrit avec succès : {}", baseOutputPath);
            } catch (IOException e) {
                logger.error(">>> [close] Erreur lors de l'écriture du fichier Excel : {}", e.getMessage(), e);
                throw new ItemStreamException("Erreur lors de l'écriture du fichier Excel", e);
            } finally {
                // Libère la mémoire (rows écrites)
                workbook.dispose();
                workbook = null;
                currentSheet = null;
                fileOutputStream = null;
                rowCount = 0;
                logger.info(">>> [close] Ressources libérées, Writer fermé.");
            }
        }
    }

    @Override
    public void write(List<? extends CaisseRetraiteRecord> items) throws Exception {
        logger.debug(">>> [write] Écriture de {} item(s) dans la feuille '{}'. (rowCount actuel: {})",
                items.size(), currentSheet.getSheetName(), rowCount);

        for (CaisseRetraiteRecord record : items) {

            if (rowCount >= ROW_LIMIT) {
                logger.info(">>> [write] Limite de {} lignes atteinte dans la feuille '{}', création d'un nouvel onglet.",
                        ROW_LIMIT, currentSheet.getSheetName());
                createNewSheet();
            }

            // Écriture d'une ligne
            Row row = currentSheet.createRow(rowCount++);

            // 0) NSS
            row.createCell(0).setCellValue(record.getNumeroSecuriteSociale());
            // 1) Nom
            row.createCell(1).setCellValue(record.getNom());
            // 2) Prénom
            row.createCell(2).setCellValue(record.getPrenom());

            // 3) Date Naissance
            Cell dateCell = row.createCell(3);
            Date dn = record.getDateNaissance();
            if (dn != null) {
                dateCell.setCellValue(dn);
                dateCell.setCellStyle(dateStyle);
            }

            // 4) Adresse
            row.createCell(4).setCellValue(record.getAdresse() != null ? record.getAdresse() : "");

            // 5) Code postal
            row.createCell(5).setCellValue(record.getCodePostal() != null ? record.getCodePostal() : "");

            // 6) Ville
            row.createCell(6).setCellValue(record.getVille() != null ? record.getVille() : "");

            // 7) Pays
            row.createCell(7).setCellValue(record.getPays() != null ? record.getPays() : "");

            // 8) Nom Conjoint
            row.createCell(8).setCellValue(record.getNomConjoint() != null ? record.getNomConjoint() : "");

            // 9) Nombre d'enfants
            Cell enfantsCell = row.createCell(9);
            if (record.getNombreEnfants() != null) {
                enfantsCell.setCellValue(record.getNombreEnfants());
            } else {
                enfantsCell.setCellValue(0);
            }
            enfantsCell.setCellStyle(numericStyle);

            // 10) Montant Cotisation
            Cell cotiCell = row.createCell(10);
            if (record.getMontantCotisation() != null) {
                cotiCell.setCellValue(record.getMontantCotisation());
            } else {
                cotiCell.setCellValue(0.0);
            }
            cotiCell.setCellStyle(numericStyle);
        }
    }

    /**
     * Crée un nouvel onglet (sheet) et réinitialise rowCount à zéro.
     */
    private void createNewSheet() {
        currentSheet = workbook.createSheet("Data_" + (sheetIndex++));
        rowCount = 0;

        // Création des styles si pas déjà fait
        if (dateStyle == null || numericStyle == null) {
            DataFormat format = workbook.createDataFormat();

            dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(format.getFormat("yyyy-MM-dd"));

            numericStyle = workbook.createCellStyle();
            numericStyle.setDataFormat(format.getFormat("0.00"));
        }

        // Ecriture de l'en-tête (11 colonnes)
        Row header = currentSheet.createRow(rowCount++);
        header.createCell(0).setCellValue("NSS");
        header.createCell(1).setCellValue("Nom");
        header.createCell(2).setCellValue("Prénom");
        header.createCell(3).setCellValue("Date Naissance");
        header.createCell(4).setCellValue("Adresse");
        header.createCell(5).setCellValue("Code Postal");
        header.createCell(6).setCellValue("Ville");
        header.createCell(7).setCellValue("Pays");
        header.createCell(8).setCellValue("Nom Conjoint");
        header.createCell(9).setCellValue("Nombre Enfants");
        header.createCell(10).setCellValue("Cotisation");

        logger.debug(">>> [createNewSheet] Nouvelle sheet '{}' créée, entête écrit.", currentSheet.getSheetName());
    }
}
