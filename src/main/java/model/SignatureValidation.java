/*
 *   Copyright 2015 Luís Diogo Zambujo, Micael Sousa Farinha and Miguel Frade
 *
 *   This file is part of aCCinaPDF.
 *
 *   aCCinaPDF is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   aCCinaPDF is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with aCCinaPDF.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package model;

import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.security.CertificateVerification;
import com.itextpdf.text.pdf.security.PdfPKCS7;
import com.itextpdf.text.pdf.security.SignaturePermissions;
import controller.Bundle;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import listener.SignatureClickListener;
import org.apache.commons.lang3.text.WordUtils;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;

/**
 *
 * @author Toshiba
 */
public class SignatureValidation {

    private final String filename;
    private final String name;
    private final PdfPKCS7 pdfPkcs7;
    private final boolean changed;
    private final boolean coversEntireDocument;
    private final int revision, numRevisions;
    private final CertificateStatus ocspCertificateStatus;
    private final CertificateStatus crlCertificateStatus;
    private final boolean validTimeStamp;
    private final List<AcroFields.FieldPosition> posList;
    private final JPanel panel;
    private SignatureClickListener listener;
    private final SignaturePermissions signaturePermissions;
    private final boolean valid;

    public SignatureValidation(String filename, String name, PdfPKCS7 pdfPkcs7, boolean changed, boolean coversEntireDocument, int revision, int numRevisions, int documentCertificationLevel, CertificateStatus ocspCertificateStatus, CertificateStatus crlCertificateStatus, boolean validTimeStamp, List<AcroFields.FieldPosition> posList, SignaturePermissions signaturePermissions, boolean valid) {
        this.filename = filename;
        this.name = name;
        this.pdfPkcs7 = pdfPkcs7;
        this.changed = changed;
        this.coversEntireDocument = coversEntireDocument;
        this.revision = revision;
        this.numRevisions = numRevisions;
        this.signaturePermissions = signaturePermissions;
        this.valid = valid;
        this.ocspCertificateStatus = ocspCertificateStatus;
        this.crlCertificateStatus = crlCertificateStatus;
        this.validTimeStamp = validTimeStamp;
        this.posList = posList;
        this.panel = new JPanel();

        this.panel.setBackground(new Color(0, 0, 0, 0));
        this.panel.setToolTipText(name);
        this.panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (SwingUtilities.isLeftMouseButton(evt)) {
                    if (listener != null) {
                        listener.onSignatureClick(SignatureValidation.this);
                    }
                }
            }
        });
    }

    public boolean isValid() {
        return valid;
    }

    public SignaturePermissions getSignaturePermissions() {
        return signaturePermissions;
    }

    public int getNumRevisions() {
        return numRevisions;
    }

    public void setListener(SignatureClickListener listener) {
        this.listener = listener;
    }

    public JPanel getPanel() {
        return panel;
    }

    public List<AcroFields.FieldPosition> getPosList() {
        return posList;
    }

    public String getFilename() {
        return filename;
    }

    public CertificateStatus getOcspCertificateStatus() {
        return ocspCertificateStatus;
    }

    public CertificateStatus getCrlCertificateStatus() {
        return crlCertificateStatus;
    }

    public boolean isValidTimeStamp() {
        return validTimeStamp;
    }

    public String getName() {
        return name;
    }

    public PdfPKCS7 getSignature() {
        return pdfPkcs7;
    }

    public boolean isChanged() {
        return changed || !coversEntireDocument;
    }

    public boolean isCoversEntireDocument() {
        return coversEntireDocument;
    }

    public int getRevision() {
        return revision;
    }

    public boolean isCertification() {
        return (signaturePermissions.isCertification());
    }

    public boolean isWarning() {
        return (getOcspCertificateStatus().equals(CertificateStatus.UNCHECKED) && getCrlCertificateStatus().equals(CertificateStatus.UNCHECKED));
    }

    public String getSignerName() {
        X509Certificate x509cert = (X509Certificate) pdfPkcs7.getSigningCertificate();
        org.bouncycastle.asn1.x500.X500Name x500name = null;
        try {
            x500name = new JcaX509CertificateHolder(x509cert).getSubject();
        } catch (CertificateEncodingException ex) {
            return Bundle.getBundle().getString("unknown");
        }
        RDN rdn = x500name.getRDNs(BCStyle.CN)[0];
        return WordUtils.capitalize(IETFUtils.valueToString(rdn.getFirst().getValue()).toLowerCase());
    }

    private boolean isVisible() {
        return ((this.posList.get(0).position.getWidth() != 0) && (this.posList.get(0).position.getHeight() != 0));
    }

    @Override
    public String toString() {
        return name + (this.isVisible() ? "" : " (" + Bundle.getBundle().getString("invisible") + ")");
    }

}
