// ========== TOAST NOTIFICATION SYSTEM ==========
function showToast(message, type = 'success') {
    let container = document.querySelector('.toast-container');
    if (!container) {
        container = document.createElement('div');
        container.className = 'toast-container';
        document.body.appendChild(container);
    }
    const icons = { success: 'bi-check-circle-fill', error: 'bi-x-circle-fill', warning: 'bi-exclamation-triangle-fill', info: 'bi-info-circle-fill' };
    const toast = document.createElement('div');
    toast.className = `custom-toast toast-${type}`;
    toast.innerHTML = `<i class="bi ${icons[type] || icons.info}"></i><span>${message}</span>`;
    container.appendChild(toast);
    setTimeout(() => { toast.style.opacity = '0'; toast.style.transform = 'translateX(100px)'; setTimeout(() => toast.remove(), 400); }, 3500);
}

// ========== QUANTITY SELECTOR ==========
function incrementQty(btn) {
    const input = btn.parentElement.querySelector('input[type="number"]');
    const max = parseInt(input.getAttribute('max')) || 999;
    let val = parseInt(input.value) || 0;
    if (val < max) { input.value = val + 1; input.dispatchEvent(new Event('change')); }
}
function decrementQty(btn) {
    const input = btn.parentElement.querySelector('input[type="number"]');
    let val = parseInt(input.value) || 0;
    if (val > 1) { input.value = val - 1; input.dispatchEvent(new Event('change')); }
}

// ========== CART QUANTITY UPDATE ==========
function updateCartItem(productId, quantity) {
    const form = document.createElement('form');
    form.method = 'POST'; form.action = '/cart/update';
    const csrf = document.querySelector('meta[name="_csrf"]');
    if (csrf) { const t = document.createElement('input'); t.type='hidden'; t.name='_csrf'; t.value=csrf.content; form.appendChild(t); }
    const pid = document.createElement('input'); pid.type='hidden'; pid.name='productId'; pid.value=productId; form.appendChild(pid);
    const qty = document.createElement('input'); qty.type='hidden'; qty.name='quantity'; qty.value=quantity; form.appendChild(qty);
    document.body.appendChild(form); form.submit();
}

function confirmDelete(formId) {
    if (confirm('Bạn có chắc chắn muốn xóa?')) { document.getElementById(formId).submit(); }
    return false;
}

function previewImages(input, containerId) {
    const container = document.getElementById(containerId);
    if (!container) return;
    container.innerHTML = '';
    if (input.files) {
        Array.from(input.files).forEach(file => {
            const reader = new FileReader();
            reader.onload = function(e) {
                const img = document.createElement('img');
                img.src = e.target.result;
                img.style.cssText = 'width:80px;height:80px;object-fit:cover;border-radius:8px;margin:4px;';
                container.appendChild(img);
            };
            reader.readAsDataURL(file);
        });
    }
}

function changeMainImage(src) {
    const main = document.querySelector('.product-gallery-main img');
    if (main) main.src = src;
    document.querySelectorAll('.product-gallery-thumbs img').forEach(t => t.classList.remove('active'));
    if (event && event.target) event.target.classList.add('active');
}

function openQuickView(productId) {
    fetch('/api/products/' + productId)
        .then(r => r.json())
        .then(product => {
            const modal = document.getElementById('quickViewModal');
            if (!modal) return;
            modal.querySelector('.qv-image').src = product.imageUrl || 'https://via.placeholder.com/400';
            modal.querySelector('.qv-name').textContent = product.name;
            modal.querySelector('.qv-description').textContent = product.description || '';
            const priceEl = modal.querySelector('.qv-price');
            if (product.salePrice > 0 && product.salePrice < product.price) {
                priceEl.innerHTML = '<span class="text-original">' + formatPrice(product.price) + '</span> <span class="text-sale">' + formatPrice(product.salePrice) + '</span>';
            } else {
                priceEl.innerHTML = '<span class="text-price">' + formatPrice(product.price) + '</span>';
            }
            modal.querySelector('.qv-stock').textContent = product.stockQuantity > 0 ? 'Còn ' + product.stockQuantity + ' sản phẩm' : 'Hết hàng';
            const qvPid = modal.querySelector('.qv-product-id');
            if (qvPid) qvPid.value = productId;
            new bootstrap.Modal(modal).show();
        });
}

function formatPrice(p) { return new Intl.NumberFormat('vi-VN').format(p) + 'đ'; }

function validateRegistration(form) {
    let valid = true;
    const email = form.querySelector('[name="email"]');
    if (email && email.value && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value)) { showFieldError(email, 'Email không đúng định dạng'); valid = false; }
    const phone = form.querySelector('[name="phone"]');
    if (phone && phone.value && !/^[0-9]{10,11}$/.test(phone.value)) { showFieldError(phone, 'SĐT phải là 10-11 chữ số'); valid = false; }
    const password = form.querySelector('[name="password"]');
    if (password && password.value.length < 6) { showFieldError(password, 'Mật khẩu phải ít nhất 6 ký tự'); valid = false; }
    return valid;
}
function showFieldError(input, message) {
    input.classList.add('is-invalid');
    let fb = input.nextElementSibling;
    if (!fb || !fb.classList.contains('invalid-feedback')) { fb = document.createElement('div'); fb.className = 'invalid-feedback'; input.parentNode.insertBefore(fb, input.nextSibling); }
    fb.textContent = message;
}

document.addEventListener('DOMContentLoaded', function() {
    document.querySelectorAll('.auto-toast').forEach(el => {
        const msg = el.dataset.message; const type = el.dataset.type || 'success';
        if (msg) showToast(msg, type);
    });
    document.querySelectorAll('.cart-qty-form').forEach(form => {
        form.querySelector('input[type="number"]')?.addEventListener('change', function() {
            const pid = this.dataset.productId; if (pid) updateCartItem(pid, this.value);
        });
    });
    const regForm = document.getElementById('registrationForm');
    if (regForm) regForm.addEventListener('submit', function(e) { if (!validateRegistration(this)) e.preventDefault(); });
    document.querySelectorAll('.form-control').forEach(i => { i.addEventListener('input', () => i.classList.remove('is-invalid')); });
    document.querySelectorAll('.status-form').forEach(f => { f.addEventListener('submit', function(e) { if (!confirm('Thay đổi trạng thái đơn hàng?')) e.preventDefault(); }); });
});
