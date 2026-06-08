/* HÀM DÙNG CHUNG*/
document.addEventListener('DOMContentLoaded', function() {
    // Ẩn thông báo sau 5s
    const tudongan = document.getElementById('tudongan');
    if (tudongan) {
        setTimeout(() => {
            tudongan.style.transition = 'opacity 0.5s ease';
            tudongan.style.opacity = '0';
            setTimeout(() => {
                tudongan.style.display = 'none';
            }, 600);
        }, 5000);
    }

    // ==================== HỘP THOẠI XÁC NHẬN XÓA ====================
    const confirmDialog = document.getElementById('confirmDialog');
    const confirmCancel = document.getElementById('confirmCancel');
    const confirmOk = document.getElementById('confirmOk');

    let currentDeleteUrl = '';

    // Hàm hiển thị hộp thoại xác nhận xóa
    window.showConfirmDelete = function(element) {
        const maSP = element.getAttribute('data-maSP');
        const tenModel = element.getAttribute('data-tenModel');

        const message = `Bạn có chắc chắn muốn xóa điện thoại <strong>${tenModel}</strong> (Mã: ${maSP}) không?`;

        document.getElementById('confirmMessage').innerHTML = message;
        confirmDialog.style.display = 'flex';

        currentDeleteUrl = `/products/delete/${maSP}`;
    };

    // Nút Hủy
    if (confirmCancel) {
        confirmCancel.addEventListener('click', function() {
            confirmDialog.style.display = 'none';
        });
    }

    // Nút Xóa
    if (confirmOk) {
        confirmOk.addEventListener('click', function() {
            if (currentDeleteUrl) {
                window.location.href = currentDeleteUrl;
            }
        });
    }
});