package com.halilsahin.leaveflow.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class LogoConverter {
    
    public static void createSimpleLogo() {
        try {
            // 64x64 PNG logo oluştur
            BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();
            
            // Anti-aliasing
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Arka plan daire (yeşil)
            g2d.setColor(new Color(76, 175, 80)); // #4CAF50
            g2d.fillOval(2, 2, 60, 60);
            
            // Daire kenarlığı (koyu yeşil)
            g2d.setColor(new Color(46, 125, 50)); // #2E7D32
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(2, 2, 60, 60);
            
            // Takvim gövdesi (beyaz)
            g2d.setColor(Color.WHITE);
            g2d.fillRoundRect(16, 20, 32, 24, 4, 4);
            
            // Takvim kenarlığı
            g2d.setColor(new Color(46, 125, 50));
            g2d.drawRoundRect(16, 20, 32, 24, 4, 4);
            
            // Takvim başlığı (koyu yeşil)
            g2d.setColor(new Color(46, 125, 50));
            g2d.fillRoundRect(16, 20, 32, 8, 4, 4);
            
            // Takvim delikleri (beyaz)
            g2d.setColor(Color.WHITE);
            g2d.fillOval(23, 23, 2, 2);
            g2d.fillOval(39, 23, 2, 2);
            
            // Takvim çizgileri (gri)
            g2d.setColor(new Color(224, 224, 224));
            g2d.setStroke(new BasicStroke(1));
            g2d.drawLine(20, 32, 44, 32);
            g2d.drawLine(20, 36, 44, 36);
            g2d.drawLine(20, 40, 44, 40);
            
            // İnsan figürü (yeşil)
            g2d.setColor(new Color(76, 175, 80));
            g2d.fillOval(29, 31, 6, 6);
            
            // İnsan gövdesi (yeşil çizgi)
            g2d.setStroke(new BasicStroke(2));
            g2d.drawArc(29, 38, 6, 8, 0, 180);
            
            // Checkmark (beyaz)
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawLine(26, 30, 30, 34);
            g2d.drawLine(30, 34, 38, 26);
            
            g2d.dispose();
            
            // PNG olarak kaydet
            File outputFile = new File("src/main/resources/logo.png");
            ImageIO.write(image, "PNG", outputFile);
            
            System.out.println("Logo başarıyla oluşturuldu: " + outputFile.getAbsolutePath());
            
        } catch (IOException e) {
            System.err.println("Logo oluşturulurken hata: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        createSimpleLogo();
    }
} 