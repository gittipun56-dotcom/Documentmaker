# ระบบหนังสือราชการ Android V4

รุ่นนี้เปลี่ยนแกนเป็น **DOCX จริง**: ใช้ไฟล์ Word template ที่กำหนด A4/ขอบกระดาษ/ฟอนต์/ย่อหน้า/แท็บ/รูปแบบไว้ล่วงหน้า แล้วแทนค่าฟิลด์ใน `word/document.xml` ก่อนส่งออกเป็น `.docx` เพื่อให้เปิดแก้ไขต่อใน Microsoft Word ได้

## แบบที่มีในรุ่นนี้
- หนังสือภายนอก
- บันทึกข้อความ
- คำสั่ง

## สิ่งที่แก้จากรุ่นต้นแบบ
- ไม่ใช้การพิมพ์ช่องว่างยาว ๆ เพื่อจัดตำแหน่ง
- ใช้ paragraph properties จริง และ tab stop จริงใน template
- ใส่ตราครุฑจากไฟล์ตราครุฑของชุดงานลงในเอกสารทั้ง 3 แบบ และฝังไว้ใน DOCX จริง
- ใช้ page size A4 และกำหนด margins ใน DOCX
- รองรับผู้รับหลายคนจากรายการที่กำหนด และข้อความผู้รับที่กรอกเอง
- Word ที่ออกเป็นไฟล์ `.docx` จริง ไม่ใช่ HTML เปลี่ยนนามสกุล

## ข้อจำกัดของ V4
- ยังไม่มี binary APK ในชุดนี้ เพราะสภาพแวดล้อมปัจจุบันไม่มี Android SDK/Gradle สำหรับคอมไพล์ APK
- ยังมีเพียง 3 แบบแรกเพื่อทดสอบแกน DOCX; แบบอื่นควรสร้างเป็น template แยก ไม่ควรใช้ layout เดียวครอบทุกประเภท
- การนำ Word ที่ถูกแก้ไขแล้วกลับมาแก้ในแอปยังไม่เปิดใช้ใน V4; ขั้นต่อไปควรใช้ content controls/bookmarks เพื่อให้ round-trip ได้แม่นยำ

## สร้าง demo Word บนเครื่องที่มี Python + python-docx
`python tools/generate_demo.py`

ผลลัพธ์อยู่ใน `demo_output/`


## V4 ฟังก์ชันใช้งานจริงที่เพิ่ม
- สร้าง DOCX จากแบบฟอร์ม 3 ประเภทโดยใช้ OOXML จริง
- ฝังตราครุฑในแม่แบบ
- ตรวจข้อมูลสำคัญก่อนสร้างไฟล์
- บันทึกร่างไว้ในเครื่อง
- เปิด Word (.docx) ที่สร้างจากระบบกลับเข้ามาเพื่อคืนค่าช่องกรอก โดยเก็บ metadata ภายในไฟล์
- ผู้รับ/เรียนเลือกหลายคนและเพิ่มผู้รับเองได้
- ทำงานออฟไลน์ ไม่ต้องใช้ AI หรือเซิร์ฟเวอร์

หมายเหตุ: ไฟล์ APK binary ยังต้องคอมไพล์ด้วย Android SDK/Gradle ในเครื่อง build ที่มี Android toolchain; source project นี้พร้อมสำหรับขั้นตอน build.

## Build APK ฟรีด้วย GitHub Actions (มือถือก็ทำได้)

โครงการนี้มี workflow ที่ `.github/workflows/build-apk.yml` แล้ว เมื่ออัปโหลดโครงการขึ้น GitHub และเปิด Actions ระบบจะใช้ GitHub-hosted Ubuntu runner, Java 17, Android SDK 35 และ Gradle 8.10.2 เพื่อสร้าง `app-debug.apk` แล้วเก็บเป็น Artifact ให้ดาวน์โหลด

ขั้นตอน:
1. สร้าง GitHub repository แบบ Public (เพื่อใช้ standard GitHub-hosted runner ฟรี)
2. อัปโหลดไฟล์/โฟลเดอร์ทั้งหมดของโครงการนี้ขึ้น repository
3. เปิดแท็บ **Actions**
4. เลือก workflow **Build Android APK**
5. กด **Run workflow**
6. รอให้ Job `build-apk` เป็นสีเขียว
7. เปิดรายการ workflow ที่สำเร็จ แล้วดาวน์โหลด Artifact ชื่อ `ระบบหนังสือราชการ-debug-apk`
8. แตกไฟล์ ZIP ของ Artifact จะได้ `app-debug.apk`
9. แตะ APK บน Android เพื่อติดตั้ง

หมายเหตุ: APK นี้เป็น **debug APK ที่ลงลายเซ็น debug ให้โดย Android Gradle Plugin** เหมาะสำหรับใช้งาน/ทดสอบส่วนตัว หากภายหลังต้องการเผยแพร่ใน Play Store ควรทำ release signing แยกต่างหาก
