# Fonts Klasörü

Bu klasör PDF export için kullanılan fontları içerir.

## Çift Font Sistemi

PDF raporlarında farklı kısımlar için farklı fontlar kullanılır:

### 1. Noto Sans Fontu (Ana Font)
- **Dosya:** NotoSansSymbols2-Regular.ttf
- **Kullanım:** Metin, başlıklar, Türkçe karakterler
- **Özellik:** Simge desteği mükemmel (✓, ✗, ☑, ✘, vb.)
- **Türkçe Desteği:** Tam destek (ş, ç, ğ, ü, ö, ı, İ)

### 2. Roboto Light Fontu (Sayısal Font)
- **Dosya:** Roboto-Light.ttf
- **Kullanım:** Sayılar, tarihler, ID'ler
- **Özellik:** Sayılar için optimize edilmiş, daha ince ve okunaklı
- **Avantaj:** Sayısal veriler daha net görünür

## Font Kullanım Alanları

### Noto Sans (Ana Font) Kullanılan Yerler:
- Başlıklar ("İzin Raporu", "İstatistikler")
- Tablo başlıkları ("Çalışan", "İzin Türü", "Süre")
- Metin içerikleri (çalışan isimleri, açıklamalar)
- Simgeler ("✓ Sayıldı", "✗ Sayılmadı")

### Roboto Light (Sayısal Font) Kullanılan Yerler:
- ID numaraları
- Tarihler (dd.MM.yyyy formatında)
- Sayısal değerler (izin günleri, kalan günler)
- İstatistikler (toplam, ortalama, sayılar)

## Fallback Sistemi
Eğer fontlar yüklenemezse:
1. **Courier New** (monospace font)
2. **Consolas** (monospace font)
3. **Arial Unicode MS** (sistem fontu)
4. **Helvetica** (sistem fontu)
5. **Varsayılan font** (son çare)

## Kullanım
PDF export servisinde bu fontlar otomatik olarak kullanılır.
Sayısal veriler Roboto Light ile, metinler Noto Sans ile yazdırılır. 