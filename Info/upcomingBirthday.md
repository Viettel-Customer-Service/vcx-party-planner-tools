Bước 1: Thay thế mã HTML
Tìm đến khối <div> chứa chữ "Danh Sách Nhân Viên" (bên trong employee-toolbar). Xóa nó đi và dán đoạn code này vào:

HTML
<div class="birthday-ticker-wrap d-flex align-items-center" style="width: 60%; background-color: #fff5f5; border: 1px solid #ffcccc; border-radius: 50px; padding: 6px 16px; overflow: hidden;">

    <div class="ticker-badge d-flex align-items-center" style="position: relative; z-index: 2; background-color: #fff5f5; padding-right: 15px;">
        <i class="bi bi-cake2-fill text-danger mr-2"></i>
        <strong class="text-dark">Sinh nhật sắp tới:</strong>
    </div>
    
    <div class="ticker-viewport" style="flex-grow: 1; overflow: hidden;">
        <div class="ticker-track" id="upcomingBirthdayTicker">
            </div>
    </div>
</div>
(Lưu ý: Bạn nhớ xóa hẳn cái bảng upcomingBirthdayBlock cũ ở bên dưới đi nhé).

Bước 2: Thêm CSS để tạo hiệu ứng tự động chạy ngang (Auto-scroll)
Mở file CSS của bạn ra và dán đoạn này vào cuối file. Đoạn này sẽ làm cho dòng chữ chạy liên tục từ phải sang trái và tạm dừng lại khi bạn di chuột vào để dễ đọc.

CSS
/* Đường ray để chữ chạy */
.ticker-track {
display: inline-block;
white-space: nowrap;
padding-left: 100%; /* Bắt đầu chạy từ tít mép lề phải */
animation: scrollTicker 20s linear infinite; /* 20s là thời gian chạy, tăng lên thì chạy chậm lại */
}

/* Tạm dừng chạy khi di chuột vào */
.ticker-track:hover {
animation-play-state: paused;
cursor: default;
}

/* Định dạng từng người trong danh sách */
.ticker-item {
display: inline-block;
margin-right: 50px; /* Khoảng cách giữa 2 nhân viên */
font-size: 0.95rem;
color: #333;
}

/* Hiệu ứng chuyển động (từ phải qua trái) */
@keyframes scrollTicker {
0% {
transform: translate3d(0, 0, 0);
}
100% {
transform: translate3d(-100%, 0, 0); /* Chạy hết chiều dài của nó */
}
}
Bước 3: Cập nhật code JavaScript đổ dữ liệu
Sửa lại đoạn JS đang dùng để tạo bảng (Table) thành tạo các thẻ <span> nối tiếp nhau:

JavaScript
// Biến hứng dữ liệu từ API
// Giả sử mảng 'data' đang chứa danh sách sinh nhật

const tickerTrack = document.getElementById("upcomingBirthdayTicker");
const tickerWrap = document.querySelector(".birthday-ticker-wrap");

if (data && data.length > 0) {
let tickerHtml = "";

    data.forEach(emp => {
        // Nếu là hôm nay thì dùng nhãn đỏ, còn lại dùng nhãn xám
        let badgeClass = (emp.trangThai === "Hôm nay") 
            ? "badge badge-danger mr-1" 
            : "badge badge-light border mr-1";
            
        tickerHtml += `
            <span class="ticker-item">
                <span class="${badgeClass}">${emp.trangThai}</span> 
                <strong>${emp.hoTen}</strong> (${emp.chucDanh} - ${emp.phong})
            </span>
        `;
    });
    
    // Đẩy vào HTML
    tickerTrack.innerHTML = tickerHtml;
    tickerWrap.classList.remove("d-none"); // Hiện thanh này lên
} else {
// Nếu tuần tới không có ai sinh nhật, ẩn luôn thanh này đi
tickerWrap.classList.add("d-none");
}