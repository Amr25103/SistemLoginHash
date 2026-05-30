import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class SistemLoginHash {
    // Menentukan nama file penyimpanan berbasis berkas teks TXT
    private static final String FILE_NAME = "database_user.txt";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\n=== SISTEM LOGIN SEDERHANA (STORAGE: TXT) ===");
            System.out.println("1. Registrasi Akun");
            System.out.println("2. Login Pengguna");
            System.out.println("3. Keluar");
            System.out.print("Pilih menu (1-3): ");
            
            String pilihan = scanner.nextLine();
            switch (pilihan) {
                case "1":
                    registrasi(scanner);
                    break;
                case "2":
                    login(scanner);
                    break;
                case "3":
                    System.out.println("Program selesai. Sampai jumpa!");
                    scanner.close();
                    return;
                default:
                    System.out.println("Pilihan tidak valid! Silakan pilih menu 1, 2, atau 3.");
            }
        }
    }

    private static void registrasi(Scanner scanner) {
        System.out.println("\n--- MENU REGISTRASI AKUN ---");
        System.out.print("Masukkan Username baru: ");
        String username = scanner.nextLine().trim();

        if (username.isEmpty()) {
            System.out.println("Username tidak boleh kosong!");
            return;
        }

        // Validasi apakah username sudah terdaftar di dalam file TXT
        if (cekUserEksis(username)) {
            System.out.println("Gagal Registrasi! Username '" + username + "' sudah digunakan.");
            return;
        }

        System.out.print("Masukkan Password baru: ");
        String passwordAsli = scanner.nextLine();

        if (passwordAsli.isEmpty()) {
            System.out.println("Password tidak boleh kosong!");
            return;
        }

        // Proses enkripsi searah (Hashing) MD5 dan SHA-256 sesuai ketentuan tugas
        String hashMD5 = hitungHash(passwordAsli, "MD5");
        String hashSHA256 = hitungHash(passwordAsli, "SHA-256");

        // Menampilkan informasi parameter hashing ke layar console (Ketentuan No. 4)
        System.out.println("\n[PROSES ENKRIPSI REGISTRASI]");
        System.out.println("-> Password Asli       : " + passwordAsli);
        System.out.println("-> Hasil Hash MD5      : " + hashMD5);
        System.out.println("-> Hasil Hash SHA-256  : " + hashSHA256);

        // Mekanisme penyimpanan ke dalam file TXT secara berlanjut (append)
        try (FileWriter fw = new FileWriter(FILE_NAME, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            
            // Format baris data di dalam TXT: username,hash_md5,hash_sha256
            out.println(username + "," + hashMD5 + "," + hashSHA256);
            System.out.println("\nRegistrasi Berhasil! Data akun telah disimpan ke file '" + FILE_NAME + "'.");
            
        } catch (IOException e) {
            System.out.println("Gagal menulis data ke file TXT: " + e.getMessage());
        }
    }

    private static void login(Scanner scanner) {
        System.out.println("\n--- MENU LOGIN PENGGUNA ---");
        System.out.print("Masukkan Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Masukkan Password: ");
        String passwordInput = scanner.nextLine();

        System.out.println("\n--- PROSES VERIFIKASI AUTENTIKASI ---");
        System.out.println("1. Menghitung nilai hash dari password yang Anda input...");
        String hashInputMD5 = hitungHash(passwordInput, "MD5");
        String hashInputSHA256 = hitungHash(passwordInput, "SHA-256");
        
        System.out.println("   - Input Teks Password : " + passwordInput);
        System.out.println("   - Hasil Hash MD5      : " + hashInputMD5);
        System.out.println("   - Hasil Hash SHA-256  : " + hashInputSHA256);

        System.out.println("2. Membuka dan membaca data dari file '" + FILE_NAME + "'...");
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            System.out.println("Gagal Login! Belum ada data akun yang terdaftar di sistem.");
            return;
        }

        boolean loginSukses = false;

        try (FileReader fr = new FileReader(file);
             BufferedReader br = new BufferedReader(fr)) {
            
            String baris;
            // Membaca file berkas teks baris demi baris hingga selesai
            while ((baris = br.readLine()) != null) {
                String[] data = baris.split(",");
                if (data.length >= 3) {
                    String dbUsername = data[0];
                    String dbHashMD5 = data[1];
                    String dbHashSHA256 = data[2];

                    // Jika username cocok, lakukan pencocokan nilai hash password
                    if (dbUsername.equalsIgnoreCase(username)) {
                        System.out.println("   [User Ditemukan!] Mencocokkan tanda hash digital...");
                        System.out.println("   - Hash MD5 di TXT      : " + dbHashMD5);
                        System.out.println("   - Hash SHA-256 di TXT  : " + dbHashSHA256);

                        // Memverifikasi apakah hash dari input sama dengan hash di file TXT
                        if (dbHashMD5.equals(hashInputMD5) && dbHashSHA256.equals(hashInputSHA256)) {
                            loginSukses = true;
                            break;
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Gagal membaca file data TXT: " + e.getMessage());
        }

        System.out.println("\n3. Kesimpulan Verifikasi Sistem:");
        if (loginSukses) {
            System.out.println(">>> LOGIN BERHASIL! Selamat Datang Kembali, " + username + " <<<");
        } else {
            System.out.println(">>> LOGIN GAGAL! Username tidak ditemukan atau Password salah. <<<");
        }
    }

    private static boolean cekUserEksis(String username) {
        File file = new File(FILE_NAME);
        if (!file.exists()) return false;

        try (FileReader fr = new FileReader(file);
             BufferedReader br = new BufferedReader(fr)) {
            String baris;
            while ((baris = br.readLine()) != null) {
                String[] data = baris.split(",");
                if (data.length > 0 && data[0].equalsIgnoreCase(username)) {
                    return true;
                }
            }
        } catch (IOException e) {
            // Mengembalikan nilai false jika terjadi kendala akses berkas
        }
        return false;
    }

    private static String hitungHash(String input, String algoritma) {
        try {
            MessageDigest md = MessageDigest.getInstance(algoritma);
            byte[] hashInBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));

            // Konversi susunan data byte ke format string Heksadesimal (Hex)
            StringBuilder sb = new StringBuilder();
            for (byte b : hashInBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Kesalahan: Algoritma " + algoritma + " tidak didukung!", e);
        }
    }
}