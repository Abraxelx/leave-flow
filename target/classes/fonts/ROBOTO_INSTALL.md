# Roboto Fontu Kurulum Talimatları

## Manuel İndirme
1. https://fonts.google.com/specimen/Roboto adresine git
2. Sağ üstteki "Download family" butonuna tıkla
3. İndirilen zip dosyasını aç
4. `static/Roboto-Regular.ttf` dosyasını bu klasöre kopyala

## Alternatif: GitHub'dan İndirme
```bash
# GitHub'dan Roboto fontunu indir
curl -L "https://github.com/google/fonts/raw/main/apache/roboto/Roboto-Regular.ttf" -o src/main/resources/fonts/Roboto-Regular.ttf
```

## Font Özellikleri
- **Aile:** Roboto
- **Stil:** Regular
- **Ağırlık:** 400
- **Türkçe Desteği:** Tam (ş, ç, ğ, ü, ö, ı, İ)
- **Lisans:** Apache License 2.0

## Test
Font kurulduktan sonra PDF export özelliği otomatik olarak Roboto fontunu kullanacak. 