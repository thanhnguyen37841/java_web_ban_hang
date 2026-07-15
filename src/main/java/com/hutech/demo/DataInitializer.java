package com.hutech.demo;

import com.hutech.demo.model.*;
import com.hutech.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final IRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;

    @Override
    public void run(String... args) {
        // Tạo role ADMIN nếu chưa có
        RoleEntity adminRole = roleRepository.findByName("ADMIN");
        if (adminRole == null) {
            adminRole = new RoleEntity(null, "ADMIN", "Quản trị viên");
            roleRepository.save(adminRole);
        }

        // Tạo role USER nếu chưa có
        RoleEntity userRole = roleRepository.findByName("USER");
        if (userRole == null) {
            userRole = new RoleEntity(null, "USER", "Người dùng");
            roleRepository.save(userRole);
        }

        // Tạo tài khoản admin nếu chưa có
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@hutech.edu.vn");
            admin.setPhone("0123456789");
            admin.setFullName("Quản trị viên");
            admin.getRoles().add(adminRole);
            userRepository.save(admin);

            System.out.println("=== Đã tạo tài khoản admin mặc định ===");
            System.out.println("Username: admin");
            System.out.println("Password: admin123");
            System.out.println("==========================================");
        }

        // Tạo tài khoản user mẫu
        if (userRepository.findByUsername("user1").isEmpty()) {
            User user1 = new User();
            user1.setUsername("user1");
            user1.setPassword(passwordEncoder.encode("user123"));
            user1.setEmail("user1@email.com");
            user1.setPhone("0987654321");
            user1.setFullName("Nguyễn Văn A");
            user1.setAddress("123 Nguyễn Huệ, Q1, TP.HCM");
            user1.getRoles().add(userRole);
            userRepository.save(user1);
        }

        // Tạo danh mục mẫu
        if (categoryRepository.count() == 0) {
            Category cat1 = new Category(); cat1.setName("Bút viết"); categoryRepository.save(cat1);
            Category cat2 = new Category(); cat2.setName("Tập vở - Sổ tay"); categoryRepository.save(cat2);
            Category cat3 = new Category(); cat3.setName("Giấy in"); categoryRepository.save(cat3);
            Category cat4 = new Category(); cat4.setName("Dụng cụ học tập"); categoryRepository.save(cat4);
            Category cat5 = new Category(); cat5.setName("Dụng cụ văn phòng"); categoryRepository.save(cat5);

            // Tạo sản phẩm mẫu
            createProduct("Bút bi Thiên Long", 5000, 4000, "Bút bi Thiên Long chính hãng, viết trơn, mực đều đặn", "Thiên Long", cat1, 500, true, "/Images/ButBi.jpg");
            createProduct("Bút máy cao cấp", 150000, 120000, "Bút máy ngòi trơn, thiết kế sang trọng", "Hồng Hà", cat1, 30, true, "/Images/Butmay.jpg");
            createProduct("Bút chì gỗ", 4000, 3000, "Bút chì gỗ loại 2B, dễ gọt, khó gãy", "Deli", cat1, 250, false, "/Images/butchi.jpg");
            createProduct("Bút dạ quang", 12000, 10000, "Bút dạ quang đánh dấu, màu sắc tươi sáng", "Thiên Long", cat1, 150, true, "/Images/butdaquang.jpg");
            createProduct("Bút gel", 8000, 0, "Bút gel mực nước, viết êm ái", "Deli", cat1, 400, false, "/Images/butgel.jpg");

            createProduct("Tập vở sinh viên 96 trang", 10000, 8000, "Tập vở giấy dày, trắng tự nhiên, chống loá", "Hồng Hà", cat2, 350, true, "/Images/TapVo.jpg");
            createProduct("Sổ tay lò xo", 45000, 0, "Sổ tay lò xo bìa cứng, tiện lợi ghi chép", "PGrand", cat2, 120, false, "/Images/so-lo-xo-pgrand.jpg");
            createProduct("Sổ bìa da cao cấp", 85000, 75000, "Sổ bìa da sang trọng, giấy kem ngà", "Deli", cat2, 45, false, "/Images/sotay1.jpg");

            createProduct("Giấy A4 Double A", 85000, 80000, "Giấy in A4 định lượng 70gsm, 500 tờ/ream", "Double A", cat3, 200, true, "/Images/giay_a4_green.jpg");
            createProduct("Giấy in ảnh A4", 120000, 0, "Giấy in ảnh bóng cao cấp", "Epson", cat3, 60, false, "/Images/giay_anh_a4.jpg");

            createProduct("Compa học sinh", 25000, 20000, "Compa kim loại chắc chắn, kèm ngòi chì", "Deli", cat4, 80, true, "/Images/compa.jpg");
            createProduct("Gọt chì Deli", 15000, 0, "Gọt chì mini hình thú ngộ nghĩnh", "Deli", cat4, 150, false, "/Images/gotchi_deli.jpg");
            createProduct("Thước kẻ nhựa 20cm", 5000, 0, "Thước kẻ học sinh trong suốt", "Thiên Long", cat4, 300, false, "/Images/thuocnhua.jpg");
            createProduct("Tẩy / Gôm học sinh", 6000, 5000, "Gôm tẩy sạch, ít bụi", "Deli", cat4, 200, false, "/Images/Tay.jpg");

            createProduct("Dập ghim số 10", 35000, 30000, "Dập ghim mini, tiện lợi văn phòng", "Deli", cat5, 100, true, "/Images/dapghim.jpg");
            createProduct("Ghim bấm hộp 1000 kim", 12000, 10000, "Kim bấm không gỉ", "Deli", cat5, 200, false, "/Images/ghim_1000.jpg");
            createProduct("Kéo cắt giấy", 25000, 22000, "Kéo inox, tay cầm bọc nhựa", "Deli", cat5, 120, true, "/Images/keo.jpg");

            System.out.println("=== Đã tạo dữ liệu sản phẩm văn phòng phẩm mẫu ===");
        }

        // Tạo mã giảm giá mẫu
        if (couponRepository.count() == 0) {
            Coupon coupon1 = new Coupon();
            coupon1.setCode("WELCOME10");
            coupon1.setDiscountType("PERCENT");
            coupon1.setDiscountValue(10);
            coupon1.setMinOrderAmount(200000);
            coupon1.setMaxUsage(100);
            coupon1.setActive(true);
            coupon1.setStartDate(LocalDateTime.now());
            coupon1.setEndDate(LocalDateTime.now().plusMonths(3));
            couponRepository.save(coupon1);

            Coupon coupon2 = new Coupon();
            coupon2.setCode("SALE50K");
            coupon2.setDiscountType("FIXED");
            coupon2.setDiscountValue(50000);
            coupon2.setMinOrderAmount(500000);
            coupon2.setMaxUsage(50);
            coupon2.setActive(true);
            coupon2.setStartDate(LocalDateTime.now());
            coupon2.setEndDate(LocalDateTime.now().plusMonths(1));
            couponRepository.save(coupon2);


            System.out.println("=== Đã tạo mã giảm giá mẫu: WELCOME10, SALE50K ===");
        }
    }

    private void createProduct(String name, double price, double salePrice,
                                String description, String brand, Category category,
                                int stock, boolean featured, String imageUrl) {
        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setSalePrice(salePrice);
        product.setDescription(description);
        product.setBrand(brand);
        product.setCategory(category);
        product.setStockQuantity(stock);
        product.setFeatured(featured);
        product.setActive(true);
        product.setImageUrl(imageUrl);
        productRepository.save(product);
    }
}
