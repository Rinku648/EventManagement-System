package com.eventmanagement.util;

import java.util.UUID;

public class TransactionIdGenerator {
    public static String generate() { return "TXN-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase(); }
    public static String generateInvoiceNumber() { return "INV-" + System.currentTimeMillis(); }
    public static String generateCertificateNumber() { return "CERT-" + System.currentTimeMillis(); }
}