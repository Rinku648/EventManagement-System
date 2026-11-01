package com.eventmanagement.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class QRCodeGenerator {
    public static String generateQrImage(String payload, String fileNameWithoutExt) throws WriterException, IOException {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(payload, BarcodeFormat.QR_CODE, 250, 250);
        File dir = new File("generated/qrcodes");
        if (!dir.exists()) dir.mkdirs();
        Path path = Path.of(dir.getPath(), fileNameWithoutExt + ".png");
        MatrixToImageWriter.writeToPath(matrix, "PNG", path);
        return path.toString();
    }
}
