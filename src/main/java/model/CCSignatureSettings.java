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

import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;
import controller.Bundle;
import controller.CCInstance;
import java.awt.Color;
import java.awt.Image;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import view.AppearanceSettingsDialog;
import view.InitialConfigDialog;

/**
 *
 * @author Diogo
 */
public final class CCSignatureSettings {

    private CCAlias ccAlias;
    private boolean visibleSignature;
    private boolean timestamp;
    private String timestampServer;
    private boolean ocspClient;
    private String reason;
    private String location;
    private int certificationLevel;
    private String text;
    private Rectangle positionOnDocument;
    private int pageNumber;
    private AppearanceSettings appearance;
    private String prefix;
    private boolean createdNewSettings;

    private static final String SETTINGS_FILE = "aCCinaPDF.cfg";

    public CCSignatureSettings(boolean forceCreateConfigFile) {
        CCInstance.newIstance();
        appearance = new AppearanceSettings();
        if (!new File(SETTINGS_FILE).exists() || forceCreateConfigFile) {
            if (!new File(SETTINGS_FILE).exists()) {
                createdNewSettings = true;
            }
            createConfigFile();
        }
        try {
            String languageStr = getConfigParameter("language");
            String pdfVersionStr = getConfigParameter("pdfversion");
            String renderQualityStr = getConfigParameter("renderQuality");
            String prefixStr = getConfigParameter("prefix");
            String boldStr = getConfigParameter("fontBold");
            String italicStr = getConfigParameter("fontItalic");
            String fontLocationStr = getConfigParameter("fontLocation");
            String showNameStr = getConfigParameter("showName");
            String showDateStr = getConfigParameter("showDate");
            String showReasonStr = getConfigParameter("showReason");
            String showLocationStr = getConfigParameter("showLocation");
            String fontColorStr = getConfigParameter("fontColor");
            String textAlignStr = getConfigParameter("textAlign");

            if (languageStr == null || pdfVersionStr == null || renderQualityStr == null || prefixStr == null || boldStr == null || italicStr == null || fontLocationStr == null
                    || showNameStr == null || showDateStr == null || showReasonStr == null || showLocationStr == null || fontColorStr == null || textAlignStr == null) {
                new File(SETTINGS_FILE).delete();
                createConfigFile();
                return;
            }

            switch (languageStr) {
                case "en-US":
                    Bundle.getBundle().setCurrentLocale(Bundle.Locales.English);
                    break;
                case "pt-PT":
                    Bundle.getBundle().setCurrentLocale(Bundle.Locales.Portugues);
                    break;
                default:
                    Bundle.getBundle().setCurrentLocale(Bundle.Locales.English);
            }

            String keystoreStr = getConfigParameter("keystore");
            if (keystoreStr == null) {
                CCInstance.getInstance().setKeystore(CCInstance.getInstance().getDefaultKeystore());
                Settings.getSettings().setKeystorePath(null);
            } else if (keystoreStr.isEmpty()) {
                CCInstance.getInstance().setKeystore(CCInstance.getInstance().getDefaultKeystore());
                Settings.getSettings().setKeystorePath(null);
            } else {
                KeyStore ks = isValidKeystore(new File(keystoreStr));
                if (ks == null) {
                    CCInstance.getInstance().setKeystore(CCInstance.getInstance().getDefaultKeystore());
                    Properties properties = new Properties();
                    String configFile = "aCCinaPDF.cfg";
                    properties.load(new FileInputStream(configFile));
                    properties.remove("keystore");
                    Settings.getSettings().setKeystorePath(null);
                    JOptionPane.showMessageDialog(null, Bundle.getBundle().getString("usingDefaultKeystoreError"), "", JOptionPane.WARNING_MESSAGE);
                    try {
                        FileOutputStream fileOut = new FileOutputStream(configFile);
                        properties.store(fileOut, "Settings");
                        fileOut.close();
                    } catch (IOException ex) {
                    }
                } else {
                    CCInstance.getInstance().setKeystore(ks);
                    Settings.getSettings().setKeystorePath(keystoreStr);
                }
            }
            Settings.getSettings().setPdfVersion(pdfVersionStr);
            Settings.getSettings().setRenderImageQuality(Integer.valueOf(renderQualityStr));
            setPrefix(prefixStr);
            appearance.setBold(Boolean.valueOf(boldStr));
            appearance.setItalic(Boolean.valueOf(italicStr));
            appearance.setFontLocation(fontLocationStr);
            appearance.setShowName(Boolean.valueOf(showNameStr));
            appearance.setShowDate(Boolean.valueOf(showDateStr));
            appearance.setShowReason(Boolean.valueOf(showReasonStr));
            appearance.setShowLocation(Boolean.valueOf(showLocationStr));
            appearance.setFontColor(new Color(Integer.valueOf(fontColorStr)));
            appearance.setAlign(Integer.valueOf(textAlignStr));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void createConfigFile() {
        InitialConfigDialog icd = new InitialConfigDialog(null, true);
        icd.setLocationRelativeTo(null);
        icd.setVisible(true);
        Locale locale = icd.getSelectedLocale();
        String fsettings = "aCCinaPDF.cfg";
        Properties propertiesWrite = new Properties();
        FileOutputStream fileOut;
        try {
            if (locale.equals(Bundle.getBundle().getLocale(Bundle.Locales.Portugues))) {
                propertiesWrite.setProperty("language", "pt-PT");
                Bundle.getBundle().setCurrentLocale(Bundle.Locales.Portugues);
            } else if (locale.equals(Bundle.getBundle().getLocale(Bundle.Locales.English))) {
                propertiesWrite.setProperty("language", "en-US");
                Bundle.getBundle().setCurrentLocale(Bundle.Locales.English);
            } else {
                propertiesWrite.setProperty("language", "en-US");
                Bundle.getBundle().setCurrentLocale(Bundle.Locales.English);
            }
            propertiesWrite.setProperty("renderQuality", String.valueOf(2));
            propertiesWrite.setProperty("pdfversion", "/1.7");
            propertiesWrite.setProperty("prefix", "aCCinatura");
            propertiesWrite.setProperty("fontLocation", "extrafonts" + File.separator + "verdana.ttf");
            propertiesWrite.setProperty("fontItalic", "true");
            propertiesWrite.setProperty("fontBold", "false");
            propertiesWrite.setProperty("textAlign", "0");
            propertiesWrite.setProperty("showName", "true");
            propertiesWrite.setProperty("showReason", "true");
            propertiesWrite.setProperty("showLocation", "true");
            propertiesWrite.setProperty("showDate", "true");
            propertiesWrite.setProperty("fontColor", "0");
            propertiesWrite.setProperty("signatureWidth", "403");
            propertiesWrite.setProperty("signatureHeight", "34");
            propertiesWrite.setProperty("pdfversion", String.valueOf(PdfWriter.PDF_VERSION_1_7));
            fileOut = new FileOutputStream(fsettings);
            propertiesWrite.store(fileOut, "Settings");
            fileOut.close();
            loadDefaults();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(AppearanceSettingsDialog.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AppearanceSettingsDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void loadDefaults() {
        Bundle.getBundle().setCurrentLocale(Bundle.Locales.Portugues);
        Settings.getSettings().setPdfVersion("/1.7");
        Settings.getSettings().setRenderImageQuality(Image.SCALE_SMOOTH);
        CCInstance.getInstance().setKeystore(CCInstance.getInstance().getDefaultKeystore());
        setPrefix("aCCinatura");
        appearance.setBold(false);
        appearance.setItalic(true);
        appearance.setFontLocation("extrafonts" + File.separator + "verdana.ttf");
        appearance.setShowName(true);
        appearance.setShowDate(true);
        appearance.setShowReason(true);
        appearance.setShowLocation(true);
        appearance.setFontColor(new Color(0));
        appearance.setAlign(0);

        if (!createdNewSettings) {
            JOptionPane.showMessageDialog(null, Bundle.getBundle().getString("configFileCorrupted"), "", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private KeyStore isValidKeystore(File file) {
        FileInputStream is = null;
        try {
            is = new FileInputStream(file);
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(is, null);
            if (ks.aliases().hasMoreElements()) {
                return ks;
            } else {
                return ks;
            }
        } catch (java.security.cert.CertificateException | NoSuchAlgorithmException | KeyStoreException | FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            return null;
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    private String getConfigParameter(String parameter) throws FileNotFoundException, IOException {
        Properties propertiesRead = new Properties();
        String configFile = "aCCinaPDF.cfg";
        propertiesRead.load(new FileInputStream(configFile));
        String value = propertiesRead.getProperty(parameter);
        return value;
    }

    public boolean isCreatedNewSettings() {
        return createdNewSettings;
    }

    public void setCreatedNewSettings(boolean createdNewSettings) {
        this.createdNewSettings = createdNewSettings;
    }

    public AppearanceSettings getAppearance() {
        return appearance;
    }

    public void setAppearance(AppearanceSettings appearance) {
        this.appearance = appearance;
    }

    public boolean isOcspClient() {
        return ocspClient;
    }

    public void setOcspClient(boolean ocspClient) {
        this.ocspClient = ocspClient;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public Rectangle getPositionOnDocument() {
        return positionOnDocument;
    }

    public void setSignaturePositionOnDocument(Rectangle positionOnDocument) {
        this.positionOnDocument = positionOnDocument;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTimestampServer() {
        return timestampServer;
    }

    public void setTimestampServer(String timestampServer) {
        this.timestampServer = timestampServer;
    }

    public CCAlias getCcAlias() {
        return ccAlias;
    }

    public void setCcAlias(CCAlias ccAlias) {
        this.ccAlias = ccAlias;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getCertificationLevel() {
        return certificationLevel;
    }

    public void setCertificationLevel(int certificationLevel) {
        this.certificationLevel = certificationLevel;
    }

    public boolean isTimestamp() {
        return timestamp;
    }

    public void setTimestamp(boolean timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isVisibleSignature() {
        return visibleSignature;
    }

    public void setVisibleSignature(boolean visibleSignature) {
        this.visibleSignature = visibleSignature;
    }
}
