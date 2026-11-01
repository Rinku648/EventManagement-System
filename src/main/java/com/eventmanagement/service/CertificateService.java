package com.eventmanagement.service;

import com.eventmanagement.dao.CertificateDAO;
import com.eventmanagement.model.Certificate;
import com.eventmanagement.util.PdfGenerator;
import com.eventmanagement.util.TransactionIdGenerator;

public class CertificateService {
    private final CertificateDAO certificateDAO = new CertificateDAO();

    public String issueCertificate(int attendeeId, int eventId, String attendeeName, String eventName) throws Exception {
        String certNumber = TransactionIdGenerator.generateCertificateNumber();
        String fileName = "certificate_" + attendeeId + "_" + eventId;
        String pdfPath = PdfGenerator.generateCertificatePdf(fileName, attendeeName, eventName, certNumber);

        Certificate c = new Certificate();
        c.setAttendeeId(attendeeId);
        c.setEventId(eventId);
        c.setCertificateNumber(certNumber);
        c.setPdfPath(pdfPath);
        certificateDAO.createCertificate(c);
        return pdfPath;
    }
}