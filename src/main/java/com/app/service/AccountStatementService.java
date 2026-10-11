
package com.app.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.app.entity.BankAccount;
import com.app.entity.Transaction;
import com.app.exception.AccountNotFoundException;
import com.app.repo.BankAccountRepo;
import com.app.repo.TransactionRepo;

@Service
public class AccountStatementService {

    private final BankAccountRepo bankAccountRepo;
    private final TransactionRepo transactionRepo;

    public AccountStatementService(
            BankAccountRepo bankAccountRepo,
            TransactionRepo transactionRepo) {
        this.bankAccountRepo = bankAccountRepo;
        this.transactionRepo = transactionRepo;
    }

    public byte[] generatePdf(String username) {

        BankAccount account = bankAccountRepo
                .findByUserUsername(username)
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Bank account not found"));

        List<Transaction> transactions =
                transactionRepo
                        .findByBankaccountUserUsernameOrderByDateDesc(
                                username);

        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            PDType1Font regular = new PDType1Font(
                    Standard14Fonts.FontName.HELVETICA);

            PDType1Font bold = new PDType1Font(
                    Standard14Fonts.FontName.HELVETICA_BOLD);

            int pageNumber = 1;

            PDPageContentStream content = startPage(
                    document, account, username,
                    pageNumber, regular, bold);

            float y = 640;

            if (transactions.isEmpty()) {
                drawText(content, regular, 50, y, 10,
                        "No transactions recorded for this account.");
            } else {
                DateTimeFormatter dateFormat =
                        DateTimeFormatter.ofPattern(
                                "dd-MM-yyyy HH:mm");

                for (Transaction transaction : transactions) {

                    // Start a new page when space runs out.
                    if (y < 55) {
                        content.close();
                        pageNumber++;

                        content = startPage(
                                document, account, username,
                                pageNumber, regular, bold);

                        y = 640;
                    }

                    String transactionDate =
                            transaction.getDate() == null
                                    ? "-"
                                    : transaction.getDate()
                                            .format(dateFormat);

                    String type = safeText(transaction.getType());

                    String counterparty =
                            transaction.getCounterpartyUsername() == null
                                    ? "N/A"
                                    : safeText(
                                            transaction.getCounterpartyUsername());

                    if (counterparty.length() > 22) {
                        counterparty =
                                counterparty.substring(0, 22);
                    }

                    String amount = formatAmount(
                            transaction.getAmount());

                    drawText(content, regular, 50, y, 8,
                            transactionDate);

                    drawText(content, regular, 145, y, 8,
                            type);

                    drawText(content, regular, 275, y, 8,
                            counterparty);

                    drawText(content, regular, 465, y, 8,
                            amount);

                    y -= 20;
                }
            }

            content.close();
            document.save(output);

            return output.toByteArray();

        } catch (IOException ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to generate account statement PDF");
        }
    }

    private PDPageContentStream startPage(
            PDDocument document,
            BankAccount account,
            String username,
            int pageNumber,
            PDType1Font regular,
            PDType1Font bold) throws IOException {

        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        PDPageContentStream content =
                new PDPageContentStream(document, page);

        drawText(content, bold, 50, 790, 18,
                "BANKFLOW ACCOUNT STATEMENT");

        drawText(content, regular, 50, 772, 9,
                "Generated: " + LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern(
                                "dd-MM-yyyy HH:mm"))
                        + " | Page " + pageNumber);

        drawText(content, bold, 50, 745, 10,
                "Account Holder: " + safeText(username));

        drawText(content, regular, 50, 727, 10,
                "Account Number: "
                        + safeText(account.getAccountNumber()));

        drawText(content, regular, 50, 709, 10,
                "Current Available Balance (INR): "
                        + formatAmount(account.getBalance()));

        content.moveTo(50, 690);
        content.lineTo(545, 690);
        content.stroke();

        drawText(content, bold, 50, 670, 9, "Date / Time");
        drawText(content, bold, 145, 670, 9, "Transaction Type");
        drawText(content, bold, 275, 670, 9, "Counterparty");
        drawText(content, bold, 465, 670, 9, "Amount (INR)");

        content.moveTo(50, 658);
        content.lineTo(545, 658);
        content.stroke();

        return content;
    }

    private void drawText(
            PDPageContentStream content,
            PDType1Font font,
            float x,
            float y,
            int fontSize,
            String text) throws IOException {

        content.beginText();
        content.setFont(font, fontSize);
        content.newLineAtOffset(x, y);
        content.showText(safeText(text));
        content.endText();
    }

    private String formatAmount(BigDecimal amount) {
        BigDecimal value = amount == null
                ? BigDecimal.ZERO
                : amount;

        return value.setScale(2, RoundingMode.HALF_UP)
                .toPlainString();
    }

    // Standard PDF fonts have limited character support.
    private String safeText(String text) {
        if (text == null) {
            return "N/A";
        }

        return text.replaceAll("[^\\x20-\\x7E]", "?");
    }
}
