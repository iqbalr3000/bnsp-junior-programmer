package com.iqbal.ai;

import com.google.genai.types.FunctionDeclaration;
import com.google.genai.types.Schema;
import com.google.genai.types.Tool;
import com.google.genai.types.Type;
import com.iqbal.model.Dokter;
import com.iqbal.model.Obat;
import com.iqbal.model.Pasien;
import com.iqbal.model.RekamMedis;
import com.iqbal.model.Reservasi;
import com.iqbal.service.DokterService;
import com.iqbal.service.ObatService;
import com.iqbal.service.PasienService;
import com.iqbal.service.RekamMedisService;
import com.iqbal.service.ReservasiService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Dispatch tool read-only lewat switch eksplisit (bukan registry/reflection) — jumlah tool
 * cuma segelintir, konsisten dengan repository layer yang juga menghindari abstraksi generik.
 */
public class AssistantToolExecutor {

    private final PasienService pasienService;
    private final DokterService dokterService;
    private final ObatService obatService;
    private final ReservasiService reservasiService;
    private final RekamMedisService rekamMedisService;

    public AssistantToolExecutor(PasienService pasienService,
                                  DokterService dokterService,
                                  ObatService obatService,
                                  ReservasiService reservasiService,
                                  RekamMedisService rekamMedisService) {
        this.pasienService = pasienService;
        this.dokterService = dokterService;
        this.obatService = obatService;
        this.reservasiService = reservasiService;
        this.rekamMedisService = rekamMedisService;
    }

    public List<Tool> declareTools() {
        return List.of(Tool.builder()
                .functionDeclarations(
                        cariPasienDeclaration(),
                        cariDokterDeclaration(),
                        cekStokObatDeclaration(),
                        obatStokMenipisDeclaration(),
                        reservasiHariIniDeclaration(),
                        reservasiPasienDeclaration(),
                        rekamMedisPasienDeclaration())
                .build());
    }

    public String dispatch(String name, Map<String, Object> args) {
        try {
            return switch (name) {
                case "cari_pasien" -> cariPasien(stringArg(args, "keyword"));
                case "cari_dokter" -> cariDokter(stringArg(args, "keyword"));
                case "cek_stok_obat" -> cekStokObat(stringArg(args, "keyword"));
                case "obat_stok_menipis" -> obatStokMenipis(intArg(args, "threshold", 10));
                case "reservasi_hari_ini" -> reservasiHariIni();
                case "reservasi_pasien" -> reservasiPasien(longArg(args, "pasienId"));
                case "rekam_medis_pasien" -> rekamMedisPasien(longArg(args, "pasienId"));
                default -> "Tool tidak dikenal: " + name;
            };
        } catch (Exception e) {
            return "Gagal mengambil data: " + e.getMessage();
        }
    }

    private String cariPasien(String keyword) {
        List<Pasien> hasil = pasienService.search(keyword);
        if (hasil.isEmpty()) {
            return "Tidak ada pasien ditemukan untuk kata kunci '" + keyword + "'.";
        }
        return hasil.stream()
                .map(p -> "id=%d, no_rekam_medis=%s, nama=%s, nik=%s, tanggal_lahir=%s, telepon=%s".formatted(
                        p.getId(), p.getNoRekamMedis(), p.getNama(), p.getNik(), p.getTanggalLahir(), p.getNoTelepon()))
                .collect(Collectors.joining("\n"));
    }

    private String cariDokter(String keyword) {
        List<Dokter> hasil = dokterService.search(keyword);
        if (hasil.isEmpty()) {
            return "Tidak ada dokter ditemukan untuk kata kunci '" + keyword + "'.";
        }
        return hasil.stream()
                .map(d -> "id=%d, nama=%s, spesialisasi=%s, telepon=%s".formatted(
                        d.getId(), d.getNama(), d.getSpesialisasi(), d.getNoTelepon()))
                .collect(Collectors.joining("\n"));
    }

    private String cekStokObat(String keyword) {
        List<Obat> hasil = obatService.search(keyword);
        if (hasil.isEmpty()) {
            return "Tidak ada obat ditemukan untuk kata kunci '" + keyword + "'.";
        }
        return hasil.stream()
                .map(o -> "kode=%s, nama=%s, stok=%d %s, harga=%s".formatted(
                        o.getKodeObat(), o.getNama(), o.getStok(), o.getSatuan(), o.getHarga()))
                .collect(Collectors.joining("\n"));
    }

    private String obatStokMenipis(int threshold) {
        List<Obat> hasil = obatService.findAll().stream()
                .filter(o -> o.getStok() < threshold)
                .toList();
        if (hasil.isEmpty()) {
            return "Tidak ada obat dengan stok di bawah " + threshold + ".";
        }
        return hasil.stream()
                .map(o -> "nama=%s, stok=%d %s".formatted(o.getNama(), o.getStok(), o.getSatuan()))
                .collect(Collectors.joining("\n"));
    }

    private String reservasiHariIni() {
        LocalDate today = LocalDate.now();
        List<Reservasi> hasil = reservasiService.findAll().stream()
                .filter(r -> today.equals(r.getTanggalReservasi()))
                .toList();
        if (hasil.isEmpty()) {
            return "Tidak ada reservasi untuk hari ini (" + today + ").";
        }
        return hasil.stream()
                .map(r -> "jam=%s, pasien=%s, dokter=%s, status=%s".formatted(
                        r.getJamReservasi(), r.getPasienNama(), r.getDokterNama(), r.getStatus()))
                .collect(Collectors.joining("\n"));
    }

    private String reservasiPasien(long pasienId) {
        List<Reservasi> hasil = reservasiService.findByPasienId(pasienId);
        if (hasil.isEmpty()) {
            return "Tidak ada reservasi untuk pasien id " + pasienId + ".";
        }
        return hasil.stream()
                .map(r -> "tanggal=%s, jam=%s, dokter=%s, status=%s, keluhan=%s".formatted(
                        r.getTanggalReservasi(), r.getJamReservasi(), r.getDokterNama(), r.getStatus(), r.getKeluhan()))
                .collect(Collectors.joining("\n"));
    }

    private String rekamMedisPasien(long pasienId) {
        List<RekamMedis> hasil = rekamMedisService.findAll().stream()
                .filter(rm -> pasienId == rm.getPasienId())
                .toList();
        if (hasil.isEmpty()) {
            return "Tidak ada rekam medis untuk pasien id " + pasienId + ".";
        }
        return hasil.stream()
                .map(rm -> "tanggal=%s, dokter=%s, diagnosis=%s".formatted(
                        rm.getTanggalPeriksa(), rm.getDokterNama(), rm.getDiagnosis()))
                .collect(Collectors.joining("\n"));
    }

    private FunctionDeclaration cariPasienDeclaration() {
        return FunctionDeclaration.builder()
                .name("cari_pasien")
                .description("Mencari data pasien berdasarkan nama, NIK, atau nomor rekam medis.")
                .parameters(Schema.builder()
                        .type(Type.Known.OBJECT)
                        .properties(Map.of("keyword", Schema.builder()
                                .type(Type.Known.STRING)
                                .description("Kata kunci pencarian: nama, NIK, atau nomor rekam medis")
                                .build()))
                        .required("keyword")
                        .build())
                .build();
    }

    private FunctionDeclaration cariDokterDeclaration() {
        return FunctionDeclaration.builder()
                .name("cari_dokter")
                .description("Mencari data dokter berdasarkan nama atau spesialisasi.")
                .parameters(Schema.builder()
                        .type(Type.Known.OBJECT)
                        .properties(Map.of("keyword", Schema.builder()
                                .type(Type.Known.STRING)
                                .description("Kata kunci pencarian: nama atau spesialisasi dokter")
                                .build()))
                        .required("keyword")
                        .build())
                .build();
    }

    private FunctionDeclaration cekStokObatDeclaration() {
        return FunctionDeclaration.builder()
                .name("cek_stok_obat")
                .description("Mengecek stok dan harga obat berdasarkan nama atau kode obat.")
                .parameters(Schema.builder()
                        .type(Type.Known.OBJECT)
                        .properties(Map.of("keyword", Schema.builder()
                                .type(Type.Known.STRING)
                                .description("Kata kunci pencarian: nama atau kode obat")
                                .build()))
                        .required("keyword")
                        .build())
                .build();
    }

    private FunctionDeclaration obatStokMenipisDeclaration() {
        return FunctionDeclaration.builder()
                .name("obat_stok_menipis")
                .description("Mendaftar obat yang stoknya di bawah ambang batas tertentu (default 10).")
                .parameters(Schema.builder()
                        .type(Type.Known.OBJECT)
                        .properties(Map.of("threshold", Schema.builder()
                                .type(Type.Known.INTEGER)
                                .description("Ambang batas stok, default 10 kalau tidak disebutkan")
                                .build()))
                        .build())
                .build();
    }

    private FunctionDeclaration reservasiHariIniDeclaration() {
        return FunctionDeclaration.builder()
                .name("reservasi_hari_ini")
                .description("Mendaftar semua reservasi yang dijadwalkan untuk hari ini.")
                .build();
    }

    private FunctionDeclaration reservasiPasienDeclaration() {
        return FunctionDeclaration.builder()
                .name("reservasi_pasien")
                .description("Mendaftar riwayat reservasi milik satu pasien tertentu berdasarkan id pasien. "
                        + "Panggil cari_pasien dulu kalau id pasien belum diketahui.")
                .parameters(Schema.builder()
                        .type(Type.Known.OBJECT)
                        .properties(Map.of("pasienId", Schema.builder()
                                .type(Type.Known.INTEGER)
                                .description("ID pasien")
                                .build()))
                        .required("pasienId")
                        .build())
                .build();
    }

    private FunctionDeclaration rekamMedisPasienDeclaration() {
        return FunctionDeclaration.builder()
                .name("rekam_medis_pasien")
                .description("Mendaftar riwayat rekam medis milik satu pasien tertentu berdasarkan id pasien. "
                        + "Panggil cari_pasien dulu kalau id pasien belum diketahui.")
                .parameters(Schema.builder()
                        .type(Type.Known.OBJECT)
                        .properties(Map.of("pasienId", Schema.builder()
                                .type(Type.Known.INTEGER)
                                .description("ID pasien")
                                .build()))
                        .required("pasienId")
                        .build())
                .build();
    }

    private static String stringArg(Map<String, Object> args, String key) {
        Object value = args.get(key);
        return value != null ? value.toString() : "";
    }

    private static int intArg(Map<String, Object> args, String key, int defaultValue) {
        Object value = args.get(key);
        return value instanceof Number number ? number.intValue() : defaultValue;
    }

    private static long longArg(Map<String, Object> args, String key) {
        Object value = args.get(key);
        if (!(value instanceof Number number)) {
            throw new IllegalArgumentException("Parameter '" + key + "' wajib berupa angka");
        }
        return number.longValue();
    }
}
