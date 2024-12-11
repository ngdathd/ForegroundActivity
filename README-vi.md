
# Lấy Danh Sách Activity Đang Ở Trạng Thái Foreground

## Giới thiệu ứng dụng của tôi

<table>
  <tr>
    <td style="width: 30%; text-align: center;">
      <video controls style="max-width: 100%; border: 1px solid #ddd; border-radius: 4px; padding: 5px;">
        <source src="https://github.com/user-attachments/assets/8ae3873a-ed39-4279-9e0d-0b7b6a16d8ea.mp4" type="video/mp4">
        Trình duyệt của bạn không hỗ trợ thẻ video.
      </video>
    </td>
    <td style="width: 70%; vertical-align: top; padding-left: 20px;">
      <p style="font-size: 15px;">- Ứng dụng yêu cầu cấp quyền post notification.</p>
      <p style="font-size: 15px;">- Ở <code>MainActivity</code>, tôi tạo 2 <code>CardView</code> để hiển thị thông tin cơ bản của 2 cách. Khi bấm nút <strong>"More"</strong>, bạn sẽ vào từng cách.</p>
      <p style="font-size: 15px;">- Màn hình của mỗi cách có 2 phần:</p>
      <ul style="font-size: 15px;">
        <li><strong>Phần Header:</strong> Giới thiệu, nút bấm cấp quyền và thông báo về nội dung notification</li>
        <li><strong>Phần List:</strong> Danh sách các activity</li>
      </ul>
      <p style="font-size: 15px;">- Sau khi cấp đủ quyền, ứng dụng sẽ tạo notification có nội dung là activity đang foreground. Bạn có thể chuyển sang ứng dụng khác để kiểm tra.</p>
    </td>
  </tr>
</table>

## Cách 1: Sử dụng `UsageStatsManager`

### Mô tả
`UsageStatsManager` có thể được sử dụng để lấy lịch sử sử dụng ứng dụng, bao gồm thông tin về thời gian và tần suất sử dụng của các ứng dụng trên thiết bị bằng phương thức `queryUsageStats`. Ứng dụng được sử dụng gần nhất có khả năng cao đang ở trạng thái **foreground**. Cứ sau một khoảng thời gian, gọi lại phương thức `queryUsageStats` để cập nhật dữ liệu.

### Ưu điểm
- Cài đặt dễ dàng.
- Không yêu cầu quyền truy cập vào các thông tin cá nhân.

### Nhược điểm
- Dữ liệu cập nhật có độ trễ (không theo thời gian thực).

### Lưu ý về chính sách Google
- Việc sử dụng `UsageStatsManager` yêu cầu khai báo quyền `PACKAGE_USAGE_STATS`. Quyền này không yêu cầu người dùng đồng ý thủ công, nhưng cần được hiển thị trong phần **Settinggs > Apps > Special app access > Usage access**.
- Ứng dụng phải đảm bảo minh bạch với người dùng về cách sử dụng dữ liệu này và tuân thủ chính sách bảo mật của Google Play.

### Các bước thực hiện
<details>
<summary>Click để mở rộng</summary>

1. **Khai báo quyền trong `AndroidManifest.xml`:**
   ```xml
   <uses-permission
        android:name="android.permission.PACKAGE_USAGE_STATS"
        tools:ignore="ProtectedPermissions" />
   ```

2. **Kiểm tra quyền sử dụng trong code:**
   ```java
   private boolean isUsageAccessGranted() {
        AppOpsManager appOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }
   ```

3. **Truy vấn thông tin bằng `UsageStatsManager`:**
   ```java
   UsageStatsManager usageStatsManager =
           (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
   
   if (usageStatsManager == null) {
       return new ArrayList<>(); // Return empty list if UsageStatsManager is unavailable
   }
   
   // Query usage stats for the last 24 hours
   List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(
           UsageStatsManager.INTERVAL_DAILY,
           currentTime - (24 * 60 * 60 * 1000), // Start time (24 hours ago)
           currentTime // End time (now)
   );

   if (usageStatsList == null || usageStatsList.isEmpty()) {
       return new ArrayList<>(); // Return empty list if no data is available
   }
   ```
</details>

---

## Cách 2: Sử dụng `AccessibilityService`

### Mô tả
`AccessibilityService` có khả năng giám sát các ứng dụng đang ở trạng thái **foreground**, cho phép lấy thông tin ứng dụng qua sự kiện `TYPE_WINDOW_STATE_CHANGED`.

### Ưu điểm
- Dữ liệu cập nhật theo thời gian thực.

### Nhược điểm
- Cài đặt phức tạp.
- Quyền đã cho phép có thể bị thu hồi khi tắt ứng dụng.
- Khi cấp phép quyền truy cập `AccessibilityService`, các thông tin khác như nội dung văn bản, hành vi cử chỉ, v.v. cũng có thể bị truy cập.

### Lưu ý về chính sách Google
- Việc sử dụng `AccessibilityService` đòi hỏi ứng dụng phải giải thích rõ ràng lý do cần quyền này và không được lạm dụng để thu thập dữ liệu không liên quan.
- Theo chính sách của Google Play, quyền này chỉ được chấp nhận nếu phục vụ mục đích hỗ trợ người dùng, ví dụ: hỗ trợ người khuyết tật.
- Sử dụng sai mục đích có thể dẫn đến việc ứng dụng bị gỡ bỏ khỏi Google Play.

### Các bước thực hiện
<details>
<summary>Click để mở rộng</summary>

1. **Tạo lớp kế thừa `AccessibilityService`:**
   ```java
   public class AccessibilityServiceExtend extends AccessibilityService {
       @Override
       public void onAccessibilityEvent(AccessibilityEvent event) {
           if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
               // Get the package name and activity name
               ComponentName componentName = new ComponentName(
                       event.getPackageName().toString(),
                       event.getClassName().toString()
               );
   
               String currentPackageName = componentName.getPackageName();
               String currentActivityName = componentName.flattenToShortString();
           }
       }

       @Override
       public void onInterrupt() {
       }
   }
   ```

2. **Khai báo dịch vụ trong `AndroidManifest.xml`:**
   ```xml
   <!--android:foregroundServiceType="mediaPlayback" sử dụng cho tính năng post notification-->
   <service
        android:name=".AccessibilityServiceExtend"
        android:exported="true"
        android:foregroundServiceType="mediaPlayback"
        android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE">
        <intent-filter>
            <action android:name="android.accessibilityservice.AccessibilityService" />
        </intent-filter>

        <meta-data
            android:name="android.accessibilityservice"
            android:resource="@xml/accessibility_service_config" />
   </service>
   ```

3. **Cấu hình tệp `res/xml/accessibility_config.xml`:**
   ```xml
   <accessibility-service xmlns:android="http://schemas.android.com/apk/res/android"
        android:accessibilityEventTypes="typeWindowStateChanged"
        android:accessibilityFeedbackType="feedbackGeneric"
        android:canRetrieveWindowContent="false"
        android:description="@string/app_name"
        android:notificationTimeout="100" />
   ```

4. **Kích hoạt dịch vụ trong cài đặt Accessibility của thiết bị:**
   - Người dùng cần bật dịch vụ trong phần **Settings > Accessibility > ForegroundActivity**.

</details>

---

## So sánh 2 cách

| Đặc điểm                | UsageStatsManager                   | AccessibilityService                  |
|-------------------------|-------------------------------------|---------------------------------------|
| **Cài đặt**             | Dễ dàng                            | Phức tạp                              |
| **Thời gian cập nhật**  | Có độ trễ                          | Theo thời gian thực                   |
| **Quyền truy cập**      | Không yêu cầu quyền nhạy cảm       | Yêu cầu quyền truy cập cao và nhạy cảm|
| **Độ chính xác**        | Phụ thuộc vào thời điểm truy vấn   | Chính xác cao nhờ sự kiện hệ thống    |
| **Tuân thủ chính sách** | Yêu cầu khai báo minh bạch         | Yêu cầu chặt chẽ, tránh lạm dụng      |

---

**Lưu ý:** Lựa chọn cách thực hiện tùy thuộc vào yêu cầu dự án và mức độ ưu tiên giữa hiệu năng, bảo mật, và tuân thủ chính sách Google.
